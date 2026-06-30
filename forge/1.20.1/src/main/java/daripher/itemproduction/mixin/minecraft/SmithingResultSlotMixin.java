package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Klinkt sich in das SmithingMenu ein, um die Produktion beim Schmiedetisch abzufangen.
 * Nutzt die offizielle Slot-API des Containers, um Sichtbarkeitsfehler komplett zu umgehen.
 */
@Mixin(SmithingMenu.class)
public class SmithingResultSlotMixin {

    @Unique
    private boolean itemproductionIsProcessing = false;

    /**
     * 'createResult' wird von Minecraft aufgerufen, sobald eine gültige Kombination 
     * (z.B. Werkzeug + Netherite + Upgrade-Template) im Schmiedetisch liegt.
     */
    @Inject(method = "createResult", at = @At("TAIL"), remap = true)
    private void onSmithingCreateResultTail(CallbackInfo ci) {
        if (this.itemproductionIsProcessing) {
            return;
        }

        // Wir nutzen den Accessor, um das geschützte 'player' Feld sicher auszulesen
        ItemCombinerMenu combinerMenu = (ItemCombinerMenu) (Object) this;
        Player player = ((ItemCombinerMenuAccessor) combinerMenu).getPlayer();

        // Nur auf dem Server verarbeiten, wenn ein gültiger Spieler aktiv ist
        if (player != null && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {

            // Verwendung des standardisierten Container-Systems
            // Slot 2 ist beim Schmiedetisch IMMER der Ergebnis-Slot (0 = Basis, 1 = Upgrade, 2 = Ergebnis)
            AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;

            try {
                Slot resultSlot = menu.getSlot(2);
                ItemStack outputStack = resultSlot.getItem();

                if (!outputStack.isEmpty()) {
                    try {
                        this.itemproductionIsProcessing = true;

                        // KORREKTUR 1: Wir reichen den ECHTEN Stack hinein, kein .copy()!
                        // Die Library erhöht die Anzahl direkt im Objekt, sodass die Vorschau im Schmiedetisch
                        // sofort die korrekte, bonus-modifizierte Anzahl anzeigt!
                        // Der Typ "smithing" wird explizit mitgeliefert, damit deine Configs greifen.
                        ItemProductionLib.itemProduced(outputStack, serverPlayer, "smithing");

                        // KORREKTUR 2: Veraltete Schleifen-Logger-Aufrufe entfernt
                    } catch (Exception e) {
                        org.apache.logging.log4j.LogManager.getLogger("ItemProductionLib")
                                .warn("[SCHMIEDE-FEHLER] Fehler bei der Verarbeitung im Schmiede-Menü: {}", e.getMessage());
                    } finally {
                        this.itemproductionIsProcessing = false;
                    }
                }
            } catch (Exception ignored) {
                // Schützt vor ungültigen Slot-Zuweisungen, falls andere Mods das Menü verändern
            }
        }
    }
}
