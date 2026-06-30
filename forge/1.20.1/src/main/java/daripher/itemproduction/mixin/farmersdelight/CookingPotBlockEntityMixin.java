package daripher.itemproduction.mixin.farmersdelight;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Klasse direkt importieren
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntityMixin {

    /**
     * Injiziert sich am Ende von processCooking, wenn das Essen im Ausgangs-Slot generiert wurde.
     */
    @Inject(method = "processCooking", at = @At("TAIL"))
    private void onCookingFinished(CallbackInfoReturnable<Boolean> cir) {
        // Nur verarbeiten, wenn das Kochen in diesem Schritt erfolgreich war (Return-Wert ist true)
        if (!cir.getReturnValue()) {
            return;
        }

        net.minecraft.world.level.block.entity.BlockEntity blockEntity = (net.minecraft.world.level.block.entity.BlockEntity) (Object) this;
        Level level = blockEntity.getLevel();

        if (level == null || level.isClientSide()) {
            return;
        }

        // 1. Hol das fertige Essen über die originale Farmer's Delight Methode
        ItemStack finishedFood = this.getMeal();

        if (finishedFood.isEmpty()) {
            return;
        }

        // 2. Greife über das Interactive-Interface sicher auf den Spieler zu
        Player foundPlayer = null;
        if ((Object) this instanceof Interactive interactive) {
            foundPlayer = interactive.resolveUser(level);
        }

        ServerPlayer targetPlayer = null;
        if (foundPlayer instanceof ServerPlayer serverPlayer) {
            targetPlayer = serverPlayer;
        }

        // FALLBACK: Falls der Topf automatisiert befüllt wurde (z.B. Pipes/Hopper), nimm den nächsten Spieler
        if (targetPlayer == null) {
            BlockPos pos = blockEntity.getBlockPos();
            Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
            if (closestPlayer instanceof ServerPlayer serverPlayer) {
                targetPlayer = serverPlayer;
            }
        }

        // 3. Wenn ein gültiger Spieler gefunden wurde, führe die Produktion aus
        if (targetPlayer != null) {
            // KORREKTUR 1: Typ-Angabe "cookingpot" mitsenden, damit deine Configs und Logs greifen
            // KORREKTUR 2: Wir übergeben den ECHTEN Stack. Die Library berechnet die Boni 
            // und erhöht die Anzahl direkt im echten Objekt!
            ItemProductionLib.itemProduced(finishedFood, targetPlayer, "cookingpot");

            // KORREKTUR 3: Wir schreiben das Ergebnis direkt physisch in das Inventar des Kochtopfs zurück.
            // Slot 5 ist bei Farmer's Delight traditionell der Ergebnis-Ausgangs-Slot des Topfes!
            if (blockEntity instanceof net.minecraft.world.Container container) {
                container.setItem(5, finishedFood);
            }

            // KORREKTUR 4: Veralteten logStackLoopEnd-Aufruf entfernt, da er Kompilierfehler verursacht hat
        }
    }

    // Shadow-Verweis auf das originale Farmer's Delight Repository, um das Essen auszulesen
    @Shadow
    public abstract ItemStack getMeal();
}
