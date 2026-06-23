package daripher.itemproduction;

import daripher.itemproduction.block.entity.Interactive;
import daripher.itemproduction.event.ItemProducedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Kernbibliothek zur Erfassung und Modifikation von produzierten Gegenständen
 * im Zusammenspiel mit dem Passive Skill Tree.
 */
@Mod(ItemProductionLib.MOD_ID)
public class ItemProductionLib {
  public static final String MOD_ID = "itemproductionlib";

  /**
   * Konstruktor registriert die grundlegenden Interaktions-Listener am
   * Forge-Event-Bus.
   */
  public ItemProductionLib() {
    IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

    forgeEventBus.addListener(this::setBlockEntityUser);
    forgeEventBus.addListener(this::onRightClickCookingBlock);

    // Aktiviert diese Instanz für klassische @SubscribeEvent-Methoden (wie das
    // Schmelzen)
    forgeEventBus.register(this);
  }

  private void setBlockEntityUser(PlayerInteractEvent.RightClickBlock event) {
    BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
    if (blockEntity instanceof Interactive interactive) {
      interactive.setUser(event.getEntity());
    }
  }

  private void onRightClickCookingBlock(PlayerInteractEvent.RightClickBlock event) {
    Level level = event.getLevel();
    BlockPos pos = event.getPos();
    Player player = event.getEntity();

    if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND
        || !(player instanceof ServerPlayer serverPlayer)) {
      return;
    }

    BlockEntity blockEntity = level.getBlockEntity(pos);
    if (blockEntity == null) {
      return;
    }

    if (blockEntity.getClass().getName().contains("StoveBlockEntity")) {
      processStoveCooking(blockEntity, serverPlayer);
    } else if (blockEntity instanceof CampfireBlockEntity campfire) {
      processCampfireCooking(campfire, serverPlayer);
    }
  }

  private void processStoveCooking(BlockEntity blockEntity, ServerPlayer player) {
    try {
      java.lang.reflect.Method getStoredItemMethod = blockEntity.getClass().getMethod("getStoredItem", int.class);
      java.lang.reflect.Field progressField = blockEntity.getClass().getField("cookingProgress");
      int[] progress = (int[]) progressField.get(blockEntity);

      for (int i = 0; i < 4; i++) {
        ItemStack resultStack = (ItemStack) getStoredItemMethod.invoke(blockEntity, i);
        if (!resultStack.isEmpty() && progress[i] == 0) {
          ItemStack modified = itemProduced(resultStack.copy(), player);
          resultStack.setTag(modified.getTag());
          resultStack.setCount(modified.getCount());
        }
      }
    } catch (Exception ignored) {
      // NOSONAR: Ignorieren verhindert Abstürze bei inkompatiblen Mod-Versionen von
      // Farmer's Delight
    }
  }

  private void processCampfireCooking(CampfireBlockEntity campfire, ServerPlayer player) {
    try {
      int[] cookingProgress = net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(
          CampfireBlockEntity.class, campfire, "cookingProgress");
      int[] cookingTime = net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(
          CampfireBlockEntity.class, campfire, "cookingTime");

      if (cookingProgress != null && cookingTime != null) {
        for (int i = 0; i < campfire.getItems().size(); i++) {
          ItemStack resultStack = campfire.getItems().get(i);
          if (!resultStack.isEmpty() && cookingProgress[i] >= cookingTime[i]) {
            ItemStack modified = itemProduced(resultStack.copy(), player);
            resultStack.setTag(modified.getTag());
            resultStack.setCount(modified.getCount());
          }
        }
      }
    } catch (Exception ignored) {
      // NOSONAR: Schützt vor Inkompatibilitäten bei veränderten Vanilla-Campfires
    }
  }

  public static ItemStack itemProduced(ItemStack stack, Player player) {
    if (stack.isEmpty()) {
      return stack;
    }
    ItemProducedEvent event = new ItemProducedEvent(stack, player);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getStack();
  }

  public static ItemStack itemProduced(ItemStack stack, BlockEntity blockEntity) {
    if (!(blockEntity instanceof Interactive interactive)) {
      return stack;
    }
    Player user = interactive.getUser();
    return user == null ? stack : itemProduced(stack, user);
  }

  /**
   * Berechnet den Produktionsgeschwindigkeits-Multiplikator für zeitliche und
   * permanente Buffs.
   */
  public static float getProductionSpeedMultiplier(Player player, String productionType) {
    if (!(player instanceof ServerPlayer serverPlayer)) {
      return 1.0f;
    }

    float multiplier = 1.0f;
    net.minecraft.nbt.CompoundTag forgeData = serverPlayer.getPersistentData();
    String nbtKey = "ProductionBuff_" + productionType.toUpperCase();

    if (forgeData.contains(nbtKey)) {
      long expiryTime = forgeData.getLong(nbtKey);
      if (serverPlayer.level().getGameTime() < expiryTime) {
        multiplier += 0.3f;
      } else {
        forgeData.remove(nbtKey);
      }
    }

    return multiplier;
  }

  /**
   * FORGE OFEN EVENT:
   * Erfasst das fertig geschmolzene Item, sobald es aus dem Ofen-Ausgabeslot
   * entnommen wird.
   * KORREKTUR: Nutzt den ObfuscationReflectionHelper, um das geschützte Feld
   * 'smelting' auszulesen.
   */
  @SubscribeEvent
  public void onPlayerSmeltItem(PlayerEvent.ItemSmeltedEvent event) {
    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
      try {
        // Forge holt das geschützte Feld 'smelting' sicher aus dem Event und schaltet
        // es frei
        ItemStack original = net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(
            PlayerEvent.ItemSmeltedEvent.class, event, "smelting");

        if (original != null && !original.isEmpty()) {
          ItemStack modified = itemProduced(original.copy(), serverPlayer);
          original.setTag(modified.getTag());
          original.setCount(modified.getCount());
        }
      } catch (Exception ignored) {
        // NOSONAR: Schützt vor Inkompatibilitäten bei abweichenden Forge-Mapping-Builds
      }
    }
  }

  /**
   * FORGE CRAFTING & KOCH EVENT:
   * Fängt das fertige Item ab, sobald der Spieler das Essen aus dem Kochtopf
   * (oder der Werkbank) herausnimmt. Ersetzt das instabile Mixin perfekt!
   */
  @SubscribeEvent
  public void onPlayerCraftOrCook(PlayerEvent.ItemCraftedEvent event) {
    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
      ItemStack original = event.getCrafting();

      // KORREKTUR: 'original != null' entfernt, da getCrafting() niemals null
      // zurückgibt.
      // '!original.isEmpty()' reicht völlig aus, um gültige Items zu verarbeiten.
      if (!original.isEmpty()) {
        // Berechnet die Skills und wertet die Suppe direkt beim Herausnehmen auf
        ItemStack modified = itemProduced(original.copy(), serverPlayer);
        original.setTag(modified.getTag());
        original.setCount(modified.getCount());
      }
    }
  }

}
