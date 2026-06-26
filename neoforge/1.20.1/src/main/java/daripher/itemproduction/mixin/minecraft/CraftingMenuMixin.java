package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack; // SAUBERER IMPORT HINZUGEFÜGT
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {

  @Shadow
  @Final
  private ResultContainer resultSlots;
  @Shadow
  @Final
  private Player player;

  @Unique
  private boolean itemProductionIsProcessing = false;

  @Inject(method = "slotsChanged", at = @At(value = "TAIL"))
  private void itemProduced(Container container, CallbackInfo callbackInfo) {
    if (this.itemProductionIsProcessing) {
      return;
    }

    // KORREKTUR: Modifikation NUR auf dem Server erlauben, um
    // Client-Desynchronisationen zu verhindern!
    if (this.player != null && !this.player.level().isClientSide()) {
      ItemStack outputStack = this.resultSlots.getItem(0);

      if (!outputStack.isEmpty()) {
        try {
          this.itemProductionIsProcessing = true;

          ItemStack modifiedStack = ItemProductionLib.itemProduced(outputStack.copy(), this.player);

          this.resultSlots.setItem(0, modifiedStack);
        } finally {
          this.itemProductionIsProcessing = false;
        }
      }
    }
  }
}
