package com.rictacius.motdManager.tasks;

public class MOTDTextOnly extends MOTD {

	public MOTDTextOnly(String text, String label, boolean isTop) {
		super(text, label, isTop);
		type = "Text";
	}
}
