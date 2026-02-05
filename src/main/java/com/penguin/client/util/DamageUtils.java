package com.penguin.client.util;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class DamageUtils {

    public static float getExplosionDamage(Vec3d explosionPos, float power, LivingEntity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null) return 0f;

        double maxDist = power * 2.0;
        double dist = target.squaredDistanceTo(explosionPos);
        if (dist > maxDist * maxDist) return 0f;

        dist = Math.sqrt(dist);
        double density = getExposure(explosionPos, target);
        double impact = (1.0 - (dist / maxDist)) * density;
        float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * maxDist + 1.0);

        // Difficulty scaling
        if (world.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL) damage = 0f;
        else if (world.getDifficulty() == net.minecraft.world.Difficulty.EASY) damage = damage * 0.5f;
        else if (world.getDifficulty() == net.minecraft.world.Difficulty.HARD) damage = damage * 1.5f;

        // Armor reduction
        damage = DamageUtil.getDamageLeft(damage, (float)target.getArmor(), (float)target.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));

        // Enchantment reduction
        if (target.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int amplifier = (target.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
            damage = Math.max(damage * (1.0F - amplifier / 25.0F), 0.0F);
        }

        int blastProt = EnchantmentHelper.getProtectionAmount(target.getArmorItems(), mc.world.getDamageSources().explosion(null));
        if (blastProt > 0) {
            damage = DamageUtil.getInflictedDamage(damage, blastProt);
        }

        return damage;
    }

    private static float getExposure(Vec3d source, Entity entity) {
        Box box = entity.getBoundingBox();
        
        // Check for zero-sized dimensions to prevent division by zero
        double boxWidth = box.maxX - box.minX;
        double boxHeight = box.maxY - box.minY;
        double boxDepth = box.maxZ - box.minZ;
        
        if (boxWidth == 0.0 || boxHeight == 0.0 || boxDepth == 0.0) {
            return 0.0F;
        }
        
        double stepX = 1.0 / (boxWidth * 2.0 + 1.0);
        double stepY = 1.0 / (boxHeight * 2.0 + 1.0);
        double stepZ = 1.0 / (boxDepth * 2.0 + 1.0);
        double offsetX = (1.0 - Math.floor(1.0 / stepX) * stepX) / 2.0;
        double offsetY = (1.0 - Math.floor(1.0 / stepY) * stepY) / 2.0;
        double offsetZ = (1.0 - Math.floor(1.0 / stepZ) * stepZ) / 2.0;
        
        if (stepX >= 0.0 && stepY >= 0.0 && stepZ >= 0.0) {
            int unobstructedSamples = 0;
            int totalSamples = 0;
            for (float sampleX = 0.0F; sampleX <= 1.0F; sampleX = (float)((double)sampleX + stepX)) {
                for (float sampleY = 0.0F; sampleY <= 1.0F; sampleY = (float)((double)sampleY + stepY)) {
                    for (float sampleZ = 0.0F; sampleZ <= 1.0F; sampleZ = (float)((double)sampleZ + stepZ)) {
                        double sampledX = MathHelper.lerp((double)sampleX, box.minX, box.maxX);
                        double sampledY = MathHelper.lerp((double)sampleY, box.minY, box.maxY);
                        double sampledZ = MathHelper.lerp((double)sampleZ, box.minZ, box.maxZ);
                        Vec3d vec3d = new Vec3d(sampledX + offsetX, sampledY + offsetY, sampledZ + offsetZ);
                        if (raycast(vec3d, source) == HitResult.Type.MISS) {
                            ++unobstructedSamples;
                        }
                        ++totalSamples;
                    }
                }
            }
            return (float) unobstructedSamples / (float) totalSamples;
        } else {
            return 0.0F;
        }
    }

    private static HitResult.Type raycast(Vec3d start, Vec3d end) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player)).getType();
    }
}
