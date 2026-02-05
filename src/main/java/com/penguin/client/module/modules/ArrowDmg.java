package com.penguin.client.module.modules;

import com.penguin.client.module.Category;
import com.penguin.client.module.Module;
import com.penguin.client.settings.BooleanSetting;
import com.penguin.client.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * ArrowDmg module - Massively increases arrow damage.
 * Works by sending position packets when releasing a bow/trident.
 * Consumes a lot of hunger and reduces accuracy.
 * Does not work with crossbows and may be patched on Paper servers.
 * Ported from meteor-rejects, adapted for 1.20.1
 */
public class ArrowDmg extends Module {
    public static ArrowDmg INSTANCE;

    public NumberSetting packets = new NumberSetting("Packets", 200, 2, 2000, 10);
    public BooleanSetting tridents = new BooleanSetting("Tridents", false);

    public ArrowDmg() {
        super("ArrowDmg", "Increases arrow damage by sending movement packets on bow release. Consumes hunger and reduces accuracy. Tridents option also works with tridents. only works on some specific servers / configurations.", Category.COMBAT);
        addSetting(packets);
        addSetting(tridents);
        INSTANCE = this;
    }

    /**
     * Called when player stops using an item (releasing bow/trident)
     */
    public void onStopUsingItem(Item item) {
        if (!isEnabled()) return;
        if (!isValidItem(item)) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.networkHandler == null) return;

        // Send sprint packet first
        mc.player.networkHandler.sendPacket(
            new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING)
        );

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        int packetCount = (int) packets.getValue();

        // Send alternating position packets to increase damage
        for (int i = 0; i < packetCount / 2; i++) {
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 1e-10, z, true)
            );
            mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 1e-10, z, false)
            );
        }
    }

    private boolean isValidItem(Item item) {
        if (item instanceof BowItem) return true;
        if (tridents.isEnabled() && item == Items.TRIDENT) return true;
        return false;
    }
}
