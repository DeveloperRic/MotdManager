package com.rictacius.motdManager.tasks;

import java.util.UUID;

import org.bukkit.ChatColor;

import com.rictacius.motdManager.utils.Variables;

public class MOTD {
	protected String text;
	protected String perm;
	protected String label;
	protected boolean isTop;
	protected String type;

	public MOTD(String text, String label, boolean isTop) {
		this.text = text;
		this.label = label;
		this.perm = "motdmanager.motd." + label;
		this.isTop = isTop;
	}

	public static String combine(MOTD top, MOTD bottom) {
		String send = top.getMOTD() + ChatColor.RESET + "\n" + bottom.getMOTD();
		return send;
	}

	public static String combine(UUID uid, MOTD top, MOTD bottom) {
		String send = top.getMOTD(uid) + ChatColor.RESET + "\n" + bottom.getMOTD(uid);
		return send;
	}

	public String getMOTD() {
		String send = ChatColor.translateAlternateColorCodes('&', text);
		if (send.length() > 50) {
			send = send.substring(0, 50);
		}
		return send;
	}

	public String getMOTDApplied() {
		String send = Variables.apply(ChatColor.translateAlternateColorCodes('&', text));
		if (send.length() > 50) {
			send = send.substring(0, 50);
		}
		return send;
	}

	public String getMOTD(UUID uid) {
		String send = Variables.applyPlayer(ChatColor.translateAlternateColorCodes('&', text), uid);
		if (send.length() > 50) {
			send = send.substring(0, 50);
		}
		return send;
	}

	public String getLabel() {
		return label;
	}

	public String getPermission() {
		return perm;
	}

	public boolean isTop() {
		return isTop;
	}

	public String getType() {
		return type;
	}
}
