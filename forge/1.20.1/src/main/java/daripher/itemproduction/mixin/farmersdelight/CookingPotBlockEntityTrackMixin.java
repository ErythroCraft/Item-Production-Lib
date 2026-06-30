package daripher.itemproduction.mixin.farmersdelight;

import daripher.itemproduction.block.entity.Interactive;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Importiert die originale Farmer's Delight Klasse
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public class CookingPotBlockEntityTrackMixin implements Interactive {

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

    /**
     * KORREKTUR: remap = true gesetzt, da saveAdditional eine originale Minecraft-Methode ist.
     * Sichert die Spieler-Daten dauerhaft im NBT-Tag des Blocks.
     */
    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void onSave(CompoundTag tag, CallbackInfo ci) {
        this.saveUserNbt(tag);
    }

    /**
     * KORREKTUR: remap = true gesetzt, da load eine originale Minecraft-Methode ist.
     * Lädt die Spieler-Daten nach einem Server-Neustart oder Chunk-Wechsel fehlerfrei ein.
     */
    @Inject(method = "load", at = @At("TAIL"), remap = true)
    private void onLoad(CompoundTag tag, CallbackInfo ci) {
        this.loadUserNbt(tag, (net.minecraft.world.level.block.entity.BlockEntity) (Object) this);
    }
}
