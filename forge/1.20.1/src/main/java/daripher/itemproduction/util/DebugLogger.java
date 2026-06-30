package daripher.itemproduction.util;

import daripher.itemproduction.config.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DebugLogger {
    private static final Logger LOGGER = LogManager.getLogger("ItemProductionLib");
    private static final String LINE_SEPARATOR = "------------------------------------------------------------------------";

    private DebugLogger() {
        // Privater Utility-Konstruktor für SonarQube (java:S1118)
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Spezialisierter Test-Block exklusiv fuer Kochtöpfe und Küchengeräte.
     */
    public static void logCookingPotStack(String playerName, String itemName, int count, String clickType) {
        if (Boolean.FALSE.equals(ModConfig.SHOW_STACK_TEST_LOGS.get())) return;

        LOGGER.warn("================ [SKILL-TREE-KOCHTOPF-TEST] ================");
        LOGGER.warn("SPIELER: {}", playerName);
        LOGGER.warn("GERICHT: {}", itemName);
        LOGGER.warn("MENGE IM SLOT: {}", count);
        LOGGER.warn("KLICK-TYP: {}", clickType);
        LOGGER.warn("------------------------------------------------------------");
    }

    /**
     * Intelligenter, zentraler Logger fuer die Hauptklasse.
     * Nutzt den übergebenen 'productionType' für perfekte Config-Filterung!
     */
    public static void logItemProduction(ItemStack stack, Player player, String crafterName, String productionType) {
        if (player == null || stack == null) return;

        String playerName = player.getName().getString();
        String itemName = stack.getItem().toString();
        int count = stack.getCount();
        String headerTitle;

        switch (productionType.toLowerCase()) {
            case "crafting":
                if (Boolean.FALSE.equals(ModConfig.ENABLE_CRAFTING_LOGS.get())) return;
                headerTitle = "============ [WERKBANK-PRODUKTION] ================";
                break;
            case "furnace":
                if (Boolean.FALSE.equals(ModConfig.ENABLE_FURNACE_LOGS.get())) return;
                headerTitle = "============ [OFEN-SCHMELZVORGANG] ================";
                break;
            case "smoker":
                if (Boolean.FALSE.equals(ModConfig.ENABLE_SMOKER_LOGS.get())) return;
                headerTitle = "============ [RAEUCHEROFEN-KOCHVORGANG] ===========";
                break;
            case "blast_furnace":
                if (Boolean.FALSE.equals(ModConfig.ENABLE_BLAST_FURNACE_LOGS.get())) return;
                headerTitle = "============ [SCHMELZOFEN-PRODUKTION] =============";
                break;
            case "brewing":
                if (Boolean.FALSE.equals(ModConfig.ENABLE_BREWING_LOGS.get())) return;
                headerTitle = "============ [ALCHEMIE-BRAUSTAND] ==================";
                break;
            case "cookingpot":
                if (Boolean.FALSE.equals(ModConfig.SHOW_STACK_TEST_LOGS.get())) return;
                headerTitle = "============ [FARMERS-DELIGHT KOCHTOPF] ============";
                break;
            case "skillet":
                if (Boolean.FALSE.equals(ModConfig.SHOW_STACK_TEST_LOGS.get())) return;
                headerTitle = "============ [FARMERS-DELIGHT BRATPFANNE] ==========";
                break;
            default:
                if (Boolean.FALSE.equals(ModConfig.ENABLE_UNKNOWN_LOGS.get())) return;
                headerTitle = "============ [ZUSAETZLICHES MOD-MENUE: " + productionType.toUpperCase() + "] ============";
                break;
        }

        // Der eigentliche Log-Vorgang wird zentral ausgeführt
        LOGGER.warn(headerTitle);
        LOGGER.warn("[INFO] Typ/Klasse: {}", productionType);
        LOGGER.warn("[INFO] Gegenstand: {} x {}", count, itemName);
        LOGGER.warn("[INFO] Gecraftet von: {}", crafterName);
        LOGGER.warn("[INFO] Entnommen von: {}", playerName);
        LOGGER.warn(LINE_SEPARATOR);
    }
}
