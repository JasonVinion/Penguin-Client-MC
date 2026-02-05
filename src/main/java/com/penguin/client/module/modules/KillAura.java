package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.ModeSetting;
import com.penguin.client.settings.NumberSetting;
import com.penguin.client.util.RotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;

public class KillAura extends Module {
    private NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private NumberSetting cps = new NumberSetting("CPS", 0.0, 0.0, 20.0, 1.0);
    private BooleanSetting players = new BooleanSetting("Players", true);
    private BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private BooleanSetting animals = new BooleanSetting("Animals", false);
    private ModeSetting weaponPref = new ModeSetting("Preference", "Sword", "Sword", "Axe");
    private ModeSetting rotation = new ModeSetting("Rotation", "None", "None", "Lock", "Silent");
    
    private long lastAttackTime = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities. CPS 0 = use attack cooldown, otherwise uses custom CPS.", Category.COMBAT);
        addSetting(range);
        addSetting(cps);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(weaponPref);
        addSetting(rotation);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Entity target = java.util.stream.StreamSupport.stream(mc.world.getEntities().spliterator(), false)
             .filter(e -> e != mc.player)
             .filter(e -> e instanceof LivingEntity)
             .filter(e -> !(e instanceof ArmorStandEntity))
             .filter(e -> mc.player.distanceTo(e) <= range.getValue())
             .filter(e -> {
                 if (e instanceof PlayerEntity) return players.isEnabled();
                 if (e instanceof Monster) return mobs.isEnabled();
                 if (e instanceof AnimalEntity) return animals.isEnabled();
                 return false;
             })
             .min(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
             .orElse(null);

        if (target != null) {
            // Handle Rotations
            float[] rots = RotationUtils.getRotations(target);
            if (rots != null) {
                if (rotation.getMode().equals("Lock")) {
                    mc.player.setYaw(rots[0]);
                    mc.player.setPitch(rots[1]);
                } else if (rotation.getMode().equals("Silent")) {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rots[0], rots[1], mc.player.isOnGround()));
                }
            }

            // Auto Switch Weapon
            int bestSlot = -1;
            int currentSlot = mc.player.getInventory().selectedSlot;
            ItemStack currentStack = mc.player.getMainHandStack();

            boolean correctWeapon = false;
            if (weaponPref.getMode().equals("Sword") && currentStack.getItem() instanceof SwordItem) correctWeapon = true;
            if (weaponPref.getMode().equals("Axe") && currentStack.getItem() instanceof AxeItem) correctWeapon = true;

            if (!correctWeapon) {
                for (int i = 0; i < 9; i++) {
                    ItemStack s = mc.player.getInventory().getStack(i);
                    if (weaponPref.getMode().equals("Sword") && s.getItem() instanceof SwordItem) {
                        bestSlot = i; break;
                    }
                    if (weaponPref.getMode().equals("Axe") && s.getItem() instanceof AxeItem) {
                        bestSlot = i; break;
                    }
                }
                if (bestSlot != -1) {
                    mc.player.getInventory().selectedSlot = bestSlot;
                }
            }

            // Check if we can attack based on CPS or cooldown
            boolean canAttack = false;
            double cpsValue = cps.getValue();
            
            if (cpsValue == 0) {
                // Use vanilla attack cooldown
                canAttack = mc.player.getAttackCooldownProgress(0.5f) == 1.0f;
            } else {
                // Use custom CPS timing
                long currentTime = System.currentTimeMillis();
                long delayMs = (long) (1000.0 / cpsValue);
                if (currentTime - lastAttackTime >= delayMs) {
                    canAttack = true;
                    lastAttackTime = currentTime;
                }
            }
            
            if (canAttack) {
                 mc.interactionManager.attackEntity(mc.player, target);
                 mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
