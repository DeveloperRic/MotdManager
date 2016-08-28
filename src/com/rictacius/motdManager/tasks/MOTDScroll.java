package com.rictacius.motdManager.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.rictacius.motdManager.Main;

public class MOTDScroll extends MOTD {
	private boolean restart;
	private int task;
	private int place;
	private String original;
	private long speed;

	public MOTDScroll(String text, String label, boolean isTop, boolean restart, long speed) {
		super(text, label, isTop);
		this.original = text;
		this.restart = restart;
		this.place = 0;
		this.speed = speed;
		type = "Scroll";
		runScroll();
	}

	public void runScroll() {
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {
			public void run() {
				if (place > text.length()) {
					place = 0;
				}
				String colour = "";
				if (place > 0) {
					colour = ChatColor.getLastColors(original.substring(0, place - 1));
					text = colour + original.substring(place);
				} else {
					text = original;
				}
				if (text.length() < 50) {
					if (restart) {
						int difference = 50 - text.length();
						text = text + " " + original.substring(0, difference);
					}
				}
				place++;
			}
		}, 0L, speed);

	}

	public void cancelScroll() {
		Bukkit.getScheduler().cancelTask(task);
	}
}
