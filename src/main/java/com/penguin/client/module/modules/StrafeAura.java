package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class StrafeAura extends Module {
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private NumberSetting speed = new NumberSetting("Speed", 0.3, 0.1, 1.0, 0.1);
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private BooleanSetting animals = new BooleanSetting("Animals", false);

    private int hitDelay = 0;

    public StrafeAura() {
        super("StrafeAura", "KillAura that circles around targets while attacking.", Category.COMBAT);
        addSetting(range);
        addSetting(speed);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Find Target
        List<Entity> targets = mc.world.getEntitiesByClass(Entity.class, mc.player.getBoundingBox().expand(range.getValue() * 2), e -> {
            if (e == mc.player) return false;
            if (e instanceof LivingEntity && ((LivingEntity)e).isDead()) return false;

            if (e instanceof PlayerEntity && players.isEnabled()) return true;
            if (e instanceof Monster && mobs.isEnabled()) return true;
            if (e instanceof AnimalEntity && animals.isEnabled()) return true;
            return false;
        });

        if (targets.isEmpty()) return;
        Entity target = null;
        double minDistance = Double.MAX_VALUE;
        for (Entity e : targets) {
            double d = e.distanceTo(mc.player);
            if (d < minDistance) {
                minDistance = d;
                target = e;
            }
        }

        if (target == null || target.distanceTo(mc.player) > range.getValue() * 2) return;

        // Attack Logic
        if (hitDelay > 0) hitDelay--;
        if (target.distanceTo(mc.player) <= range.getValue()) {
            if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.resetLastAttackedTicks();
            }
        }

        // Movement Logic (Strafe/Get Behind)
        // Target's behind position:
        double rad = Math.toRadians(target.getYaw());
        double behindX = target.getX() - Math.sin(-rad) * 2.0; // 2 blocks behind
        double behindZ = target.getZ() - Math.cos(-rad) * 2.0;

        // Move towards behindX, behindZ
        double dx = behindX - mc.player.getX();
        double dz = behindZ - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist > 0.1) {
            double s = speed.getValue();

            // Normalize and apply speed
            double mx = (dx / dist) * s;
            double mz = (dz / dist) * s;

            mc.player.setVelocity(mx, mc.player.getVelocity().y, mz);

            // Look at target
            double lookX = target.getX() - mc.player.getX();
            double lookZ = target.getZ() - mc.player.getZ();
            float yaw = (float) (Math.atan2(lookZ, lookX) * 180.0 / Math.PI) - 90.0f;
            mc.player.setYaw(yaw);
        }
    }
}
