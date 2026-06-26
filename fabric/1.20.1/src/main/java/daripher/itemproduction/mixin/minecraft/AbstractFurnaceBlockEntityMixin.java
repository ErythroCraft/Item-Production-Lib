package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

  /**
   * Injiziert sich in den Server-Tick des Ofens, um die Geschwindigkeit dynamisch
   * zu berechnen.
   * Der ungenutzte geschützte Konstruktor wurde entfernt.
   */
  @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I", ordinal = 1))
  private static void accelerateFurnaceCooking(Level level, BlockPos pos, BlockState state,
      AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
    if (level == null || level.isClientSide()) {
      return;
    }

    if (blockEntity instanceof Interactive interactive) {
      Player user = interactive.getUser();
      if (user instanceof ServerPlayer player) {
        applyFurnaceSpeedBoost(level, blockEntity, player);
      }
    }
  }

  /**
   * Erhöht den Ofen-Fortschritt basierend auf den gelernten
   * Skilltree-Multiplikatoren.
   * KORREKTUR: Nutzt reine Java-Reflection mit SRG- und Mappings-Fallback, um
   * Startabstürze zu bannen!
   */
  private static void applyFurnaceSpeedBoost(Level level, AbstractFurnaceBlockEntity blockEntity, ServerPlayer player) {
    float speedMultiplier = ItemProductionLib.getProductionSpeedMultiplier(player, "furnace");

    if (speedMultiplier > 1.0f) {
      int tickInterval = (int) (1.0f / (speedMultiplier - 1.0f));

      if (tickInterval > 0 && level.getGameTime() % tickInterval == 0) {
        try {
          java.lang.reflect.Field progressField;
          java.lang.reflect.Field totalTimeField;

          try {
            // 1. Versuch: Produktionsumgebung des Spielers (1.20.1 SRG-Namen)
            progressField = AbstractFurnaceBlockEntity.class.getDeclaredField("f_58318_"); // cookingProgress
            totalTimeField = AbstractFurnaceBlockEntity.class.getDeclaredField("f_58320_"); // cookingTotalTime
          } catch (NoSuchFieldException e) {
            // 2. Versuch: Deine Entwicklungsumgebung (IDE Mappings)
            progressField = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingProgress");
            totalTimeField = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTotalTime");
          }

          progressField.setAccessible(true);
          totalTimeField.setAccessible(true);

          int currentProgress = progressField.getInt(blockEntity);
          int totalTime = totalTimeField.getInt(blockEntity);

          if (currentProgress > 0 && currentProgress < totalTime) {
            progressField.setInt(blockEntity, currentProgress + 1);
          }
        } catch (Exception ignored) {
          // NOSONAR: Schützt vor veränderten Ofen-Klassen durch andere Mods
        }
      }
    }
  }
}
