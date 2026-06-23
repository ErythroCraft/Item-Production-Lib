package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.block.entity.Interactive;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements Interactive {
  private @Nullable Player player;

  @Override
  public @Nullable Player getUser() {
    return player;
  }

  @Override
  public void setUser(@Nullable Player player) {
    this.player = player;
  }
}
