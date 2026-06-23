package daripher.itemproduction.mixin.farmersdelight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

@Mixin(value = StoveBlockEntity.class)
public class StoveBlockEntityMixin {
  @SuppressWarnings("DefaultAnnotationParam")
  @ModifyExpressionValue(
      method = "cookAndOutputItems",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/item/crafting/CampfireCookingRecipe;"
                      + "getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;",
              remap = true),
      remap = false)
  private ItemStack itemProduced(ItemStack original) {
    @SuppressWarnings("DataFlowIssue")
    StoveBlockEntity blockEntity = (StoveBlockEntity) (Object) this;
    return ItemProductionLib.itemProduced(original, blockEntity);
  }
}
