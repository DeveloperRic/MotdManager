package com.rictacius.motdManager;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;
import com.rictacius.motdManager.commands.MOTDMCommand;
import com.rictacius.motdManager.listener.PlayerUUIDResolver;
import com.rictacius.motdManager.listener.RefreshTimer;
import com.rictacius.motdManager.tasks.MOTD;
import com.rictacius.motdManager.utils.ErrorFile;
import com.rictacius.motdManager.utils.Loader;
import com.rictacius.motdManager.utils.ServerChecker;
import com.rictacius.motdManager.utils.Variables;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Main extends JavaPlugin implements Listener {
	PluginDescriptionFile pdfFile = getDescription();
	Logger logger = getLogger();

	public static Main pl;

	public void onEnable() {
		pl = this;
		setupPermissions();
		setupChat();
		setupEconomy();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Config...."), ChatColor.YELLOW);
		createFiles();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Commands...."), ChatColor.YELLOW);
		registerCommands();
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Registering Events...."), ChatColor.YELLOW);
		registerEvents();
		new Variables();
		Loader.loadMOTDs();
		createProtocolEvent();
		Methods.sendColoredMessage(this, ChatColor.AQUA,
				(pdfFile.getName() + " has been enabled! (V." + pdfFile.getVersion() + ")"), ChatColor.GREEN);
	}

	public static Permission permission = null;
	public static Economy economy = null;
	public static Chat chat = null;

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}

	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.chat.Chat.class);
		if (chatProvider != null) {
			chat = chatProvider.getProvider();
		}

		return (chat != null);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	public void onDisable() {
		if (Boolean.parseBoolean(getConfig().getString("change-default-motd"))) {
			String original = Bukkit.getMotd();
			boolean goneFar = false;
			try {
				Properties properties = new Properties();
				FileInputStream in = new FileInputStream("server");
				properties.load(in);
				in.close();
				List<MOTD> lines = Loader.getLines(getConfig().getString("new-default-motd"));
				String motd = MOTD.combine(lines.get(0), lines.get(1));
				properties.setProperty("motd", motd);
				goneFar = true;
				FileOutputStream out = new FileOutputStream("server");
				properties.store(out, "---No Comment---");
				out.close();
				Methods.sendColoredMessage(this, ChatColor.AQUA, (pdfFile.getName() + " modified default MOTD!"),
						ChatColor.YELLOW);
			} catch (Exception e) {
				if (goneFar) {
					ErrorFile.writeError(
							"Could not motdify default MOTD for offline/restarting servers, here is your original motd: ("
									+ original + ")");
				}
			}
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA,
				(pdfFile.getName() + " has been disabled! (V." + pdfFile.getVersion() + ")"), ChatColor.YELLOW);
	}

	public void registerCommands() {
		try {
			getCommand("motdmanager").setExecutor(new MOTDMCommand());
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering commands!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Commands successfuly registered!"), ChatColor.LIGHT_PURPLE);
	}

	public void registerEvents() {
		try {
			PluginManager pm = getServer().getPluginManager();

			pm.registerEvents(new PlayerUUIDResolver(), this);
			pm.registerEvents(new ServerChecker(), this);
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering events!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Events successfuly registered!"), ChatColor.LIGHT_PURPLE);
	}

	public void registerConfig() {
		try {
			getConfig().options().copyDefaults(true);
			saveConfig();

		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Error while registering config!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.AQUA, ("Trace:"), ChatColor.RED);
			e.printStackTrace();
		}
		Methods.sendColoredMessage(this, ChatColor.AQUA, ("Config successfuly registered!"), ChatColor.LIGHT_PURPLE);
	}

	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("Exteria_Utilities");
	}

	private HashMap<InetAddress, RefreshTimer> timers = new HashMap<InetAddress, RefreshTimer>();

	public void registerNewIPTimer(InetAddress ip, WrappedServerPing ping, MOTD top, MOTD bottom, Player p) {
		if (timers.get(ip) != null) {
			RefreshTimer rt = timers.get(ip);
			rt.destroyTimer();
			rt = null;
		}
		RefreshTimer rt = new RefreshTimer(ping, top, bottom, p);
		timers.put(ip, rt);
	}

	public void handlePacketEvent(PacketEvent e) {
		WrappedServerPing ping = (WrappedServerPing) e.getPacket().getServerPings().read(0);
		Player player = e.getPlayer();
		Set<String> labels = getConfig().getConfigurationSection("motds").getKeys(false);
		int count = 0;
		InetAddress ip = player.getAddress().getAddress();
		for (String label : labels) {
			ArrayList<MOTD> lines = Loader.getLines(label);
			MOTD top = lines.get(0);
			MOTD bottom = lines.get(1);
			String perm = top.getPermission();
			if (permission.playerHas(getConfig().getString("perm-world"), player, perm)) {
				registerNewIPTimer(ip, ping, top, bottom, e.getPlayer());
				break;
			} else {
				if (count == 0) {
					registerNewIPTimer(ip, ping, top, bottom, e.getPlayer());
					break;
				}
			}
			count++;
		}
		UUID uid = PlayerUUIDResolver.resolveUUID(player);
		if (Boolean.parseBoolean(getConfig().getString("change-server-playerslist"))) {
			List<WrappedGameProfile> players = new ArrayList<WrappedGameProfile>();
			List<String> playerslist = getConfig().getStringList("new-server-playerslist");
			for (String line : playerslist) {
				players.add(new WrappedGameProfile(UUID.randomUUID(),
						Variables.applyPlayer(ChatColor.translateAlternateColorCodes('&', line), uid)));
			}
			ping.setPlayers(players);
		}
		if (Boolean.parseBoolean(getConfig().getString("change-current-players"))) {
			ping.setPlayersOnline(Integer.parseInt(getConfig().getString("new-current-players")));
		}
		if (Boolean.parseBoolean(getConfig().getString("change-max-players"))) {
			ping.setPlayersMaximum(Integer.parseInt(getConfig().getString("new-max-players")));
		}
		if (Boolean.parseBoolean(getConfig().getString("change-version-message"))) {
			ping.setVersionProtocol(-1);
			ping.setVersionName(Variables.applyPlayer(
					ChatColor.translateAlternateColorCodes('&', getConfig().getString("new-version-message")), uid));
		} else {
			ping.setVersionProtocol(ping.getVersionProtocol());
		}
		if (Boolean.parseBoolean(getConfig().getString("change-servericon-face"))) {
			try {
				ping.setFavicon(CompressedImage.fromPng(getPlayerIcon(Bukkit.getOfflinePlayer(uid).getName())));
			} catch (Exception e1) {
				String url = getConfig().getString("default-image-url");
				try {
					ping.setFavicon(CompressedImage.fromPng(getIcon(url)));
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		} else if (Boolean.parseBoolean(

				getConfig().getString("change-servericon-png"))) {
			try {
				String url = getConfig().getString("new-servericon-link");
				ping.setFavicon(CompressedImage.fromPng(getIcon(url)));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public void createProtocolEvent() {
		if (Boolean.parseBoolean(getConfig().getString("enabled"))) {
			ProtocolLibrary.getProtocolManager()
					.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL,
							Arrays.asList(new PacketType[] { PacketType.Status.Server.SERVER_INFO }),
							new ListenerOptions[] { ListenerOptions.ASYNC }) {
						public void onPacketSending(PacketEvent event) {
							handlePacketEvent(event);
						}
					});
		}
	}

	private BufferedImage getIcon(String url) throws IOException {
		URL asset = new URL(url);
		Image img = ImageIO.read(asset);

		return toBufferedImage(img.getScaledInstance(64, 64, 1));
	}

	private BufferedImage getPlayerIcon(String name) throws IOException {
		URL asset = new URL("https://minotar.net/helm/" + name);
		Image img = ImageIO.read(asset);

		return toBufferedImage(img.getScaledInstance(64, 64, 1));
	}

	// Utility method
	private BufferedImage toBufferedImage(Image image) {
		BufferedImage buffer = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = buffer.createGraphics();

		g.drawImage(image, null, null);
		return buffer;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private File configf, variablesf, playersf;
	private FileConfiguration config, variables, players;

	public FileConfiguration getVariablesConfig() {
		return this.variables;
	}

	public FileConfiguration getPlayersConfig() {
		return this.players;
	}

	public int reloadAllConfigFiles() {
		createFiles();
		int errors = 0;
		ArrayList<String> errorFiles = new ArrayList<String>();
		String file = "";
		ArrayList<StackTraceElement[]> traces = new ArrayList<StackTraceElement[]>();
		StackTraceElement[] trace = null;
		try {
			this.reloadConfig();
		} catch (Exception e) {
			errors++;
			trace = e.getStackTrace();
			traces.add(trace);
			file = "Main Config File";
			errorFiles.add(file);
		}
		try {
			variables = YamlConfiguration.loadConfiguration(variablesf);
		} catch (Exception e) {
			errors++;
			trace = e.getStackTrace();
			traces.add(trace);
			file = "Variables Config File";
			errorFiles.add(file);
		}
		try {
			players = YamlConfiguration.loadConfiguration(playersf);
		} catch (Exception e) {
			errors++;
			trace = e.getStackTrace();
			traces.add(trace);
			file = "Players Config File";
			errorFiles.add(file);
		}
		try {
			Variables.reload();
		} catch (Exception e) {
			errors++;
			trace = e.getStackTrace();
			traces.add(trace);
			file = "Variables Class";
			errorFiles.add(file);
		}
		try {
			Loader.loadMOTDs();
		} catch (Exception e) {
			errors++;
			trace = e.getStackTrace();
			traces.add(trace);
			file = "Loader Class";
			errorFiles.add(file);
		}
		if (errors > 0) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not reload all config files!"), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("The following files generated erros:"), ChatColor.RED);
			for (String fileName : errorFiles) {
				Methods.sendColoredMessage(this, ChatColor.GOLD, (ChatColor.GRAY + " - " + ChatColor.RED + fileName),
						ChatColor.RED);
			}
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace(s):"), ChatColor.RED);
			for (StackTraceElement[] currentTrace : traces) {
				int i = 0;
				Methods.sendColoredMessage(this, ChatColor.GOLD,
						(ChatColor.GRAY + "* " + ChatColor.RED + errorFiles.get(i)), ChatColor.RED);
				for (StackTraceElement printTrace : currentTrace) {
					Methods.sendColoredMessage(this, ChatColor.GOLD, (printTrace.toString()), ChatColor.RED);
				}
				i++;
			}
		}
		return errors;
	}

	public void saveAllConfigFiles() {
		saveConfig();
		try {
			getVariablesConfig().save(variablesf);
		} catch (Exception ex) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + variablesf), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
			ex.printStackTrace();
		}
	}

	public void saveVariablesFile() {
		try {
			getVariablesConfig().save(variablesf);
		} catch (Exception ex) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + variablesf), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
			ex.printStackTrace();
		}
	}

	public void savePlayersFile() {
		try {
			getPlayersConfig().save(playersf);
		} catch (Exception ex) {
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Could not save config to " + playersf), ChatColor.RED);
			Methods.sendColoredMessage(this, ChatColor.GOLD, ("Trace:"), ChatColor.RED);
			ex.printStackTrace();
		}
	}

	private void createFiles() {
		try {
			configf = new File(getDataFolder(), "config.yml");
			variablesf = new File(getDataFolder(), "variables.yml");
			playersf = new File(getDataFolder(), "players.yml");

			if (!configf.exists()) {
				configf.getParentFile().mkdirs();
				saveResource("config.yml", false);
			}
			if (!variablesf.exists()) {
				variablesf.getParentFile().mkdirs();
				saveResource("variables.yml", false);
			}
			if (!playersf.exists()) {
				playersf.getParentFile().mkdirs();
				saveResource("players.yml", false);
			}

			config = new YamlConfiguration();
			variables = new YamlConfiguration();
			players = new YamlConfiguration();
			try {
				config.load(configf);
				variables.load(variablesf);
				players.load(playersf);
			} catch (Exception e) {
				Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
						ChatColor.RED);
				e.printStackTrace();
			}
			getConfig().options().copyDefaults(false);
		} catch (Exception e) {
			Methods.sendColoredMessage(this, ChatColor.LIGHT_PURPLE, ("Error while registering config!"),
					ChatColor.RED);
			e.printStackTrace();
		}
	}
}
