package daripher.itemproduction.mixin.davespotioneering;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;
import tfar.davespotioneering.blockentity.CAdvancedBrewingStandBlockEntity;

@Mixin(value = AdvancedBrewingStandBlockEntity.class, remap = false)
public abstract class AdvancedBrewingStandBlockEntityMixin
    extends CAdvancedBrewingStandBlockEntity {
  @SuppressWarnings("DataFlowIssue")
  private AdvancedBrewingStandBlockEntityMixin() {
    super(null, null, null);
  }

  @Inject(method = "brewPotions", at = @At("TAIL"))
  private void enhanceBrewedPotions(CallbackInfo callbackInfo) {
    @SuppressWarnings("DataFlowIssue")
    AdvancedBrewingStandBlockEntity blockEntity = (AdvancedBrewingStandBlockEntity) (Object) this;
    for (int slot = 0; slot < 3; slot++) {
      ItemStack stack = handler.$getStackInSlot(slot);
      stack = ItemProductionLib.itemProduced(stack, blockEntity);
      handler.$setStackInSlot(slot, stack);
    }
  }
}
