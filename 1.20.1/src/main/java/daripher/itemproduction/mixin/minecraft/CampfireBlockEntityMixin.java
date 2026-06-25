package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    private CampfireBlockEntityMixin() {
        throw new IllegalStateException("Mixin class cannot be instantiated");
    }

    @Redirect(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void modifyCampfireOutput(Level level, double x, double y, double z, ItemStack stack) {

        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level
                .getBlockEntity(new net.minecraft.core.BlockPos((int) x, (int) y, (int) z));

        ItemStack modifiedStack = ItemProductionLib.itemProduced(stack.copy(), blockEntity);

        Containers.dropItemStack(level, x, y, z, modifiedStack);
    }
}
