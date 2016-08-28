package com.rictacius.motdManager.tasks;

import org.bukkit.Bukkit;

import com.rictacius.motdManager.Main;

public class MOTDStrobe extends MOTD {
	private long delay;
	private int task;
	private String primary;
	private String secondary;
	private boolean isSecondary;

	public MOTDStrobe(String text, String label, boolean isTop, long delay, String secondary) {
		super(text, label, isTop);
		this.primary = text;
		this.secondary = secondary;
		this.delay = delay;
		isSecondary = false;
		type = "Strobe";
		runStrobe();
	}

	public void runStrobe() {
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.pl, new Runnable() {
			public void run() {
				if (isSecondary) {
					text = secondary;
					isSecondary = false;
				} else {
					text = primary;
					isSecondary = true;
				}
			}
		}, 0L, delay);
	}

	public void cancelStrobe() {
		Bukkit.getScheduler().cancelTask(task);
	}
}
