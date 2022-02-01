package com.franciscodadone.staffchatlite.chat;

import com.franciscodadone.staffchatlite.bungeecord.BungeeCheck;
import com.franciscodadone.staffchatlite.bungeecord.senders.BungeeSendMessage;
import com.franciscodadone.staffchatlite.permissions.PermissionTable;
import com.franciscodadone.staffchatlite.storage.Global;
import com.franciscodadone.staffchatlite.util.Logger;
import com.franciscodadone.staffchatlite.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatManager {

    public static void sendStaffChatMessage(CommandSender sender, String message) {
        if(Bukkit.getServer().getOnlinePlayers().size() > 0) {
            String playerName = (sender instanceof Player) ? ((Player)sender).getPlayerListName() : "Console";

            if(Global.bungeeEnabled) {
                if(Global.serverName.equals("Unknown")) {
                    BungeeCheck.check();
                    new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        BungeeSendMessage.send(playerName, message, Global.serverName);
                        sendStaffChatMessageFromOtherServer(playerName, message, Global.serverName);
                    }).start();
                }
            } else {
                sendStaffChatMessage(playerName, message);
            }
        } else {
            Logger.warning("Could not send staff chat message because there is no one online.");
        }
    }

    public static void sendStaffChatMessageFromOtherServer(String playerName, String message, String serverName) {
        String toSend = Global.plugin.getConfig().getString("chat-style");
        toSend = toSend.replace("%player%", playerName);
        toSend = toSend.replace("%prefix%", Global.langConfig.getConfig().getString("prefix"));
        toSend = toSend.replace("%message%", message);
        toSend = toSend.replace("%server%", "(" + serverName + ")");
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(p.hasPermission(PermissionTable.chat)) {
                p.sendMessage(Utils.Color(toSend));
            }
        }
        Bukkit.getConsoleSender().sendMessage(Utils.Color(toSend));
    }

    public static void sendStaffChatMessage(String playerName, String message) {
        String toSend = Global.plugin.getConfig().getString("chat-style");
        toSend = toSend.replace("%player%", playerName);
        toSend = toSend.replace("%prefix%", Global.langConfig.getConfig().getString("prefix"));
        toSend = toSend.replace("%message%", message);
        toSend = toSend.replace("%server%", "");
        for(Player p : Bukkit.getServer().getOnlinePlayers()) {
            if(p.hasPermission(PermissionTable.chat)) {
                p.sendMessage(Utils.Color(toSend));
            }
        }
        Bukkit.getConsoleSender().sendMessage(Utils.Color(toSend));
    }

    public static void toggleStaffChat(Player player) {
        if(!Global.playersToggledStaffChat.contains(player)) {
            Global.playersToggledStaffChat.add(player);
        } else {
            Global.playersToggledStaffChat.remove(player);
        }
    }

}
