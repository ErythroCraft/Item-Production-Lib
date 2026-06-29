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

// 1. Importiere die Klasse direkt von Farmer's Delight
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

// 2. Nutze "value" statt "targets" (remap bleibt auf false)
@Mixin(value = SkilletBlockEntity.class, remap = false)
public class SkilletBlockEntityMixin {

    // 2. remap = false stellt sicher, dass der Compiler nicht nach MCP/SRG-Mappings sucht
    @Inject(method = "cookingTick", at = @At("TAIL"), remap = false)
    private static void modifySkilletOutputViaForge(Level level, BlockPos pos, BlockState state,
                                                    @Coerce BlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((IItemHandler handler) -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);

                if (!stack.isEmpty()) {
                    ItemStack modified = ItemProductionLib.itemProduced(stack.copy(), blockEntity);
                    stack.setTag(modified.getTag());
                    stack.setCount(modified.getCount());
                }
            }
        });
    }
}
