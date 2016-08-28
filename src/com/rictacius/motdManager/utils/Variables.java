package com.rictacius.motdManager.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;

import com.rictacius.motdManager.Main;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Variables {
	private static HashMap<String, String> variables = new HashMap<String, String>();

	public Variables() {
		reload();
	}

	public static void reload() {
		Set<String> keys = Main.pl.getVariablesConfig().getConfigurationSection("variables").getKeys(false);
		for (String key : keys) {
			if (key.startsWith("?server")) {
				Server server = Bukkit.getServer();
				String findkey = key.replaceAll(Pattern.quote("?server"), "");
				try {
					Method method = server.getClass().getMethod(findkey);
					if (method != null) {
						try {
							Object obj = method.invoke(server);
							if (obj != null) {
								variables.put(key, String.valueOf(obj));
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			} else if (key.startsWith("?plugin")) {
				PluginDescriptionFile pdf = Main.pl.getDescription();
				String findkey = key.replaceAll(Pattern.quote("?plugin"), "");
				try {
					Method method = pdf.getClass().getMethod(findkey);
					if (method != null) {
						try {
							Object obj = method.invoke(pdf);
							if (obj != null) {
								variables.put(key, String.valueOf(obj));
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			} else if (key.startsWith("?permission")) {
				Permission perm = Main.permission;
				String findkey = key.replaceAll(Pattern.quote("?permission"), "");
				try {
					Method method = perm.getClass().getMethod(findkey);
					if (method != null) {
						try {
							Object obj = method.invoke(perm);
							if (obj != null) {
								variables.put(key, String.valueOf(obj));
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			} else if (key.startsWith("?economy")) {
				Economy eco = Main.economy;
				String findkey = key.replaceAll(Pattern.quote("?economy"), "");
				try {
					Method method = eco.getClass().getMethod(findkey);
					if (method != null) {
						try {
							Object obj = method.invoke(eco);
							if (obj != null) {
								variables.put(key, String.valueOf(obj));
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			} else if (key.startsWith("?chat")) {
				Chat chat = Main.chat;
				String findkey = key.replaceAll(Pattern.quote("?chat"), "");
				try {
					Method method = chat.getClass().getMethod(findkey);
					if (method != null) {
						try {
							Object obj = method.invoke(chat);
							if (obj != null) {
								variables.put(key, String.valueOf(obj));
							}
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				}
			} else {
				key = key.toLowerCase();
				variables.put("%" + key + "%", String.valueOf(Main.pl.getVariablesConfig().get("variables." + key)));
			}
		}
		PluginDescriptionFile pdf = Main.pl.getDescription();
		String serverversion = Bukkit.getServer().getVersion();
		int svindex = serverversion.indexOf('(');
		serverversion = serverversion.substring(svindex);
		serverversion = serverversion.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("MC:", "");
		variables.put("%serverversion%", serverversion);
		int shortsvindex = serverversion.lastIndexOf('.');
		String shortsv = serverversion.substring(0, shortsvindex);
		variables.put("%shortserverversion%", shortsv);
		variables.put("%version%", pdf.getVersion());
		variables.put("%plugin%", pdf.getName());
	}

	public static String apply(String s) {
		for (String key : variables.keySet()) {
			s = s.replaceAll(Pattern.quote(key), variables.get(key));
		}
		return s;
	}

	public static String applyPlayer(String s, UUID uid) {
		s = apply(s);
		if (uid != null) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(uid);
			s = s.replaceAll("%pname%", p.getName());
		} else {
			s = s.replaceAll("%pname%", "!LOGIN!");
		}
		return s;
	}
}
