package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Completely reworked FeedAura with better targeting, multiple modes, and smart animal selection.
 */
public class FeedAura extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Closest", "Closest", "Looking At", "Priority");
    private NumberSetting range = new NumberSetting("Range", 4.5, 2.0, 6.0, 0.1);
    private NumberSetting delay = new NumberSetting("Delay", 3.0, 0.0, 20.0, 1.0);
    private BooleanSetting rotate = new BooleanSetting("Rotate", true);
    private BooleanSetting multiTarget = new BooleanSetting("Multi-Target", false);
    private NumberSetting targetsPerTick = new NumberSetting("Targets/Tick", 2.0, 1.0, 5.0, 1.0);
    
    private int tickDelay = 0;

    public FeedAura() {
        super("FeedAura", "Completely reworked: Feeds nearby animals with smart targeting and multiple modes.", Category.PLAYER);
        addSetting(mode);
        addSetting(range);
        addSetting(delay);
        addSetting(rotate);
        addSetting(multiTarget);
        addSetting(targetsPerTick);
    }

    @Override
    public void onTick() {
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ItemStack mainHand = mc.player.getMainHandStack();
        ItemStack offHand = mc.player.getOffHandStack();
        
        // Check if we have any breeding item
        if (mainHand.isEmpty() && offHand.isEmpty()) return;

        List<AnimalEntity> validTargets = findValidTargets(mc, mainHand, offHand);
        if (validTargets.isEmpty()) return;

        int targetCount = multiTarget.isEnabled() ? (int) targetsPerTick.getValue() : 1;
        int fed = 0;

        for (AnimalEntity animal : validTargets) {
            if (fed >= targetCount) break;
            
            Hand handToUse = null;
            if (animal.isBreedingItem(mainHand)) {
                handToUse = Hand.MAIN_HAND;
            } else if (animal.isBreedingItem(offHand)) {
                handToUse = Hand.OFF_HAND;
            }
            
            if (handToUse == null) continue;

            // Rotate towards animal if enabled
            if (rotate.isEnabled()) {
                Vec3d targetPos = animal.getEyePos();
                Vec3d playerPos = mc.player.getEyePos();
                Vec3d diff = targetPos.subtract(playerPos);
                
                double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
                float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0f;
                float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, distXZ));
                
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
            }

            // Interact with the animal
            mc.interactionManager.interactEntity(mc.player, animal, handToUse);
            mc.player.swingHand(handToUse);
            fed++;
        }

        if (fed > 0) {
            tickDelay = (int) delay.getValue();
        }
    }

    private List<AnimalEntity> findValidTargets(MinecraftClient mc, ItemStack mainHand, ItemStack offHand) {
        List<AnimalEntity> targets = new ArrayList<>();
        double rangeSq = range.getValue() * range.getValue();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof AnimalEntity animal)) continue;
            if (mc.player.squaredDistanceTo(animal) > rangeSq) continue;
            if (animal.getBreedingAge() != 0 || animal.isInLove()) continue;
            if (!animal.isBreedingItem(mainHand) && !animal.isBreedingItem(offHand)) continue;
            
            // Line of sight check - simple version
            if (canSeeSimple(mc, animal)) {
                targets.add(animal);
            }
        }

        // Sort based on mode
        switch (mode.getMode()) {
            case "Closest":
                targets.sort(Comparator.comparingDouble(a -> mc.player.squaredDistanceTo(a)));
                break;
            case "Looking At":
                targets.sort(Comparator.comparingDouble(this::getAngleToEntity));
                break;
            case "Priority":
                // Prioritize animals that are already in groups for breeding
                targets.sort(Comparator.comparingInt(this::getAnimalPriority).reversed()
                        .thenComparingDouble(a -> mc.player.squaredDistanceTo(a)));
                break;
        }

        return targets;
    }

    private boolean canSeeSimple(MinecraftClient mc, AnimalEntity animal) {
        Vec3d start = mc.player.getEyePos();
        Vec3d end = animal.getEyePos();
        HitResult hit = mc.world.raycast(new RaycastContext(
            start, end,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));
        return hit.getType() == HitResult.Type.MISS;
    }

    private double getAngleToEntity(AnimalEntity animal) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d playerLook = mc.player.getRotationVec(1.0f);
        Vec3d toAnimal = animal.getPos().subtract(mc.player.getPos()).normalize();
        return Math.acos(playerLook.dotProduct(toAnimal));
    }

    private int getAnimalPriority(AnimalEntity animal) {
        // Higher priority for animals near other breedable animals of the same type
        int nearby = 0;
        for (Entity e : MinecraftClient.getInstance().world.getEntities()) {
            if (e instanceof AnimalEntity other && other != animal) {
                if (other.getClass() == animal.getClass() && animal.squaredDistanceTo(other) < 16) {
                    if (other.getBreedingAge() == 0 && !other.isInLove()) {
                        nearby++;
                    }
                }
            }
        }
        return nearby;
    }
}
