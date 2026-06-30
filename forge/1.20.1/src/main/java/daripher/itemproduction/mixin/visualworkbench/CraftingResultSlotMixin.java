package daripher.itemproduction.mixin.visualworkbench;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Klinkt sich in die Entnahme des fertigen Items aus dem Crafting-Ergebnis-Slot ein.
 * Funktioniert universell für die Visual Workbench, da diese die Standard-Vanilla-Slot-Logik erbt.
 */
@Mixin(net.minecraft.world.inventory.ResultSlot.class)
public class CraftingResultSlotMixin {

    /**
     * 'onTake' wird aufgerufen, wenn der Spieler (oder ein Shift-Klick) das Item erfolgreich 
     * aus dem Ergebnisfeld der Werkbank herausnimmt.
     */
    @Inject(method = "onTake", at = @At("HEAD"), remap = true)
    private void onCraftingItemTaken(Player player, ItemStack stack, CallbackInfo ci) {
        // Nur auf dem Server verarbeiten
        if (player == null || player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Falls der Slot aus irgendeinem Grund leer sein sollte, abbrechen
        if (stack == null || stack.isEmpty()) {
            return;
        }

        try {
            // KORREKTUR 1: Wir reichen den ECHTEN Stack hinein, kein .copy()!
            // Die Library erhöht die Anzahl direkt in dem Gegenstand, den der Spieler gerade aus dem Slot zieht.
            // Der Typ "crafting" wird mitgesendet, damit deine Configs und Logs exakt greifen.
            ItemProductionLib.itemProduced(stack, serverPlayer, "crafting");

            // KORREKTUR 2: Veraltete Schleifen-Logger-Aufrufe entfernt
        } catch (Exception e) {
            org.apache.logging.log4j.LogManager.getLogger("ItemProductionLib")
                    .warn("[CRAFTING-FEHLER] Fehler bei der Verarbeitung im Crafting-Slot: {}", e.getMessage());
        }
    }
}
