package com.rictacius.motdManager.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jpaste.pastebin.Pastebin;

import com.rictacius.motdManager.Main;
import com.rictacius.motdManager.tasks.MOTD;

public class ErrorFile {
	private static Main plugin = Main.pl;

	public static URL writeError(String error) {
		try {
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE-MMMM-d-yyyy-HH-mm");
			String date = formatter.format(now);
			File errorFile = new File(plugin.getDataFolder().getPath() + "/errors/" + date + ".yml");
			File defaultfile = new File(plugin.getDataFolder().getPath() + "/default-error.yml");
			if (!defaultfile.exists()) {
				errorFile.mkdirs();
				defaultfile.mkdirs();
				plugin.saveResource("default-error.yml", true);
				Loader.console.sendMessage(Loader.prefix + defaultfile.getPath());
			}
			FileConfiguration config = new YamlConfiguration();
			try {
				config.load(defaultfile);
			} catch (FileNotFoundException e) {
				Loader.console.sendMessage(
						Loader.prefix + "Could not find error file path, did you modify the jar contents?");
				e.printStackTrace();
			} catch (IOException e) {
				Loader.console.sendMessage(Loader.prefix + "Could not load error file, does the file exisit?");
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				Loader.console
						.sendMessage(Loader.prefix + "Could not load error file did you edit the files contents?");
				e.printStackTrace();
			}
			config.options().copyDefaults(true);
			config.set("error", error);
			config.set("date", date);
			String serverName = Bukkit.getServerName();
			String serverType = Bukkit.getServer().getName();
			String serverIP = Bukkit.getServer().getIp();
			if (serverIP == null) {
				serverIP = "Unknown";
			} else if (serverIP.equals("")) {
				serverIP = "Unknown";
			}
			int serverPort = Bukkit.getServer().getPort();
			String serverVersion = Bukkit.getServer().getVersion();
			String motdmVersion = plugin.getDescription().getVersion();
			String configFile = plugin.getConfig().saveToString().toString();
			String variablesFile = plugin.getVariablesConfig().saveToString().toString();
			String playersFile = plugin.getPlayersConfig().saveToString().toString();
			Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();
			for (int i = 0; i < plugins.length; i++) {
				if (Bukkit.getServer().getPluginManager().getPlugin(plugins[i].getName()).isEnabled()) {
					config.set("plugins." + plugins[i].getName(), "enabled");
				} else {
					config.set("plugins." + plugins[i].getName(), "disabled");
				}
			}
			config.set("server-name", serverName);
			config.set("server-type", serverType);
			config.set("server-ip", serverIP + " : " + serverPort);
			config.set("server-version", serverVersion);
			config.set("motdm-version", motdmVersion);
			config.set("files.config", configFile);
			config.set("files.variables", variablesFile);
			config.set("files.players", playersFile);
			HashMap<String, ArrayList<MOTD>> motds = Loader.getMOTDs();
			for (String label : motds.keySet()) {
				for (int i = 0; i < motds.get(label).size(); i++) {
					MOTD motd = motds.get(label).get(i);
					String type = motd.getType();
					boolean isTop = motd.isTop();
					String current = motd.getMOTD();
					String applied = motd.getMOTDApplied();
					config.set("runningMOTDS." + label + "." + i + ".type", type);
					config.set("runningMOTDS." + label + "." + i + ".isTop", isTop);
					config.set("runningMOTDS." + label + "." + i + ".state", current.replaceAll("\\xa7", "&"));
					config.set("runningMOTDS." + label + "." + i + ".current", applied.replaceAll("\\xa7", "&"));
				}
			}
			config.save(errorFile);
			return Pastebin.pastePaste("707d4468afc6923cb547cc3eb5a44297", config.saveToString(),
					"MOTDManager Dump File");
		} catch (Exception e) {
			Loader.console
					.sendMessage(Loader.prefix + "Could not write error dump file did you edit the configs correctly?");
			e.printStackTrace();
		}
		return null;
	}
}
