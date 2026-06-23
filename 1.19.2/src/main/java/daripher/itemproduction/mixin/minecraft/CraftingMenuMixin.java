package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
  private @Shadow @Final ResultContainer resultSlots;
  private @Shadow @Final Player player;

  @Inject(method = "slotsChanged", at = @At(value = "TAIL"))
  private void itemProduced(Container container, CallbackInfo callbackInfo) {
    resultSlots.setItem(0, ItemProductionLib.itemProduced(resultSlots.getItem(0), player));
  }
}
