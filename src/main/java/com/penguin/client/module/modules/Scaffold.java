package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {
    private ModeSetting mode = new ModeSetting("Mode", "Fast", "Legit", "Fast");
    private BooleanSetting inventory = new BooleanSetting("Inventory", true);
    private BooleanSetting tower = new BooleanSetting("Tower", false);
    private BooleanSetting keepY = new BooleanSetting("Keep Y", false);
    private NumberSetting towerSpeed = new NumberSetting("Tower Speed", 0.42, 0.1, 1.0, 0.05);

    private int startY = -1;
    private int towerTicks = 0;

    public Scaffold() {
        super("Scaffold", "Automatically places blocks beneath you. Tower mode lets you build upward rapidly while holding jump.", Category.WORLD);
        addSetting(mode);
        addSetting(inventory);
        addSetting(tower);
        addSetting(towerSpeed);
        addSetting(keepY);
    }

    @Override
    public void onEnable() {
        if (MinecraftClient.getInstance().player != null) {
            startY = MinecraftClient.getInstance().player.getBlockPos().getY() - 1;
        }
        towerTicks = 0;
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        BlockPos below = mc.player.getBlockPos().down();
        
        // Keep Y functionality - maintains placement at starting Y level
        if (keepY.isEnabled() && startY != -1) {
            int currentY = mc.player.getBlockPos().getY() - 1;
            if (currentY <= startY) {
                // Player is at or below start Y
                below = new BlockPos(below.getX(), startY, below.getZ());
            }
        } else {
            // Update startY if keepY is disabled (so re-enabling it works relative to new pos)
            if (mc.player.isOnGround()) {
                startY = mc.player.getBlockPos().getY() - 1;
            }
        }

        // Tower functionality - rapid upward building while holding jump
        if (tower.isEnabled() && mc.options.jumpKey.isPressed()) {
            towerTicks++;
            
            if (mc.player.isOnGround()) {
                // Apply upward velocity for towering
                mc.player.setVelocity(mc.player.getVelocity().x, towerSpeed.getValue(), mc.player.getVelocity().z);
            } else if (towerTicks % 4 == 0) {
                // Continue applying velocity while in air to maintain tower speed
                Vec3d vel = mc.player.getVelocity();
                if (vel.y < towerSpeed.getValue() * 0.5) {
                    mc.player.setVelocity(vel.x, towerSpeed.getValue() * 0.7, vel.z);
                }
            }
        } else {
            towerTicks = 0;
        }

        attemptPlace(below);

        // Prediction for sprinting/strafing
        Vec3d velocity = mc.player.getVelocity();
        if (Math.abs(velocity.x) > 0.05 || Math.abs(velocity.z) > 0.05) {
            // Predict movement
            for (int i = 1; i <= 3; i++) {
                Vec3d futurePos = mc.player.getPos().add(velocity.multiply(i));
                BlockPos futureBelow = new BlockPos((int) Math.floor(futurePos.x), below.getY(), (int) Math.floor(futurePos.z));
                if (!futureBelow.equals(below)) {
                    attemptPlace(futureBelow);
                }
            }
        }
    }

    private void attemptPlace(BlockPos pos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Legit mode - add slight head rotation toward placement
        if (mode.getMode().equals("Legit") && mc.world.getBlockState(pos).isReplaceable()) {
            // Subtle pitch adjustment when placing
            float targetPitch = 80f;
            float currentPitch = mc.player.getPitch();
            if (currentPitch < targetPitch) {
                mc.player.setPitch(Math.min(currentPitch + 5f, targetPitch));
            }
        }

        if (mc.world.getBlockState(pos).isReplaceable()) {
            int slot = -1;
            for (int i = 0; i < 9; i++) {
                 ItemStack stack = mc.player.getInventory().getStack(i);
                 if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                     slot = i;
                     break;
                 }
            }
            if (slot == -1 && inventory.isEnabled() && mc.player.currentScreenHandler != null && mc.currentScreen == null) {
                // Search main inventory for blocks and swap to hotbar
                for (int i = 9; i < 36; i++) {
                    ItemStack stack = mc.player.getInventory().getStack(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                        // Find empty hotbar slot or use slot 8
                        int targetSlot = 8;
                        for (int j = 0; j < 9; j++) {
                            if (mc.player.getInventory().getStack(j).isEmpty()) {
                                targetSlot = j;
                                break;
                            }
                        }
                        // Quick swap using number key
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, targetSlot, 
                            net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                        slot = targetSlot;
                        break;
                    }
                }
            }

            int prev = mc.player.getInventory().selectedSlot;
            if (slot != -1 && slot != prev) {
                 mc.player.getInventory().selectedSlot = slot;
            }

            if (mc.player.getMainHandStack().getItem() instanceof BlockItem) {
                 for (Direction dir : Direction.values()) {
                     BlockPos neighbor = pos.offset(dir);
                     if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                         Direction opposite = dir.getOpposite();

                         Vec3d hitPos = new Vec3d(neighbor.getX() + 0.5 + opposite.getOffsetX() * 0.5,
                                                  neighbor.getY() + 0.5 + opposite.getOffsetY() * 0.5,
                                                  neighbor.getZ() + 0.5 + opposite.getOffsetZ() * 0.5);

                         mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(
                             hitPos, opposite, neighbor, false
                         ));
                         mc.player.swingHand(Hand.MAIN_HAND);
                         break;
                     }
                 }
            }

            if (slot != -1 && slot != prev) {
                 mc.player.getInventory().selectedSlot = prev;
            }
        }
    }
    
    @Override
    public void onDisable() {
        startY = -1;
        towerTicks = 0;
    }
}
