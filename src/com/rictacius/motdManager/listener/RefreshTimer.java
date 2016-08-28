package com.rictacius.motdManager.listener;

import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.rictacius.motdManager.Main;
import com.rictacius.motdManager.tasks.MOTD;
import io.netty.channel.ChannelException;

public class RefreshTimer {
	private int task;
	private int count;
	private int timeout;
	private WrappedServerPing ping;
	private MOTD top;
	private MOTD bottom;
	private Player player;

	public RefreshTimer(WrappedServerPing ping, MOTD top, MOTD bottom, Player player) {
		this.ping = ping;
		this.top = top;
		this.bottom = bottom;
		this.player = player;
		timeout = Integer.parseInt(Main.pl.getConfig().getString("refresh-timeout"));
		count = 0;
		runRefreshTimer();
	}

	@SuppressWarnings("deprecation")
	public void runRefreshTimer() {
		task = Bukkit.getScheduler().scheduleAsyncRepeatingTask(Main.pl, new Runnable() {
			public void run() {
				if (count > 0) {
					cancelTask();
					return;
				}
				if (count > timeout) {
					cancelTask();
					return;
				}
				String motd = MOTD.combine(PlayerUUIDResolver.resolveUUID(player), top, bottom);
				PacketContainer info = ProtocolLibrary.getProtocolManager()
						.createPacket(PacketType.Status.Server.SERVER_INFO);
				ping.setMotD(motd);
				try {
					info.getServerPings().write(0, ping);
					ProtocolLibrary.getProtocolManager().sendServerPacket(player, info, false);
				} catch (InvocationTargetException | ChannelException e) {
					cancelTask();
					destroyTimer();
					e.printStackTrace();
				}
				count++;
			}
		}, 0L, 1L);
	}

	public void cancelTask() {
		Bukkit.getScheduler().cancelTask(task);
	}

	public void destroyTimer() {
		cancelTask();
		ping = null;
		top = null;
		bottom = null;
		player = null;
	}
}
