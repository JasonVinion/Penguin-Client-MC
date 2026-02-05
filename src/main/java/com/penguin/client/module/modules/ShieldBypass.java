package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * ShieldBypass module - Attempts to teleport you behind enemies to bypass shields.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class ShieldBypass extends Module {
    public static ShieldBypass INSTANCE;

    public BooleanSetting ignoreAxe = new BooleanSetting("Ignore Axe", true);
    public NumberSetting teleportDistance = new NumberSetting("TP Distance", 2.0, 1.0, 4.0, 0.1);

    public ShieldBypass() {
        super("ShieldBypass", "Attempts to teleport you behind enemies to bypass shields. Will send movement packets to appear behind blocking targets.", Category.COMBAT);
        addSetting(ignoreAxe);
        addSetting(teleportDistance);
        INSTANCE = this;
    }

    /**
     * Check if an attack from pos would be blocked by the target's shield.
     * Returns true if shield would block the attack (attack from front).
     * Returns false if attack would hit (from behind the shield).
     */
    private boolean wouldBeBlocked(Vec3d attackerPos, LivingEntity target) {
        Vec3d targetPos = target.getPos();
        // Direction from target to attacker
        Vec3d attackDirection = attackerPos.subtract(targetPos).normalize();
        // Where the target is looking (where the shield faces)
        Vec3d lookVec = target.getRotationVec(1.0f);
        
        // Dot product: positive means attack is from the front (blocked by shield)
        // negative means attack is from behind (not blocked)
        double dot = new Vec3d(attackDirection.x, 0.0, attackDirection.z).dotProduct(
            new Vec3d(lookVec.x, 0.0, lookVec.z));
        return dot > 0.0; // Blocked if attack comes from the front
    }

    /**
     * Attempt to bypass shield when attacking a blocking target
     * Returns true if bypass was performed
     */
    public boolean tryBypass(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null || mc.world == null) return false;
        if (!(target instanceof LivingEntity living)) return false;
        if (!living.isBlocking()) return false;

        // Check if we should ignore when holding an axe (axes disable shields anyway)
        if (ignoreAxe.isEnabled()) {
            ItemStack mainHand = mc.player.getMainHandStack();
            if (mainHand.getItem() instanceof AxeItem) return false;
        }

        // Check if we're currently blocked by the shield
        Vec3d playerPos = mc.player.getPos();
        if (!wouldBeBlocked(playerPos, living)) {
            // Not blocked, no need to bypass
            return false;
        }

        // Calculate position behind the target (use target's yaw, not player's)
        // Move to a position behind where the target is looking
        float targetYaw = living.getYaw();
        Vec3d offset = Vec3d.fromPolar(0, targetYaw + 180).normalize().multiply(teleportDistance.getValue());
        Vec3d targetPos = living.getPos();
        Vec3d newPos = targetPos.add(offset);

        // Try to find a valid position (move up to avoid being inside blocks)
        for (float i = 0; i < 4; i += 0.25f) {
            Vec3d testPos = newPos.add(0, i, 0);
            boolean collides = !mc.world.isSpaceEmpty(living.getBoundingBox().offset(offset).offset(0, i, 0));
            
            if (!collides) {
                newPos = testPos;
                break;
            }
        }

        // Verify we would NOT be blocked from the new position
        if (wouldBeBlocked(newPos, living)) {
            // Would still be blocked, don't attempt bypass
            return false;
        }

        // Send position packet to move behind target
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true)
        );

        // Send attack
        mc.player.networkHandler.sendPacket(
            PlayerInteractEntityC2SPacket.attack(living, mc.player.isSneaking())
        );
        mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        mc.player.resetLastAttackedTicks();

        // Send position packet to move back
        mc.player.networkHandler.sendPacket(
            new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true)
        );

        return true;
    }
}
