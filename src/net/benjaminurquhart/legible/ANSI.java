package net.benjaminurquhart.legible;

import java.awt.Color;

public enum ANSI {
	
	RED(Color.RED),
	GRAY(Color.GRAY),
	CYAN(Color.CYAN),
	BLUE(Color.BLUE),
	PINK(Color.PINK),
	GREEN(Color.GREEN),
	BLACK(Color.BLACK),
	WHITE(Color.WHITE),
	YELLOW(Color.YELLOW),
	LIGHT_GRAY(Color.LIGHT_GRAY),
	
	PURPLE(new Color(0x77279e));
	
	public static final String RESET = "\u001b[0;1m";
	
	private final Color color;
	
	private ANSI(Color color) {
		this.color = color;
	}
	private static String toTrueColorImpl(Color color) {
		return String.format("\u001b[38;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
	}
	public static String toTrueColor(Color color) {
		if((color.getRGB()&0xffffff) == 0xffffff) {
			return RESET;
		}
		return toTrueColorImpl(color);
	}
	public static String toTrueColorBackground(Color color) {
		return String.format("\u001b[48;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
	}
	public Color getColor() {
		return color;
	}
	@Override
	public String toString() {
		return toTrueColorImpl(color);
	}
}
