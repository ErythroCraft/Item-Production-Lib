package daripher.itemproduction.mixin.farmersdelight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public class CookingPotBlockEntityMixin {
  @SuppressWarnings("DefaultAnnotationParam")
  @ModifyExpressionValue(
      method = "processCooking",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;"
                      + "getResultItem()Lnet/minecraft/world/item/ItemStack;",
              remap = true))
  private ItemStack itemProduced(ItemStack original) {
    @SuppressWarnings("DataFlowIssue")
    CookingPotBlockEntity blockEntity = (CookingPotBlockEntity) (Object) this;
    return ItemProductionLib.itemProduced(original, blockEntity);
  }
}
