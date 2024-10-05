package org.bischofftv.logtoolspigot;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.event.block.SignChangeEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class LogtoolSpigot extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Register the custom messaging channel to communicate with BungeeCord
        Messenger messenger = getServer().getMessenger();
        messenger.registerOutgoingPluginChannel(this, "logtool:channel");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        String signContent = String.join(" | ", lines);

        // Send sign content to BungeeCord
        sendDataToBungee(player, "SignChange", signContent);

        getLogger().info(player.getName() + " wrote a sign with content: " + signContent);
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        BookMeta bookMeta = event.getNewBookMeta();
        List<String> pages = bookMeta.getPages();

        // Check if the player signed the book
        if (event.isSigning()) {
            String title = bookMeta.getTitle();
            // Sending signed book info to BungeeCord
            sendDataToBungee(player, "bookSign", title);
        } else {
            // Sending book content to BungeeCord (edited but not signed)
            sendDataToBungee(player, "bookEdit", String.join(" ", pages));
        }
    }

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        if (event.getResult() != null && event.getResult().getType() != Material.AIR) {
            if (event.getInventory().getRenameText() != null && !event.getInventory().getRenameText().isEmpty()) {
                String renameText = event.getInventory().getRenameText();

                // Sending renamed item info to BungeeCord
                sendDataToBungee(null, "anvilRename", renameText);
            }
        }
    }

    private void sendDataToBungee(Player player, String action, String data) {
        if (player == null) return;

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(byteArray);

        try {
            out.writeUTF(action);
            out.writeUTF(player.getName());
            out.writeUTF(data);

            // Sending the plugin message
            player.sendPluginMessage(this, "logtool:channel", byteArray.toByteArray());
        } catch (IOException e) {
            getLogger().severe("Failed to send data to BungeeCord: " + e.getMessage());
        }
    }
}
