package com.franciscodadone.staffchatlite;

import com.franciscodadone.staffchatlite.thirdparty.bungeecord.BungeeCheck;
import com.franciscodadone.staffchatlite.commands.CommandManager;
import com.franciscodadone.staffchatlite.events.ChatEvent;
import com.franciscodadone.staffchatlite.thirdparty.bungeecord.listeners.BungeeMessageListener;
import com.franciscodadone.staffchatlite.events.PlayerJoin;
import com.franciscodadone.staffchatlite.storage.Global;
import com.franciscodadone.staffchatlite.thirdparty.bstats.Metrics;
import com.franciscodadone.staffchatlite.thirdparty.discordsrv.StaffDiscordHandler;
import com.franciscodadone.staffchatlite.thirdparty.discordsrv.StaffDiscordSRVListener;
import com.franciscodadone.staffchatlite.util.Logger;
import com.franciscodadone.staffchatlite.util.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class StaffChatLite extends JavaPlugin {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onEnable() {

        // // Global variables // //
        Global.plugin = this;
        //noinspection InstantiationOfUtilityClass
        new Global();

        // Events
        getServer().getPluginManager().registerEvents(new ChatEvent(), this);               // enabling the listener
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);

        // BungeeCord integration
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeMessageListener());
        BungeeCheck.check();

        // // Global Config // //
        this.saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();

        // Loading Lang config
        // Getting all lang files from jar
        ArrayList<String> langFiles = new ArrayList<>();
        try {
            final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            while(entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith("lang/")) {
                    name = name.replaceAll("lang/", "");
                    if(!name.equals("")) langFiles.add(name);
                }
            }
            jar.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Creating lang folder if it doesn't exists.
        File langFolder = new File(getDataFolder() + File.separator + "lang");
        if(!langFolder.isDirectory()) {
            langFolder.mkdir();
        }
        // Iterating over lang files found in jar to if it doesn't exist, create it or
        // check for updates.
        for(String langFile : langFiles) {
            File file = new File(getDataFolder() + File.separator + "lang", langFile);
            if(!file.exists()) {
                try {
                    new File(getDataFolder() + File.separator + "lang", langFile).createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                ConfigUpdater.update(this, "lang" + File.separator + langFile, new File(getDataFolder() + File.separator + "lang", langFile), Collections.emptyList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // // Hooks // //
        // DiscordSRV
        try {
            if(getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
                StaffDiscordHandler.init();
                DiscordSRV.api.subscribe(discordSRVListener);
                Logger.info("DiscordSRV Hooked!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // // Loading commands // //
        Objects.requireNonNull(this.getCommand("sc")).setExecutor(new CommandManager());
        Objects.requireNonNull(this.getCommand("staffchat")).setExecutor(new CommandManager());
        Objects.requireNonNull(this.getCommand("schelp")).setExecutor(new CommandManager());
        Objects.requireNonNull(this.getCommand("scadmin")).setExecutor(new CommandManager());
        Objects.requireNonNull(this.getCommand("sct")).setExecutor(new CommandManager());

        // // Update checker // //
        new UpdateChecker(99628).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Logger.info("Up to date!");
            } else {
                Logger.warning("Plugin Outdated!");
                Logger.warning("&eDownload the new version from: &9https://www.spigotmc.org/resources/staffchatlite.99628/");
                UpdateChecker.updateString = version;
            }
        });

        // Metrics and lang file chart
        Metrics metrics = new Metrics(this, 14124);
        metrics.addCustomChart(new Metrics.SimplePie("lang_file", () -> Global.plugin.getConfig().getString("lang-file")));

    }

    @Override
    public void onDisable() {
        if(Global.bungeeEnabled) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        }
        if(Global.discordSRVEnabled) DiscordSRV.api.unsubscribe(discordSRVListener);
    }

    private StaffDiscordSRVListener discordSRVListener = new StaffDiscordSRVListener();
}
