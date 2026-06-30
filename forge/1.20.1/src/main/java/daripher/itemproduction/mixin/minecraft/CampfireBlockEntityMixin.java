package daripher.itemproduction.mixin.minecraft;

import daripher.itemproduction.ItemProductionLib;
import daripher.itemproduction.block.entity.Interactive;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin {

    /**
     * Klinkt sich am ANFANG (HEAD) von cookTick ein.
     * Hier sind die fertigen Items noch physikalisch im Slot vorhanden, 
     * bevor Minecraft sie im selben Tick löscht und auswirft!
     */
    @Inject(method = "cookTick", at = @At("HEAD"), remap = true)
    private static void onCampfireCookingFinished(Level level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, CallbackInfo ci) {
        if (level == null || level.isClientSide() || blockEntity == null) {
            return;
        }

        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) blockEntity;
        int[] cookingProgress = accessor.itemproductionGetCookingProgress();
        int[] cookingTime = accessor.itemproductionGetCookingTime();
        NonNullList<ItemStack> items = blockEntity.getItems();

        if (cookingProgress == null || cookingTime == null || items == null) {
            return;
        }

        // Das Lagerfeuer besitzt bis zu 4 Slots parallel
        for (int i = 0; i < Math.min(cookingProgress.length, items.size()); i++) {
            if (i >= cookingTime.length) continue;

            // KORREKTUR: Fängt den exakten Moment des Fertigwerdens im Server-Tick ab
            if (cookingProgress[i] >= cookingTime[i] - 1 && cookingTime[i] > 0) {
                ItemStack rawInput = items.get(i);

                if (rawInput != null && !rawInput.isEmpty()) {
                    // Spieler über das globale NBT-Gedächtnis ermitteln
                    Player foundPlayer = null;
                    if (blockEntity instanceof Interactive interactive) {
                        foundPlayer = interactive.resolveUser(level);
                    }

                    ServerPlayer targetPlayer = null;
                    if (foundPlayer instanceof ServerPlayer serverPlayer) {
                        targetPlayer = serverPlayer;
                    } else {
                        // Fallback Umkreis
                        Player closestPlayer = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8.0, false);
                        if (closestPlayer instanceof ServerPlayer serverPlayer) {
                            targetPlayer = serverPlayer;
                        }
                    }

                    if (targetPlayer != null) {
                        // Wir ermitteln das fertige Bratergebnis über den 1.20.1 RecipeManager
                        ItemStack cookedResult = ItemStack.EMPTY;
                        try {
                            SimpleContainer temporaryContainer = new SimpleContainer(rawInput);
                            Optional<CampfireCookingRecipe> recipe = level.getRecipeManager()
                                    .getRecipeFor(RecipeType.CAMPFIRE_COOKING, temporaryContainer, level);

                            if (recipe.isPresent()) {
                                cookedResult = recipe.get().getResultItem(level.registryAccess());
                            }
                        } catch (Exception ignored) {
                            continue;
                        }

                        if (cookedResult == null || cookedResult.isEmpty()) {
                            continue;
                        }

                        // Jedes der 4 Items wird separat als Menge 1 durch die Library geschickt!
                        // Dadurch wird die Prozentchance für jedes Fleischstück völlig unabhängig berechnet.
                        ItemStack targetStack = cookedResult.copy();
                        targetStack.setCount(1);

                        ItemStack bonusResult = ItemProductionLib.itemProduced(targetStack, targetPlayer, "campfire");

                        // Wenn dein Skill-Tree für dieses spezifische Item zugeschlagen hat
                        int bonusAmount = bonusResult.getCount() - 1;
                        if (bonusAmount > 0) {
                            ItemStack extraDrop = bonusResult.copy();
                            extraDrop.setCount(bonusAmount);

                            // Spawnt das zusätzliche Fleisch physisch als extra Drop über dem Lagerfeuer
                            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extraDrop);

                            org.apache.logging.log4j.LogManager.getLogger("ItemProductionLib")
                                    .warn("[LAGERFEUER-BONUS] Slot {} erfolgreich! +{} extra '{}' fuer {} generiert!", 
                                            i, bonusAmount, extraDrop.getItem().toString(), targetPlayer.getName().getString());
                        }
                    }
                }
            }
        }
    }
}