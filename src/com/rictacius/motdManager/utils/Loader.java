package com.rictacius.motdManager.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import com.rictacius.motdManager.Main;
import com.rictacius.motdManager.tasks.MOTD;
import com.rictacius.motdManager.tasks.MOTDScroll;
import com.rictacius.motdManager.tasks.MOTDStrobe;
import com.rictacius.motdManager.tasks.MOTDTextOnly;

import net.md_5.bungee.api.ChatColor;

public class Loader {
	private static HashMap<String, ArrayList<MOTD>> motds = new HashMap<String, ArrayList<MOTD>>();
	private static Main plugin = Main.pl;
	public static String prefix = ChatColor.translateAlternateColorCodes('&', "&b&l[&cMOTD.M&b&l] &r");
	public static ConsoleCommandSender console = Bukkit.getConsoleSender();

	public Loader() {
		loadMOTDs();
	}

	public static void loadMOTDs() {
		Set<String> labels = plugin.getConfig().getConfigurationSection("motds").getKeys(false);
		for (String label : labels) {
			try {
				List<String> types = new ArrayList<String>();

				// Top
				String base = "motds." + label + ".lineOne";
				String text = plugin.getConfig().getString(base + ".text");
				types.add(ChatColor.translateAlternateColorCodes('&', text));
				if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".scrollwhenoutofbounds"))) {
					if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".scrollrestartonend"))) {
						long speed = Long.parseLong(plugin.getConfig().getString(base + ".scrollspeed"));
						types.add("scroll#£#true#£#" + speed);
					} else {
						long speed = Long.parseLong(plugin.getConfig().getString(base + ".scrollspeed"));
						types.add("scroll#£#false#£#" + speed);
					}
				} else if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".strobe"))) {
					long delay = Long.parseLong(plugin.getConfig().getString(base + ".strobedelay"));
					String strobetext = ChatColor
							.translateAlternateColorCodes('&', plugin.getConfig().getString(base + ".strobetext"))
							.replaceAll("#£#", "");
					types.add("strobe#£#" + delay + "#£#" + strobetext);
				}
				parseMOTDKey(types, label, true);

				// Bottom
				types.clear();
				base = "motds." + label + ".lineTwo";
				text = plugin.getConfig().getString(base + ".text");
				types.add(ChatColor.translateAlternateColorCodes('&', text));
				if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".scrollwhenoutofbounds"))) {
					if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".scrollrestartonend"))) {
						long speed = Long.parseLong(plugin.getConfig().getString(base + ".scrollspeed"));
						types.add("scroll#£#true#£#" + speed);
					} else {
						long speed = Long.parseLong(plugin.getConfig().getString(base + ".scrollspeed"));
						types.add("scroll#£#false#£#" + speed);
					}
				} else if (Boolean.parseBoolean(plugin.getConfig().getString(base + ".strobe"))) {
					long delay = Long.parseLong(plugin.getConfig().getString(base + ".strobedelay"));
					String strobetext = ChatColor
							.translateAlternateColorCodes('&', plugin.getConfig().getString(base + ".strobetext"))
							.replaceAll("#£#", "");
					types.add("strobe#£#" + delay + "#£#" + strobetext);
				}
				parseMOTDKey(types, label, false);
			} catch (Exception e) {
				console.sendMessage(prefix + ChatColor.RED + "Could not load MOTD " + label + "!");
				e.printStackTrace();
			}
		}
	}

	public static void parseMOTDKey(List<String> types, String label, boolean isTop) {
		try {
			MOTDTextOnly newMOTD = new MOTDTextOnly(types.get(0), label, isTop);
			if (motds.get(label) != null) {
				if (motds.get(label).size() < 2) {
					ArrayList<MOTD> set = new ArrayList<MOTD>();
					MOTD current = motds.get(label).get(0);
					if (current.isTop()) {
						set.add(current);
						set.add(newMOTD);
					} else {
						set.add(newMOTD);
						set.add(current);
					}
					motds.put(label, set);
				}
			} else {
				ArrayList<MOTD> set = new ArrayList<MOTD>();
				set.add(newMOTD);
				motds.put(label, set);
			}
			console.sendMessage(prefix + ChatColor.GREEN + "Parsed MOTD " + label + "!");
		} catch (Exception e) {
			console.sendMessage(prefix + ChatColor.RED + "Could not parse MOTD " + label + "!");
			e.printStackTrace();
		}
	}

	public static void parseMOTDKey(List<String> types, String label, boolean isTop, String fake) {
		try {
			if (types.size() == 1) {
				MOTDTextOnly newMOTD = new MOTDTextOnly(types.get(0), label, isTop);
				if (motds.get(label) != null) {
					if (motds.get(label).size() < 2) {
						ArrayList<MOTD> set = new ArrayList<MOTD>();
						MOTD current = motds.get(label).get(0);
						if (current.isTop()) {
							set.add(current);
							set.add(newMOTD);
						} else {
							set.add(newMOTD);
							set.add(current);
						}
						motds.put(label, set);
					}
				} else {
					ArrayList<MOTD> set = new ArrayList<MOTD>();
					set.add(newMOTD);
					motds.put(label, set);
				}
			} else if (types.size() == 2) {
				if (types.get(1).startsWith("scroll")) {
					String[] data = types.get(1).split("#£#");
					boolean restart = Boolean.parseBoolean(data[1]);
					long speed = Long.parseLong(data[2]);
					MOTDScroll newMOTD = new MOTDScroll(types.get(0), label, isTop, restart, speed);
					if (motds.get(label) != null) {
						if (motds.get(label).size() < 2) {
							ArrayList<MOTD> set = new ArrayList<MOTD>();
							MOTD current = motds.get(label).get(0);
							if (current.isTop()) {
								set.add(current);
								set.add(newMOTD);
							} else {
								set.add(newMOTD);
								set.add(current);
							}
							motds.put(label, set);
						}
					} else {
						ArrayList<MOTD> set = new ArrayList<MOTD>();
						set.add(newMOTD);
						motds.put(label, set);
					}
				} else if (types.get(1).startsWith("strobe")) {
					String[] data = types.get(1).split("#£#");
					long delay = Long.parseLong(data[1]);
					MOTDStrobe newMOTD = new MOTDStrobe(types.get(0), label, isTop, delay, data[2]);
					if (motds.get(label) != null) {
						if (motds.get(label).size() < 2) {
							ArrayList<MOTD> set = new ArrayList<MOTD>();
							MOTD current = motds.get(label).get(0);
							if (current.isTop()) {
								set.add(current);
								set.add(newMOTD);
							} else {
								set.add(newMOTD);
								set.add(current);
							}
							motds.put(label, set);
						}
					} else {
						ArrayList<MOTD> set = new ArrayList<MOTD>();
						set.add(newMOTD);
						motds.put(label, set);
					}
				}
			}
			console.sendMessage(prefix + ChatColor.GREEN + "Parsed MOTD " + label + "!");
		} catch (Exception e) {
			console.sendMessage(prefix + ChatColor.RED + "Could not parse MOTD " + label + "!");
			e.printStackTrace();
		}
	}

	public static ArrayList<MOTD> getLines(String label) {
		return motds.get(label);
	}

	public static HashMap<String, ArrayList<MOTD>> getMOTDs() {
		return motds;
	}
}
