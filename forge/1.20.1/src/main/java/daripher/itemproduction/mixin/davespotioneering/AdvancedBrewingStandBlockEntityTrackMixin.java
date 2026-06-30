package daripher.itemproduction.mixin.davespotioneering;

import daripher.itemproduction.block.entity.Interactive;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Importiere die Klasse direkt aus Dave's Potioneering
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;

@Mixin(value = AdvancedBrewingStandBlockEntity.class, remap = false)
public class AdvancedBrewingStandBlockEntityTrackMixin implements Interactive {

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
     * Bleibt bei remap = false, da 'startOpen' eine eigene Methode von Dave's Potioneering ist.
     */
    @Inject(method = "startOpen", at = @At("HEAD"), remap = false)
    private void onStartOpen(Player player, CallbackInfo ci) {
        if (player != null && !player.level().isClientSide()) {
            this.itemproductionUser = player;
            this.itemproductionUserUuid = player.getUUID();
        }
    }

    /**
     * KORREKTUR 1: remap = true gesetzt! 'stillValid' kommt aus dem Minecraft-Container-System
     * und wird im echten Build verschleiert.
     */
    @Inject(method = "stillValid", at = @At("HEAD"), remap = true)
    private void onStillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && !player.level().isClientSide()) {
            this.itemproductionUser = player;
            this.itemproductionUserUuid = player.getUUID();
        }
    }

    /**
     * KORREKTUR 2: remap = true gesetzt, da 'saveAdditional' eine echte Minecraft-Methode ist!
     */
    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void onSave(CompoundTag tag, CallbackInfo ci) {
        this.saveUserNbt(tag);
    }

    /**
     * KORREKTUR 3: remap = true gesetzt, da 'load' eine echte Minecraft-Methode ist!
     */
    @Inject(method = "load", at = @At("TAIL"), remap = true)
    private void onLoad(CompoundTag tag, CallbackInfo ci) {
        this.loadUserNbt(tag, (net.minecraft.world.level.block.entity.BlockEntity) (Object) this);
    }
}
