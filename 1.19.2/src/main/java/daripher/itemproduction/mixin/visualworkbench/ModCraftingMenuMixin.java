package daripher.itemproduction.mixin.visualworkbench;

import daripher.itemproduction.ItemProductionLib;
import fuzs.visualworkbench.world.inventory.ModCraftingMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModCraftingMenu.class, remap = false)
public class ModCraftingMenuMixin {
  private @Shadow @Final ResultContainer resultSlots;
  private @Shadow @Final Player player;

  @Inject(method = "slotsChanged", at = @At(value = "TAIL"), remap = true)
  private void itemProduced(Container container, CallbackInfo callbackInfo) {
    resultSlots.setItem(0, ItemProductionLib.itemProduced(resultSlots.getItem(0), player));
  }
}
