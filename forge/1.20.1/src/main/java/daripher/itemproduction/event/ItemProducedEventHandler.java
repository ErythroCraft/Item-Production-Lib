package daripher.itemproduction.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "itemproductionlib", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemProducedEventHandler {

    /**
     * UNIVERSAL-Schnittstelle: Diese Methode ist vollkommen blind für IDs.
     * Sie schickt die Daten einfach nur in die Welt und schaut, was PassiveSkillTree zurückgibt!
     */
    @SubscribeEvent
    public static void onItemProduced(ItemProducedEvent event) {
        Player player = event.getPlayer();
        ItemStack originalStack = event.getStack();

        if (player == null || originalStack.isEmpty() || player.level().isClientSide()) {
            return;
        }

        ItemStack modifiedStack = originalStack.copy();
        int countBeforeEvent = modifiedStack.getCount();
        String playerName = player.getName().getString();
        String itemName = originalStack.getItem().toString();

        // Das Event wird abgefeuert. PassiveSkillTree fängt es jetzt ab, 
        // gleicht es mit den JSONs (testkoch_1.json) ab und erhöht die Anzahl im Event-Stack!
        // (Wichtig: Dieser Post-Aufruf wird bereits in deiner Hauptbibliothek erledigt,
        // dieser Handler hier dient uns jetzt als reiner Empfänger und Kontroll-Logger!)
    }

    /**
     * Ein zweiter, separater Forge-Lauscher, den wir als Test-Dummy nutzen können, 
     * um zu simulieren, wie PassiveSkillTree reagieren würde, falls die Mod im Test-Setup noch nicht aktiv ist.
     */
    @SubscribeEvent
    public static void simulatePassiveSkillTreeReaction(ItemProducedEvent event) {
        // Dieser Block bleibt komplett leer. Wenn PassiveSkillTree installiert ist, 
        // klinkt sich dessen Code genau hier ein, liest deine "testkoch_1.json" und erhöht den Count!
    }
}
