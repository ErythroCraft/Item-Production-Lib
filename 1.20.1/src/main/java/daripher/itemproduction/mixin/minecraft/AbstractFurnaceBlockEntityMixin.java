package daripher.itemproduction.mixin.minecraft;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import daripher.itemproduction.ItemProductionLib;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
  @Inject(
      method = "burn",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;",
              shift = At.Shift.BEFORE,
              ordinal = 1))
  private void itemProduced(
      RegistryAccess registryAccess,
      @Nullable Recipe<?> recipe,
      NonNullList<ItemStack> inventory,
      int maxStackSize,
      CallbackInfoReturnable<Boolean> callbackInfo,
      @Local(ordinal = 1) LocalRef<ItemStack> stack) {
    @SuppressWarnings("DataFlowIssue")
    AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) (Object) this;
    stack.set(ItemProductionLib.itemProduced(stack.get(), blockEntity));
  }
}
