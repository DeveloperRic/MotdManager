package com.rictacius.motdManager.listener;

import java.net.InetAddress;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.rictacius.motdManager.Main;

public class PlayerUUIDResolver implements Listener {
	private Main plugin = Main.pl;

	public PlayerUUIDResolver() {
	}

	@EventHandler
	public void onPing(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		InetAddress ip = p.getAddress().getAddress();
		plugin.getPlayersConfig().set("players." + ip.getHostAddress().replaceAll(Pattern.quote("."), "-"),
				p.getUniqueId().toString());
		plugin.savePlayersFile();
	}

	public static UUID resolveUUID(Player p) {
		String suid = Main.pl.getPlayersConfig().getString(
				"players." + p.getAddress().getAddress().getHostAddress().replaceAll(Pattern.quote("."), "-"));
		if (suid != null) {
			return UUID.fromString(suid);
		}
		return null;
	}
}
