package daripher.itemproduction.mixin.davespotioneering;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;
import tfar.davespotioneering.inv.BrewingHandler;

@Mixin(value = AdvancedBrewingStandBlockEntity.class, remap = false)
public class AdvancedBrewingStandBlockEntityMixin {
  private @Shadow @Final BrewingHandler brewingHandler;

  @Inject(method = "brewPotions", at = @At("TAIL"))
  private void enhanceBrewedPotions(CallbackInfo callbackInfo) {
    @SuppressWarnings("DataFlowIssue")
    AdvancedBrewingStandBlockEntity blockEntity = (AdvancedBrewingStandBlockEntity) (Object) this;
    for (int slot = 0; slot < 3; slot++) {
      ItemStack stack = brewingHandler.getStackInSlot(slot);
      stack = ItemProductionLib.itemProduced(stack, blockEntity);
      brewingHandler.setStackInSlot(slot, stack);
    }
  }
}
