package net.coreprotect.listener.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;

public class PluginChannelHandshakeListener implements PluginMessageListener, Listener {

    public static final String pluginChannel = "coreprotect:handshake";
    private static final int networkingProtocolVersion = 1; // Changed to static final for clarity
    private final Set<UUID> pluginChannelPlayers;
    private static PluginChannelHandshakeListener instance;

    public PluginChannelHandshakeListener() {
        instance = this;
        pluginChannelPlayers = new HashSet<>();
    }

    public static PluginChannelHandshakeListener getInstance() {
        return instance;
    }

    public Set<UUID> getPluginChannelPlayers() {
        return new HashSet<>(pluginChannelPlayers); // Return a copy for safety
    }

    public boolean isPluginChannelPlayer(CommandSender commandSender) {
        return commandSender instanceof Player player && getPluginChannelPlayers().contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        pluginChannelPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        handleHandshake(channel, player, bytes);
    }

    private void handleHandshake(String channel, Player player, byte[] bytes) {
        if (!player.hasPermission("coreprotect.networking") || !channel.equals(pluginChannel)) {
            return;
        }

        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)) {

            String modVersion = dis.readUTF();
            String modId = dis.readUTF();
            int protocolVersion = dis.readInt();

            if (Config.getGlobal().NETWORK_DEBUG) {
                Chat.console(new String(bytes));
                Chat.console(modVersion);
                Chat.console(modId);
                Chat.console(String.valueOf(protocolVersion));
            }

            if (protocolVersion != networkingProtocolVersion) {
                Chat.console(Phrase.build(Phrase.NETWORK_CONNECTION, player.getName(), modId, modVersion, Selector.SECOND));
                return;
            }

            pluginChannelPlayers.add(player.getUniqueId());
            Chat.console(Phrase.build(Phrase.NETWORK_CONNECTION, player.getName(), modId, modVersion, Selector.FIRST));

            player.sendPluginMessage(CoreProtect.getInstance(), pluginChannel, sendRegistered());
        } catch (IOException exception) {
            Chat.console(exception.toString());
            exception.printStackTrace();
        }
    }

    private byte[] sendRegistered() throws IOException {
        try (ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
             DataOutputStream msgOut = new DataOutputStream(msgBytes)) {
            msgOut.writeBoolean(true);
            return msgBytes.toByteArray();
        }
    }
}