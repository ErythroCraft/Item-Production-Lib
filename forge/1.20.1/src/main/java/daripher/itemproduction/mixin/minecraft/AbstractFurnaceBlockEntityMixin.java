package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

    @Unique
    private static int itemproductionLastOutputCount = 0;

    /**
     * Klinkt sich am ANFANG des Ofen-Ticks ein, um den Zustand des Ausgangsslots zu sichern.
     */
    @Inject(method = "serverTick", at = @At("HEAD"), remap = true)
    private static void onFurnaceTickHead(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }
        // Slot 2 ist bei allen Öfen (Furnace, Smoker, Blast Furnace) IMMER der Ergebnis-Slot
        ItemStack output = blockEntity.getItem(2);
        itemproductionLastOutputCount = output.isEmpty() ? 0 : output.getCount();

        // DEIN GESCHWINDIGKEITS-BUFF (Wird weiterhin am Anfang ausgeführt)
        if ((Object) blockEntity instanceof Interactive interactive) {
            Player user = interactive.resolveUser(level);
            if (user instanceof ServerPlayer player) {
                applyFurnaceSpeedBoost(level, blockEntity, player);
            }
        }
    }

    /**
     * Klinkt sich am ENDE des Ofen-Ticks ein. 
     * Wenn die Anzahl im Slot gestiegen ist, wurde passiv ein Item geschmolzen!
     */
    @Inject(method = "serverTick", at = @At("TAIL"), remap = true)
    private static void onFurnaceTickTail(Level level, BlockPos pos, BlockState state, AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        ItemStack outputStack = blockEntity.getItem(2);
        if (outputStack.isEmpty()) {
            return;
        }

        // Wenn die Anzahl im Ausgangsslot exakt in diesem Tick größer geworden ist
        if (outputStack.getCount() > itemproductionLastOutputCount) {

            Player foundPlayer = null;
            if ((Object) blockEntity instanceof Interactive interactive) {
                foundPlayer = interactive.resolveUser(level);
            }

            ServerPlayer targetPlayer = null;
            if (foundPlayer instanceof ServerPlayer serverPlayer) {
                targetPlayer = serverPlayer;
            } else {
                // Fallback Umkreis
                Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 12.0, false);
                if (closestPlayer instanceof ServerPlayer serverPlayer) {
                    targetPlayer = serverPlayer;
                }
            }

            if (targetPlayer != null) {
                // Ermittelt den genauen Ofentyp für deine Config-Schalter
                String productionType = "furnace";
                if (blockEntity instanceof BlastFurnaceBlockEntity) {
                    productionType = "blast_furnace";
                } else if (blockEntity instanceof SmokerBlockEntity) {
                    productionType = "smoker";
                }

                // Wir jagen den echten Stack durch die Library. Boni werden direkt in den Slot geschrieben!
                ItemProductionLib.itemProduced(outputStack, targetPlayer, productionType);
            }
        }
    }

    /**
     * Erhöht den Ofen-Fortschritt basierend auf den gelernten Skilltree-Multiplikatoren.
     */
    @Unique
    private static void applyFurnaceSpeedBoost(Level level, AbstractFurnaceBlockEntity blockEntity, ServerPlayer player) {
        float speedMultiplier = ItemProductionLib.getProductionSpeedMultiplier(player, "furnace");

        if (speedMultiplier > 1.0f) {
            int tickInterval = (int) (1.0f / (speedMultiplier - 1.0f));

            if (tickInterval > 0 && level.getGameTime() % tickInterval == 0) {
                ContainerData data = ((AbstractFurnaceBlockEntityAccessor) blockEntity).itemproductionGetDataAccess();
                if (data != null) {
                    int currentProgress = data.get(2); // cookingProgress
                    int totalTime = data.get(3);       // cookingTotalTime

                    if (currentProgress > 0 && currentProgress < totalTime) {
                        data.set(2, currentProgress + 1);
                    }
                }
            }
        }
    }
}
