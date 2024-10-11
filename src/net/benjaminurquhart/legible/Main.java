package net.benjaminurquhart.legible;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
	
	private static final Pattern FORBIDDEN = Pattern.compile("[<>:\"/\\\\|?*]");
	
	//public static boolean COLORLESS = false, PRINT_SINGLE = false;

	public static void main(String[] args) throws Exception {
		/*
		if(!COLORLESS) {
			System.out.print(ANSI.RESET);
		}*/
		
		//"C:\\Program Files (x86)\\Steam\\steamapps\\common\\In Stars And Time"
		//"C:\\Program Files (x86)\\Steam\\steamapps\\content\\app_1677310\\depot_1677311"
		//"C:\\Users\\benja\\Downloads\\Dwellers Empty Path"
		
		/*
		if(PRINT_SINGLE) {
			
			JSONObject map = game.getMap(24);
			JSONArray event = map.getJSONArray("events").getJSONObject(1).getJSONArray("pages").getJSONObject(1).getJSONArray("list");
			
			game.stringifyCommands(event, 24, true).forEach(line -> System.out.printf("%s%s\n", line, ANSI.RESET));
			
			return;
		}*/
		
		
		// Dump In Stars And Time
		// Just like change these to whatever game(s)
		LegibleMV game = new LegibleMV("C:\\Program Files (x86)\\Steam\\steamapps\\common\\In Stars And Time");
		LegibleMV old = new LegibleMV("C:\\Program Files (x86)\\Steam\\steamapps\\content\\app_1677310\\depot_1677311");
		dump(game, new File("1.0.6.3"));
		dump(old, new File("1.0.3"));
	}
	
	private static void dump(LegibleMV game, File base) throws Exception {
		
		if(!base.exists()) {
			base.mkdirs();
		}
		
		File common = new File(base, "common");
		if(!common.exists()) {
			common.mkdirs();
		}
		
		File mapFolder = new File(base, "map");
		if(!mapFolder.exists()) {
			mapFolder.mkdirs();
		}
		
		List<String> lines = new ArrayList<>();
		int length = game.common().length();
		String trigger;
		int triggerVal;
		for(int i = 0; i < length; i++) {
			lines.clear();
			if(game.common(i) instanceof JSONObject event) {
				
				try {
					System.out.printf("%d %s\n", i, event.getString("name"));
					lines.add(event.getString("name"));
					
					triggerVal = event.getInt("trigger");
					switch(triggerVal) {
					case 0: trigger = "None"; break;
					case 1: trigger = "Autorun"; break;
					case 2: trigger = "Parallel"; break;
					default: trigger = "UNKNOWN_" + triggerVal;
					}
					if(triggerVal > 0) {
						int switchId = event.optInt("switch", event.getInt("switchId"));
						trigger = String.format("%s (%d %s)", trigger, switchId, game.switches(switchId));
					}
					lines.add("Trigger: " + trigger);
					lines.add("");
					
					JSONArray commands = event.getJSONArray("list");
					lines.addAll(game.stringifyCommands(commands, false));
				}
				catch(Exception e) {
					e.printStackTrace();
					lines.add("");
					lines.add("ERROR " + e);
					lines.add(event.toString());
				}
				
				Files.write(Path.of(common.getAbsolutePath(), String.format("CE%s_%s.txt", i, FORBIDDEN.matcher(event.getString("name")).replaceAll("_"))), lines);
			}
		}
		
		JSONArray infos = game.mapinfo();
		JSONObject page, map;
		JSONArray events, pages;
		File mapEventFolder;
		for(int i = 1; i < infos.length(); i++) {
			if(infos.get(i) instanceof JSONObject info) {
				map = game.getMap(i);
				events = map.getJSONArray("events");
				mapEventFolder = new File(mapFolder, String.format("%d_%s", i, FORBIDDEN.matcher(info.getString("name")).replaceAll("_")));
				if(!mapEventFolder.exists()) {
					mapEventFolder.mkdirs();
				}
				System.out.printf("%d %s\n", i, info.getString("name"));
				for(int j = 1; j < events.length(); j++) {
					if(events.get(j) instanceof JSONObject event) {
						pages = event.getJSONArray("pages");
						for(int k = 0; k < pages.length(); k++) {
							page = pages.getJSONObject(k);
							
							// TODO: write event conditions and other parameters
							Files.write(
									Path.of(
											mapEventFolder.getAbsolutePath(), 
											String.format("EV%03d_%s_%d.txt", j, FORBIDDEN.matcher(event.getString("name")).replaceAll("_"), k)
									), 
									game.stringifyCommands(page.getJSONArray("list"), i, false)
							);
						}
					}
				}
			}
		}
	}
}
