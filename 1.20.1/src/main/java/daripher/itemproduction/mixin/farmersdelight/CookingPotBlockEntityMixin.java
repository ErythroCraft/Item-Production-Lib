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
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity", remap = false)
public class CookingPotBlockEntityMixin {

    /**
     * Klinkt sich in den Koch-Tick des Kochtopfs ein.
     * Nutzt den globalen Schutz-Tag, um den Spam im Slot sofort zu stoppen.
     * Da alle Items diesen Tag erhalten, bleiben sie untereinander perfekt
     * stapelbar!
     */
    @Inject(method = "cookingTick", at = @At("TAIL"))
    private static void modifyCookingPotOutputViaForge(Level level, BlockPos pos, BlockState state,
            @Coerce BlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((IItemHandler handler) -> {
            ItemStack stack = handler.getStackInSlot(8); // Slot 8 = Result

            // Wenn eine fertige Suppe im Slot liegt und noch nicht verarbeitet wurde:
            if (!stack.isEmpty() && !stack.getOrCreateTag().contains("SkillTreeProcessed")) {

                // 1. Globalen Schutzstempel aufsetzen, damit der Ofen-Tick sofort aufhört zu
                // spammen
                stack.getOrCreateTag().putBoolean("SkillTreeProcessed", true);

                // 2. Durch deine Library jagen (Das Log-Signal kommt jetzt garantiert 1x)
                ItemStack modified = ItemProductionLib.itemProduced(stack.copy(), blockEntity);
                stack.setTag(modified.getTag());
                stack.setCount(modified.getCount());
            }
        });
    }
}
