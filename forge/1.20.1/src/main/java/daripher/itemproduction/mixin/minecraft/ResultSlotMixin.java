package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.util.ItemProcessingHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Absolut ausfallsicherer Klick-Trigger fuer das Steinschneider-Menue.
 * Klinkt sich in die fundamentale 'clicked'-Methode ein, die immer existiert.
 */
@Mixin(AbstractContainerMenu.class)
public class ResultSlotMixin {

    @Inject(method = "clicked", at = @At("HEAD"))
    private void onStonecutterSlotClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (player == null || player.level().isClientSide() || !(player instanceof ServerPlayer)) {
            return;
        }

        // Wir filtern nach Spezial-Klicks (wie Caps-Lock), um den Editor zu schuetzen
        if (clickType == ClickType.CLONE) {
            return;
        }

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        String menuClassName = menu.getClass().getName();

        // Slot 1 ist in Minecraft beim Steinschneider der Ausgabe-Slot (0 = Input, 1 = Output)
        if (menuClassName.contains("StonecutterMenu") && slotId == 1) {
            try {
                Slot slot = menu.getSlot(slotId);
                ItemStack stack = slot.getItem();

                if (!stack.isEmpty() && !ItemProcessingHelper.isProcessed(stack)) {
                    ItemProcessingHelper.markAsProcessed(stack, player);

                    // Schickt das Item an die Hauptklasse. Stacks werden dort automatisch zerlegt!
                    ItemProductionLib.itemProduced(stack.copy(), player);
                }
            } catch (Exception ignored) {
                // Verhindert unvorhergesehene Abstuerze
            }
        }
    }
}
