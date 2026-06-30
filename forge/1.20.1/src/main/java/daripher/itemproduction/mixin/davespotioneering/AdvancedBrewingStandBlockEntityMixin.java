package daripher.itemproduction.mixin.davespotioneering;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable; // WICHTIG: Für das physische Zurückschreiben in die Slots
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;

@Mixin(value = AdvancedBrewingStandBlockEntity.class, remap = false)
public abstract class AdvancedBrewingStandBlockEntityMixin {

    @Inject(method = "brewPotions", at = @At("TAIL"))
    private void enhanceBrewedPotions(CallbackInfo callbackInfo) {
        AdvancedBrewingStandBlockEntity blockEntity = (AdvancedBrewingStandBlockEntity) (Object) this;
        Level level = blockEntity.getLevel();

        if (level == null || level.isClientSide()) {
            return;
        }

        // 1. Greife über das Interactive-Interface sicher auf den Spieler zu
        Player foundPlayer = null;
        if ((Object) this instanceof Interactive interactive) {
            foundPlayer = interactive.resolveUser(level);
        }

        ServerPlayer targetPlayer = null;
        if (foundPlayer instanceof ServerPlayer serverPlayer) {
            targetPlayer = serverPlayer;
        }

        // FALLBACK: Umkreis-Suche
        if (targetPlayer == null) {
            BlockPos pos = blockEntity.getBlockPos();
            Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
            if (closestPlayer instanceof ServerPlayer serverPlayer) {
                targetPlayer = serverPlayer;
            }
        }

        // 2. Wenn ein Spieler zugeordnet werden konnte, verarbeite die 3 Trank-Ausgangsslots
        if (targetPlayer != null) {
            final ServerPlayer finalPlayer = targetPlayer;

            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((IItemHandler handler) -> {
                // Slots 0, 1 und 2 sind die Trank-Ausgänge
                for (int slot = 0; slot < 3; slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);

                    if (!stack.isEmpty()) {
                        // KORREKTUR 1: Typ-Angabe "brewing" explizit mitsenden, damit deine Configs greifen
                        // KORREKTUR 2: Wir reichen den ECHTEN Stack hinein. Die Library berechnet die Boni 
                        // und erhöht die Anzahl direkt im Objekt.
                        ItemProductionLib.itemProduced(stack, finalPlayer, "brewing");

                        // KORREKTUR 3: Da IItemHandler standardmäßig schreibgeschützt sein kann, 
                        // erzwingen wir ein physisches Update des Slots, falls sich die Anzahl geändert hat
                        if (handler instanceof IItemHandlerModifiable modifiableHandler) {
                            modifiableHandler.setStackInSlot(slot, stack);
                        }
                    }
                }
            });
        }
    }
}
