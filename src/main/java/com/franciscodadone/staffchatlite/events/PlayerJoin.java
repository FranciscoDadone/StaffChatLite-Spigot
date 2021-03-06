package com.franciscodadone.staffchatlite.events;

import com.franciscodadone.staffchatlite.thirdparty.bungeecord.BungeeCheck;
import com.franciscodadone.staffchatlite.storage.Global;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent ignored) {
        if(Global.serverName.equals("Unknown") && !Global.bungeeEnabled) {
            // Sleeps 3 seconds to wait the player join.
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored1) {}
            BungeeCheck.check();
        }
    }
}
