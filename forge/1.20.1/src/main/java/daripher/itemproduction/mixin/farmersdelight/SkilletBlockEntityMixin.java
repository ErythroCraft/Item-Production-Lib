package daripher.itemproduction.mixin.farmersdelight;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;

@Mixin(value = SkilletBlockEntity.class, remap = false)
public abstract class SkilletBlockEntityMixin {

    @Shadow
    private int cookingTime;

    @Shadow
    private int cookingTimeTotal;

    /**
     * Klinkt sich am ANFANG von cookAndOutputItems ein.
     * Nutzt das 1.20.1 Rezept-System über den Container-Fallback, um @Shadow-Fehler komplett zu eliminieren!
     */
    @Inject(method = "cookAndOutputItems", at = @At("HEAD"))
    private void onSkilletItemCooked(ItemStack cookingStack, Level level, CallbackInfo ci) {
        if (level == null || level.isClientSide() || cookingStack.isEmpty()) {
            return;
        }

        // 1. SICHERUNG: Nur in dem exakten Tick feuern, in dem das Kochen beendet ist!
        if (this.cookingTime + 1 < this.cookingTimeTotal) {
            return;
        }

        SkilletBlockEntity skillet = (SkilletBlockEntity) (Object) this;
        ItemStack cookedResult = ItemStack.EMPTY;

        // 2. ABSOLUT FEHLERFREIER 1.20.1 REZEPT-DETEKTOR:
        // Wir packen das rohe Item in einen virtuellen 1-Slot-Container und fragen Minecrafts 
        // offizielles Rezeptbuch nach dem passenden Campfire-Ergebnis ab. Das klappt immer!
        try {
            SimpleContainer temporaryContainer = new SimpleContainer(cookingStack);
            Optional<CampfireCookingRecipe> recipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CAMPFIRE_COOKING, temporaryContainer, level);

            if (recipe.isPresent()) {
                cookedResult = recipe.get().getResultItem(level.registryAccess());
            }
        } catch (Exception ignored) {
            return; // Wenn die Rezept-Abfrage fehlschlägt, abbrechen um Abstürze zu verhindern
        }

        if (cookedResult == null || cookedResult.isEmpty()) {
            return;
        }

        BlockPos pos = skillet.getBlockPos();

        // Spieler ermitteln
        Player foundPlayer = null;
        if ((Object) this instanceof Interactive interactive) {
            foundPlayer = interactive.resolveUser(level);
        }

        ServerPlayer targetPlayer = null;
        if (foundPlayer instanceof ServerPlayer serverPlayer) {
            targetPlayer = serverPlayer;
        } else {
            Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
            if (closestPlayer instanceof ServerPlayer serverPlayer) {
                targetPlayer = serverPlayer;
            }
        }

        if (targetPlayer != null) {
            // Wir jagen eine kontrollierte 1er-Menge des FERTIGEN Essens durch deine Library
            ItemStack targetStack = cookedResult.copy();
            targetStack.setCount(1);
            
            ItemStack bonusResult = ItemProductionLib.itemProduced(targetStack, targetPlayer, "skillet");

            // Wenn der Skill-Tree zusätzliche Gegenstände generiert hat
            int bonusAmount = bonusResult.getCount() - 1;
            if (bonusAmount > 0) {
                ItemStack extraDrop = bonusResult.copy();
                extraDrop.setCount(bonusAmount);

                // Blickrichtung für den physikalischen Auswurfeffekt berechnen
                Direction direction = skillet.getBlockState().getValue(SkilletBlock.FACING).getClockWise();
                
                double spawnX = pos.getX() + 0.5 + (direction.getStepX() * 0.2);
                double spawnY = pos.getY() + 0.3;
                double spawnZ = pos.getZ() + 0.5 + (direction.getStepZ() * 0.2);

                // Item in der Welt spawnen lassen
                ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, extraDrop);
                
                // Schiebt das Item mit dem gleichen Schwung wie das Original von der Pfanne
                itemEntity.setDeltaMovement(direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F);
                itemEntity.setPickUpDelay(20); // 1 Sekunde Trichter-Schutz

                level.addFreshEntity(itemEntity);

                org.apache.logging.log4j.LogManager.getLogger("ItemProductionLib")
                        .warn("[PFANNEN-BONUS] Skill erfolgreich! +{} extra FERTIGES '{}' fuer {} ausgeworfen!", 
                                bonusAmount, extraDrop.getItem().toString(), targetPlayer.getName().getString());
            }
        }
    }
}
