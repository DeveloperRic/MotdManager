package com.rictacius.motdManager.commands;

import java.net.URL;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.rictacius.motdManager.Main;
import com.rictacius.motdManager.tasks.MOTD;
import com.rictacius.motdManager.utils.ErrorFile;
import com.rictacius.motdManager.utils.Loader;
import com.rictacius.motdManager.utils.PermCheck;

import net.md_5.bungee.api.ChatColor;

public class MOTDMCommand implements CommandExecutor {
	private Main plugin = Main.pl;

	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (!PermCheck.senderHasAccess(sender, "motdmanager.admin")) {
			sender.sendMessage(ChatColor.RED + "You do not have permission!");
			return true;
		}
		if (args.length < 1) {
			sendHelp(sender);
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				int errors = plugin.reloadAllConfigFiles();
				if (errors > 0) {
					sender.sendMessage(ChatColor.RED + "Error while reloading config! Check console!");
				} else {
					sender.sendMessage(ChatColor.GREEN + "Reloaded successfuly");
				}
			} else if (args[0].equalsIgnoreCase("dump")) {
				URL link = ErrorFile.writeError("MOTDManager Reqested Dump File");
				sender.sendMessage(ChatColor.GREEN + "ErrorDump file written successfuly!");
				sender.sendMessage(ChatColor.GOLD
						+ "A pastebin has been created for your dumpfile, you can find it here " + link.toString());
			} else {
				sendHelp(sender);
			}
		} else if (args[0].equalsIgnoreCase("show")) {
			if (args.length == 2) {
				String motd = args[1];
				ArrayList<MOTD> lines = Loader.getLines(motd);
				if (lines == null) {
					sender.sendMessage(ChatColor.RED + "That MOTD does not exist or is not loaded!");
					return true;
				}
				String text = MOTD.combine(lines.get(0), lines.get(1));
				sender.sendMessage(ChatColor.AQUA + "Current state of MOTD " + motd);
				sender.sendMessage(text);
			} else if (args.length >= 3) {
				String motd = args[1];
				String rank = args[2];
				ArrayList<MOTD> lines = Loader.getLines(motd);
				if (lines == null) {
					sender.sendMessage(ChatColor.RED + "That MOTD does not exist or is not loaded!");
					return true;
				}
				boolean found = false;
				for (String group : Main.permission.getGroups()) {
					if (group.equalsIgnoreCase(rank)) {
						found = true;
						break;
					}
				}
				if (found == false) {
					sender.sendMessage(ChatColor.RED + "That group does not exist!");
					return true;
				}
				if (!Main.permission.groupHas(plugin.getConfig().getString("perm-world"), rank,
						lines.get(0).getPermission())) {
					sender.sendMessage(ChatColor.RED + "That group does not have access to that motd!");
				}
				String text = MOTD.combine(lines.get(0), lines.get(1));
				sender.sendMessage(ChatColor.AQUA + "Current state of MOTD " + motd);
				sender.sendMessage(text);
			} else {
				sendHelp(sender);
			}
		} else {
			sendHelp(sender);
		}
		return true;
	}

	void sendHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.AQUA + "MOTDManager v" + plugin.getDescription().getVersion());
		sender.sendMessage(ChatColor.GREEN + "/motdmanager reload");
		sender.sendMessage(ChatColor.GREEN + "/motdmanager dump");
		sender.sendMessage(ChatColor.GREEN + "/motdmanager show <motd> [group]");
	}

}
