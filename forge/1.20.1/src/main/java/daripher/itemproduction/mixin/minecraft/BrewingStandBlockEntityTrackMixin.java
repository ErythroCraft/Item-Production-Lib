package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.block.entity.Interactive;

import java.util.UUID;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityTrackMixin implements Interactive {

    @Unique
    private Player itemproductionUser = null;

    @Unique
    private UUID itemproductionUserUuid = null;

    // --- INTERFACE IMPLEMENTIERUNG (Getter & Setter) ---
    @Override
    public Player getUser() {
        return this.itemproductionUser;
    }

    @Override
    public void setUser(Player player) {
        this.itemproductionUser = player;
    }

    @Override
    public UUID getUserUUID() {
        return this.itemproductionUserUuid;
    }

    @Override
    public void setUserUUID(UUID uuid) {
        this.itemproductionUserUuid = uuid;
    }

    // KORREKTUR: Alle @Inject-Methoden (serverTick, saveAdditional, load) wurden entfernt!
    // Das Speichern und Laden übernimmt ab jetzt deine globale BlockEntityMixin.
}
