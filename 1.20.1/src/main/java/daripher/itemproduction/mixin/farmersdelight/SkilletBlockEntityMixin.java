package daripher.itemproduction.mixin.farmersdelight;

import daripher.itemproduction.ItemProductionLib;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity", remap = false)
public class SkilletBlockEntityMixin {

    @Inject(method = "cookingTick", at = @At("TAIL"))
    private static void modifySkilletOutputViaForge(Level level, BlockPos pos, BlockState state,
            @Coerce BlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((IItemHandler handler) -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);

                if (!stack.isEmpty()) {
                    // Wir modifizieren das Item – das Spawn-Event in der Hauptklasse erledigt den
                    // Rest fehlerfrei
                    ItemStack modified = ItemProductionLib.itemProduced(stack.copy(), blockEntity);
                    stack.setTag(modified.getTag());
                    stack.setCount(modified.getCount());
                }
            }
        });
    }
}
