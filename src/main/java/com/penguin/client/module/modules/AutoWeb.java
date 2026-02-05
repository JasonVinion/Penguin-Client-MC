package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.InventoryUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AutoWeb extends Module {
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", true);
    private BooleanSetting animals = new BooleanSetting("Animals", false);
    private BooleanSetting predict = new BooleanSetting("Predict", true);
    private NumberSetting predictFactor = new NumberSetting("Predict Factor", 2.0, 0.0, 5.0, 0.5);

    public AutoWeb() {
        super("AutoWeb", "Places webs at feet of nearby entities. Configure target types in settings.", Category.COMBAT);
        addSetting(range);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(predict);
        addSetting(predictFactor);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int webSlot = InventoryUtils.getSlotHotbar(Items.COBWEB);
        if (webSlot == -1) return;

        List<Entity> targets = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(e -> e != mc.player)
                .filter(e -> e instanceof LivingEntity)
                .filter(e -> mc.player.distanceTo(e) <= range.getValue())
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());

        if (targets.isEmpty()) return;
        Entity target = targets.get(0);

        BlockPos targetPos = target.getBlockPos();
        if (predict.isEnabled()) {
            Vec3d vel = target.getVelocity();
            targetPos = BlockPos.ofFloored(
                    target.getX() + vel.x * predictFactor.getValue(),
                    target.getY(),
                    target.getZ() + vel.z * predictFactor.getValue()
            );
        }

        if (mc.world.getBlockState(targetPos).isReplaceable() || mc.world.isAir(targetPos)) {
            // Place web
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = webSlot;

            BlockHitResult hit = new BlockHitResult(targetPos.toCenterPos(), Direction.DOWN, targetPos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }
    
    private boolean isValidTarget(Entity e) {
        if (e instanceof PlayerEntity && players.isEnabled()) return true;
        if (e instanceof Monster && mobs.isEnabled()) return true;
        if (e instanceof AnimalEntity && animals.isEnabled()) return true;
        return false;
    }
}
