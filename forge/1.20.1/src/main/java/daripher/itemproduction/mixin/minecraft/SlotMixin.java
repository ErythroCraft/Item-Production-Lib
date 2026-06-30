package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow
    public int index;

    /**
     * Klinkt sich in die 'onTake'-Methode JEDES Slots ein.
     * Filtert gezielt die verbleibenden Vanilla-Ausgabelots heraus.
     * Vollständig befreit von Item-Tags für perfektes Gegenstands-Stacking!
     */
    @Inject(method = "onTake", at = @At("HEAD"), remap = true)
    private void onTakeItemFromOutputSlot(Player player, ItemStack stack, CallbackInfo ci) {
        // Nur auf dem Server verarbeiten, wenn das Item nicht leer ist
        if (player == null || player.level().isClientSide() || stack.isEmpty() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AbstractContainerMenu containerMenu = player.containerMenu;
        if (containerMenu == null) {
            return;
        }

        int slotId = this.index;

        // Wir prüfen nur noch Menüs, die wir NICHT bereits über spezifische Mixins abgefangen haben.
        if (isRemainingVanillaOutputSlot(containerMenu, slotId)) {
            try {
                // KORREKTUR 1: Dynamische Typ-Erkennung für das Log, damit deine Config-Schalter exakt getroffen werden
                String productionType = "ui_production";
                if (containerMenu instanceof AnvilMenu) productionType = "anvil";
                else if (containerMenu instanceof GrindstoneMenu) productionType = "grindstone";
                else if (containerMenu instanceof CartographyTableMenu) productionType = "cartography_table";
                else if (containerMenu instanceof EnchantmentMenu) productionType = "enchantment";

                // KORREKTUR 2: Wir reichen den ECHTEN Stack hinein, kein .copy()!
                // Die Library berechnet die Boni und erhöht die Anzahl direkt in dem Gegenstand,
                // den der Spieler gerade mit der Maus aus dem UI zieht!
                ItemProductionLib.itemProduced(stack, serverPlayer, productionType);

                // KORREKTUR 3: Veraltete Logger-Schleifen-Methoden wurden entfernt
            } catch (Exception e) {
                org.apache.logging.log4j.LogManager.getLogger("ItemProductionLib")
                        .warn("[UI-FEHLER] Fehler bei der Verarbeitung im Slot {}: {}", slotId, e.getMessage());
            }
        }
    }

    /**
     * Prüft, ob es sich um den Ausgangsslot der verbleibenden Vanilla-Menüs handelt.
     */
    @Unique
    private boolean isRemainingVanillaOutputSlot(AbstractContainerMenu containerMenu, int slotId) {
        return (containerMenu instanceof AnvilMenu && slotId == 2)
                || (containerMenu instanceof GrindstoneMenu && slotId == 2)
                || (containerMenu instanceof CartographyTableMenu && slotId == 2)
                || (containerMenu instanceof EnchantmentMenu && slotId == 0);
    }
}
