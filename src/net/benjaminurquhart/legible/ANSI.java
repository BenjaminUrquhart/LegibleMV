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
	
	
	protected static boolean enabled = true;
	
	private static class ANSIWrapper {
		private final String str;
		
		ANSIWrapper(String str) {
			this.str = str;
		}
		
		@Override
		public String toString() {
			return ANSI.enabled ? str : "";
		}
	}
	
	public static final ANSIWrapper RESET = new ANSIWrapper("\u001b[0;1m");
	
	private final Color color;
	
	private ANSI(Color color) {
		this.color = color;
	}
	
	public static void enable(boolean enabled) {
		ANSI.enabled = enabled;
	}
	
	public static boolean enabled() {
		return enabled;
	}
	
	private static String toTrueColorImpl(Color color) {
		return String.format("\u001b[38;2;%d;%d;%dm", color.getRed(), color.getGreen(), color.getBlue());
	}
	
	public static String toTrueColor(Color color) {
		if((color.getRGB()&0xffffff) == 0xffffff) {
			return RESET.toString();
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
		return enabled ? toTrueColorImpl(color) : "";
	}
}
