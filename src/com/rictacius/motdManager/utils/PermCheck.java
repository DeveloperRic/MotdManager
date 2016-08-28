package com.rictacius.motdManager.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.rictacius.motdManager.Main;

import net.milkbowl.vault.permission.Permission;

public class PermCheck {
	private static Permission permset = Main.permission;

	public static boolean hasPerm(Player player, String perm) {
		if (player.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		String build = "";
		for (int i = 0; i < parts.length; i++) {
			build = build + parts[i] + ".";
			if (permset.playerHas(player, build + "*")) {
				return true;
			}
		}
		if (permset.playerHas(player, perm)) {
			return true;
		}
		return false;
	}

	public static boolean hasAccess(Player player, String perm) {
		if (player.isOp()) {
			return true;
		}
		if (player.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split(".");
		String build = "";
		for (int i = 0; i < parts.length; i++) {
			build = build + parts[i] + ".";
			if (player.hasPermission(build + "*")) {
				return true;
			}
		}
		if (player.hasPermission(perm)) {
			return true;
		}
		return false;
	}

	public static boolean senderHasAccess(CommandSender sender, String perm) {
		if (sender.isOp()) {
			return true;
		}
		if (sender instanceof Player) {
			if (hasPerm((Player) sender, perm)) {
				return true;
			}
		}
		if (sender.hasPermission("*")) {
			return true;
		}
		String[] parts = perm.split("\\.");
		String build = "";
		for (String bit : parts) {
			build = build + bit + ".";
			String temp = build + "*";
			if (sender.hasPermission(temp)) {
				return true;
			}
		}
		if (sender.hasPermission(perm)) {
			return true;
		}
		return false;
	}
}
