package com.penguin.client.mixin;

import com.penguin.client.module.ModuleManager;
import com.penguin.client.module.modules.AntiAim;
import com.penguin.client.module.modules.AntiCrash;
import com.penguin.client.module.modules.AntiHunger;
import com.penguin.client.module.modules.AntiVanish;
import com.penguin.client.module.modules.AutoFish;
import com.penguin.client.module.modules.ChatSuffix;
import com.penguin.client.module.modules.ClientSettings;
import com.penguin.client.module.modules.FancyChat;
import com.penguin.client.module.modules.KnockbackPlus;
import com.penguin.client.module.modules.Sneak;
import com.penguin.client.module.modules.Velocity;
import com.penguin.client.module.modules.XCarry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (ClientSettings.INSTANCE != null && ClientSettings.INSTANCE.disableEverythingOnJoin.isEnabled()) {
            if (ModuleManager.INSTANCE != null) {
                ModuleManager.INSTANCE.getModules().forEach(m -> {
                    if (m.isEnabled()) m.toggle();
                });
            }
        }
    }

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // AntiCrash check for velocity
        if (AntiCrash.INSTANCE != null && AntiCrash.INSTANCE.isEnabled()) {
            double velX = packet.getVelocityX() / 8000.0;
            double velY = packet.getVelocityY() / 8000.0;
            double velZ = packet.getVelocityZ() / 8000.0;
            if (!AntiCrash.isVelocityValid(velX, velY, velZ)) {
                if (AntiCrash.INSTANCE.logAttempts.isEnabled() && mc.player != null) {
                    mc.player.sendMessage(net.minecraft.text.Text.of("§c[AntiCrash] §fBlocked invalid velocity packet"), false);
                }
                ci.cancel();
                return;
            }
        }
        
        if (Velocity.isEnabledStatic()) {
            if (mc.player != null && packet.getId() == mc.player.getId()) {
                double hMult = Velocity.getHorizontalMultiplier();
                double vMult = Velocity.getVerticalMultiplier();
                
                // If both multipliers are 0, cancel entirely
                if (hMult == 0 && vMult == 0) {
                    ci.cancel();
                    return;
                }
                
                // Apply modified velocity after packet is processed
                mc.execute(() -> {
                    if (mc.player != null) {
                        mc.player.setVelocity(
                            mc.player.getVelocity().x * hMult,
                            mc.player.getVelocity().y * vMult,
                            mc.player.getVelocity().z * hMult
                        );
                    }
                });
            }
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // AntiCrash check for explosion
        if (AntiCrash.INSTANCE != null && AntiCrash.INSTANCE.isEnabled()) {
            double x = packet.getX();
            double y = packet.getY();
            double z = packet.getZ();
            // In 1.20.1, player velocity is returned directly from the packet
            float knockbackX = packet.getPlayerVelocityX();
            float knockbackY = packet.getPlayerVelocityY();
            float knockbackZ = packet.getPlayerVelocityZ();
            
            if (!AntiCrash.isPositionValid(x, y, z) || !AntiCrash.isVelocityValid(knockbackX, knockbackY, knockbackZ)) {
                if (AntiCrash.INSTANCE.logAttempts.isEnabled() && mc.player != null) {
                    mc.player.sendMessage(net.minecraft.text.Text.of("§c[AntiCrash] §fBlocked invalid explosion packet"), false);
                }
                ci.cancel();
                return;
            }
        }
        
        if (Velocity.isEnabledStatic()) {
            double hMult = Velocity.getHorizontalMultiplier();
            double vMult = Velocity.getVerticalMultiplier();
            
            // If both multipliers are 0, cancel entirely
            if (hMult == 0 && vMult == 0) {
                ci.cancel();
                return;
            }
            
            // Apply modified velocity after packet is processed
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.setVelocity(
                        mc.player.getVelocity().x * hMult,
                        mc.player.getVelocity().y * vMult,
                        mc.player.getVelocity().z * hMult
                    );
                }
            });
        }
    }
    
    @Inject(method = "onParticle", at = @At("HEAD"), cancellable = true)
    private void onParticle(ParticleS2CPacket packet, CallbackInfo ci) {
        // AntiCrash check for particles
        if (AntiCrash.INSTANCE != null && AntiCrash.INSTANCE.isEnabled()) {
            if (!AntiCrash.isParticleCountValid(packet.getCount())) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (AntiCrash.INSTANCE.logAttempts.isEnabled() && mc.player != null) {
                    mc.player.sendMessage(net.minecraft.text.Text.of("§c[AntiCrash] §fBlocked particle spam (" + packet.getCount() + " particles)"), false);
                }
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"), cancellable = true)
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        // AntiCrash check for position
        if (AntiCrash.INSTANCE != null && AntiCrash.INSTANCE.isEnabled()) {
            if (!AntiCrash.isPositionValid(packet.getX(), packet.getY(), packet.getZ())) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (AntiCrash.INSTANCE.logAttempts.isEnabled() && mc.player != null) {
                    mc.player.sendMessage(net.minecraft.text.Text.of("§c[AntiCrash] §fBlocked invalid position packet"), false);
                }
                ci.cancel();
            }
        }
    }
    
    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        // AntiVanish message tracking
        if (AntiVanish.INSTANCE != null && AntiVanish.INSTANCE.isEnabled()) {
            String message = packet.content().getString();
            AntiVanish.INSTANCE.onReceiveMessage(message);
        }
    }

    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
    private String modifyChatMessage(String content) {
        // Don't modify commands
        if (content.startsWith("/")) {
            return content;
        }
        
        String result = content;
        
        // Apply FancyChat transformation first
        result = FancyChat.transformText(result);
        
        // Then apply ChatSuffix if enabled
        if (ChatSuffix.enabled) {
            result = result + ChatSuffix.suffix.getValue();
        }
        
        return result;
    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (XCarry.INSTANCE != null && XCarry.INSTANCE.isEnabled()) {
            if (packet instanceof CloseHandledScreenC2SPacket) {
                if (((CloseHandledScreenC2SPacket) packet).getSyncId() == MinecraftClient.getInstance().player.playerScreenHandler.syncId) {
                    ci.cancel();
                }
            }
        }

        if (Sneak.INSTANCE != null && Sneak.INSTANCE.isEnabled()) {
            if (packet instanceof ClientCommandC2SPacket) {
                ClientCommandC2SPacket command = (ClientCommandC2SPacket) packet;
                if (command.getMode() == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY) {
                    ci.cancel();
                }
            }
        }
        
        // KnockbackPlus - send sprint packet on attack
        if (packet instanceof PlayerInteractEntityC2SPacket) {
            KnockbackPlus.onAttack();
        }
        
        // AntiAim packet modification
        if (packet instanceof net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket) {
            net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket move = (net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket) packet;
            PlayerMoveC2SPacketAccessor accessor = (PlayerMoveC2SPacketAccessor) move;
            
            // Check AntiAim - applies for Server Only, Spin, and Jitter modes
            if (AntiAim.INSTANCE != null && AntiAim.INSTANCE.isEnabled()) {
                String mode = AntiAim.INSTANCE.mode.getMode();
                if (mode.equals("Server Only") || mode.equals("Spin") || mode.equals("Jitter")) {
                    accessor.setYaw(AntiAim.getServerYaw(accessor.getYaw()));
                    accessor.setPitch(AntiAim.getServerPitch(accessor.getPitch()));
                }
            }
            
            // Then check AntiHunger
            if (AntiHunger.enabled) {
                if (MinecraftClient.getInstance().player != null && !MinecraftClient.getInstance().player.isFallFlying() && !MinecraftClient.getInstance().player.getAbilities().flying) {
                    accessor.setOnGround(false);
                }
            }
        }

        if (AntiHunger.enabled) {
            if (packet instanceof ClientCommandC2SPacket) {
                ClientCommandC2SPacket command = (ClientCommandC2SPacket) packet;
                if (command.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        if (AutoFish.INSTANCE != null && AutoFish.INSTANCE.isEnabled()) {
            if (packet.getSound().value() == net.minecraft.sound.SoundEvents.ENTITY_FISHING_BOBBER_SPLASH) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && mc.player.fishHook != null) {
                    double dist = mc.player.fishHook.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ());
                    if (dist < 4.0) {
                         AutoFish.INSTANCE.caught = true;
                    }
                }
            }
        }
    }
}
