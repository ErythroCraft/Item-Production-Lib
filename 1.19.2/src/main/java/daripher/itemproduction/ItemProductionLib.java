package daripher.itemproduction;

import daripher.itemproduction.block.entity.Interactive;
import daripher.itemproduction.event.ItemProducedEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(ItemProductionLib.MOD_ID)
public class ItemProductionLib {
  public static final String MOD_ID = "itemproductionlib";

  public ItemProductionLib() {
    IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
    forgeEventBus.addListener(this::setBlockEntityUser);
  }

  private void setBlockEntityUser(PlayerInteractEvent.RightClickBlock event) {
    BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
    if (blockEntity instanceof Interactive interactive) {
      interactive.setUser(event.getEntity());
    }
  }

  public static ItemStack itemProduced(ItemStack stack, Player player) {
    if (stack.isEmpty()) return stack;
    ItemProducedEvent event = new ItemProducedEvent(stack, player);
    MinecraftForge.EVENT_BUS.post(event);
    return event.getStack();
  }

  public static ItemStack itemProduced(ItemStack stack, BlockEntity blockEntity) {
    if (!(blockEntity instanceof Interactive interactive)) return stack;
    Player user = interactive.getUser();
    return user == null ? stack : itemProduced(stack, user);
  }
}
