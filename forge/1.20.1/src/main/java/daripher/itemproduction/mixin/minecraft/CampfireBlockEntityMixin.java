package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    /**
     * Klinkt sich in den cookTick ein.
     * Modifiziert KEINE Items und erzeugt KEINE NBT-Tags!
     * Sorgt nur dafür, dass das Lagerfeuer seinen angemeldeten Benutzer niemals
     * verliert.
     */
    @Inject(method = "cookTick", at = @At("HEAD"))
    private static void ensureCampfireUserSafety(Level level, BlockPos pos, BlockState state,
            CampfireBlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        // Falls der User durch einen Chunk-Reload oder Server-Zustand kurzfristig null
        // ist,
        // holen wir uns den am dichtesten stehenden Spieler als Sicherheits-Fallback
        if (blockEntity instanceof Interactive interactive && interactive.getUser() == null) {
            Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0,
                    p -> p instanceof ServerPlayer);
            if (closestPlayer != null) {
                interactive.setUser(closestPlayer);
            }
        }
    }
}
