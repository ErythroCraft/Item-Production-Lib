package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.class)
public abstract class FurnacePassiveResultMixin {

    // Shadow-Verweis auf das echte, interne Item-Array des Ofens
    @Shadow
    protected NonNullList<ItemStack> items;

    /**
     * KORREKTUR 1: Methode ist nun KEIN 'static' mehr, da 'smelt' eine Instanzmethode ist!
     * KORREKTUR 2: Die Signatur wurde auf die korrekte Instanz-Ebene für 1.20.1 angepasst.
     */
    @Inject(method = "smelt", at = @At("RETURN"), remap = true)
    private void onSmeltItemFinished(Recipe<?> recipe, NonNullList<ItemStack> recipeItems, int maxStackSize, CallbackInfoReturnable<Boolean> cir) {
        // Nur verarbeiten, wenn das Schmelzen in diesem Tick erfolgreich war (Return-Wert ist true)
        if (!cir.getReturnValue() || this.items == null) {
            return;
        }

        // Slot 2 ist bei allen Öfen (Furnace, Smoker, Blast Furnace) IMMER der Ergebnis-Slot
        ItemStack outputStack = this.items.get(2);
        if (outputStack.isEmpty()) {
            return;
        }

        net.minecraft.world.level.block.entity.BlockEntity blockEntity = (net.minecraft.world.level.block.entity.BlockEntity) (Object) this;
        net.minecraft.world.level.Level level = blockEntity.getLevel();

        if (level == null || level.isClientSide()) {
            return;
        }

        // Spieler über das funktionierende Interactive-Interface ermitteln
        Player foundPlayer = null;
        if ((Object) this instanceof Interactive interactive) {
            foundPlayer = interactive.resolveUser(level);
        }

        ServerPlayer targetPlayer = null;
        if (foundPlayer instanceof ServerPlayer serverPlayer) {
            targetPlayer = serverPlayer;
        } else {
            // Fallback: Nahegelegenster Spieler (Falls automatisiert befüllt)
            net.minecraft.core.BlockPos pos = blockEntity.getBlockPos();
            Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 12.0, false);
            if (closestPlayer instanceof ServerPlayer serverPlayer) {
                targetPlayer = serverPlayer;
            }
        }

        if (targetPlayer != null) {
            // Ermittelt den genauen Ofentyp für deine Config-Schalter
            String productionType = "furnace";
            if (blockEntity instanceof net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity) {
                productionType = "blast_furnace";
            } else if (blockEntity instanceof net.minecraft.world.level.block.entity.SmokerBlockEntity) {
                productionType = "smoker";
            }

            // Wir jagen den echten Stack durch die Library. Boni werden direkt in den Slot geschrieben!
            ItemProductionLib.itemProduced(outputStack, targetPlayer, productionType);
        }
    }
}
