package net.benjaminurquhart.legible;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class LegibleMV {
	
	private String[] switches = null, variables = null;
	private JSONArray common = null, actors = null, mapinfo = null, classes = null, skills = null, weapons = null, armor = null, states = null, items = null, troops = null;
	private JSONObject system = null;
	
	private List<JSONObject> maps;
	
	private File baseFolder;
	
	public LegibleMV(String gameFolder) {
		maps = new ArrayList<>();
		maps.add(null);
		baseFolder = new File(gameFolder);
		
		try {
			common = readJSONArray("CommonEvents");
			
			mapinfo = readJSONArray("MapInfos");
			classes = readJSONArray("Classes");
			weapons = readJSONArray("Weapons");
			states = readJSONArray("States");
			skills = readJSONArray("Skills");
			troops = readJSONArray("Troops");
			actors = readJSONArray("Actors");
			armor = readJSONArray("Armors");
			items = readJSONArray("Items");
			
			system = readJSONObject("System");
			switches = system.getJSONArray("switches").toList().toArray(String[]::new);
			variables = system.getJSONArray("variables").toList().toArray(String[]::new);
			
		}
		catch(Exception e) {
			throwUnchecked(e);
		}
	}
	
	public JSONObject common(int i) {
		Object obj = common.get(i);
		if(obj instanceof JSONObject event) {
			return event;
		}
		return null;
	}
	
	public JSONArray common() {
		return common;
	}
	
	public JSONArray actors() {
		return actors;
	}
	
	public JSONArray mapinfo() {
		return mapinfo;
	}
	
	public JSONArray classes() {
		return classes;
	}
	
	public JSONArray skills() {
		return skills;
	}
	
	public JSONArray weapons() {
		return weapons;
	}
	
	public JSONArray armor() {
		return armor;
	}
	
	public JSONArray states() {
		return states;
	}
	
	public JSONArray items() {
		return items;
	}
	
	public JSONArray troops() {
		return troops;
	}
	
	public JSONObject system() {
		return system;
	}

	
	public String[] variables() {
		return variables;
	}
	
	public String switches(int i) {
		return switches[i];
	}
	
	public String[] switches() {
		return switches;
	}
	
	public List<String> stringifyCommands(JSONArray commands, boolean colors) {
		return stringifyCommands(commands, -1, colors);
	}
	
	public List<String> stringifyCommands(JSONArray commands, int mapID, boolean colors) {
		ANSI.enable(colors);
		List<String> out = new ArrayList<>();
		int length = commands.length();
		String indentStr, cmdStr;
		int indentation;
		JSONObject cmd;
		for(int i = 0; i < length; i++) {
			cmd = commands.getJSONObject(i);
			cmdStr = stringifyCommand(cmd, mapID);
			if(cmdStr != null) {
				indentation = cmd.getInt("indent");
				if(cmd.getInt("code") == 0) {
					indentation--;
				}
				if(indentation > 0) {
					indentStr = "    ".repeat(indentation);
				}
				else {
					indentStr = "";
				}
				out.add(indentStr + cmdStr.replace("\n", "\n" + indentStr));
			}
		}
		return out;
	}
	
	public JSONArray readJSONArray(String file) throws Exception {
		return new JSONArray(Files.readString(Path.of(baseFolder.getAbsolutePath(), "www", "data", file + ".json")));
	}
	
	public JSONObject readJSONObject(String file) throws Exception {
		return new JSONObject(Files.readString(Path.of(baseFolder.getAbsolutePath(), "www", "data", file + ".json")));
	}
	
	public String getVariableFormatted(int index) {
		return String.format("#%04d %s", index, variables[index]);
	}
	
	public String getSwitchFormatted(int index) {
		return String.format("#%04d %s", index, switches[index]);
	}
	
	public static String formatSound(JSONObject data) {
		return String.format("%s (%d%%, %d, %d)", data.getString("name"), data.getInt("volume"), data.getInt("pitch"), data.getInt("pan"));
	}
	/*
	private static String parseChoice(String choice, boolean trim) {
		if(PARSE_CHOICE) {
			// TODO
		}
		return choice;
	}*/
	
	public JSONObject getMap(int index) {
		try {
			while(index > maps.size()) {
				maps.add(null);
			}
			JSONObject map;
			if(index < maps.size()) {
				map = maps.get(index);
				if(map == null) {
					map = readJSONObject(String.format("Map%03d", index));
					maps.set(index, map);
				}
			}
			else {
				map = readJSONObject(String.format("Map%03d", index));
				maps.add(map);
			}
			return map;
		}
		catch(Exception e) {
			throwUnchecked(e);
		}
		return null;
	}
	
	// Oh boy
	// There are several thousand lines dedicated to this in the game interpreter
	// so I expected this to get lengthy very quickly.
	private String stringifyCommand(JSONObject cmd, int mapID) {
		JSONArray params = cmd.getJSONArray("parameters");
		switch(cmd.getInt("code")) {
		
		// End
		case 0: return cmd.getInt("indent") == 0 ? "" : "}";
		
		// Text setup (face, index, background, position)
		case 101: {
			String portrait, background, position;
			if(params.getString(0).isEmpty()) {
				portrait = "None";
			}
			else {
				portrait = String.format("%s(%d)", params.getString(0), params.getInt(1));
			}
			
			switch(params.getInt(1)) {
			case 0: background = "Window"; break;
			case 1: background = "Dim"; break;
			case 2: background = "Transparent"; break;
			default: background = "BG_" + params.get(1);
			}
			
			switch(params.getInt(2)) {
			case 0: position = "Top"; break;
			case 1: position = "Middle"; break;
			case 2: position = "Bottom"; break;
			default: position = "POS_" + params.get(2);
			}
			
			return String.format("Text: %s, %s, %s", portrait, background, position);
		}
		
		// Text
		case 401: return "    : " + ANSI.CYAN + params.getString(0).replace("\n", "      \n");
		
		// Choice setup (choices...)
		case 102: return String.format("%sShow Choices: %s%s%s %s", 
				ANSI.PURPLE, 
				ANSI.CYAN, 
				params.getJSONArray(0).toList().stream().map(String::valueOf).collect(Collectors.joining(", ")),
				ANSI.RESET,
				params.toList().subList(1, params.length()) // TODO: figure out what these options are
		);
			
		// Choice
		case 402: return String.format("%sWhen %s%s%s {", ANSI.PURPLE, ANSI.CYAN, params.get(1), ANSI.RESET);
		
		// Choice end
		case 404: return null;
		
		// Comment
		case 108:
		case 408: {
			String comment = params.getString(0);
			if(comment.length() > 2 && (comment.indexOf("//") == 0 || comment.indexOf("\\\\") == 0)) {
				return comment;
			}
			return "// " + comment;
		}
		
		// If (type, key, misc...)
		case 111: {
			switch(params.getInt(0)) {
			// Switch
			case 0: return String.format("if %s is %s {", getSwitchFormatted(params.getInt(1)), params.getInt(2) == 0 ? "ON" : "OFF");
			// Variable
			case 1: {
				String left = getVariableFormatted(params.getInt(1)), operator, right;
				
				// 0 = static, 1 = variable
				if(params.getInt(2) == 0) {
					right = String.valueOf(params.get(3));
				}
				else {
					right = getVariableFormatted(params.getInt(3));
				}
				
				switch(params.getInt(4)) {
				case 0: operator = "=="; break;
				case 1: operator = ">="; break;
				case 2: operator = "<="; break;
				case 3: operator = ">";  break;
				case 4: operator = "<";  break;
				case 5: operator = "!="; break;
				default: operator = "OP_" + params.get(4);
				}
				
				return String.format("if %s %s %s {", left, operator, right);
			}
			// Actor
			case 4: {
				String actor = String.format("(%d) %s", params.getInt(1), actors.getJSONObject(params.getInt(1)).getString("name"));
				String condition;
				switch(params.getInt(2)) {
				case 0: condition = actor + " is in the party"; break;
				case 1: condition = String.format("Name of %s is %s", actor, params.get(3)); break;
				case 2: condition = String.format("Class of %s is (%d) %s", actor, params.getInt(3), classes.getJSONObject(params.getInt(3)).getString("name")); break;
				case 3: condition = String.format("%s has learned (%d) %s", actor, params.getInt(3), skills.getJSONObject(params.getInt(3)).getString("name")); break;
				case 4: condition = String.format("%s has equipped (%d) %s (Weapon)", actor, params.getInt(3), weapons.getJSONObject(params.getInt(3)).getString("name")); break;
				case 5: condition = String.format("%s has equipped (%d) %s (Armor)", actor, params.getInt(3), armor.getJSONObject(params.getInt(3)).getString("name")); break;
				case 6: condition = String.format("%s is affected by (%d) %s", actor, params.getInt(3), states.getJSONObject(params.getInt(3)).getString("name")); break;
				default: condition = String.format("COND_%d %s", params.getInt(2), params.get(3));
				}
				return String.format("if %s {", condition);
			}
			
			// Has item
			case 8: return String.format("if Party has (%d) %s ", params.getInt(1), items.getJSONObject(params.getInt(1)).getString("name"));
			
			// Button
			case 11: return String.format("if Button [%s] is pressed down {", params.getString(1).toUpperCase());
			
			// Eval
			case 12: return String.format("if eval('%s') {", params.get(1));
			
			default: return String.format("if %s { // TODO", params);
			}
		}
			
		// Else
		case 411: return "else {";
		
		// Loop
		case 112: return "Loop {";
		
		// Break Loop
		case 113: return "Break Loop";
		
		// End Loop
		case 413: return "Repeat Above";
		
		// Common Event
		case 117: return String.format("Common Event: (%d) %s", params.getInt(0), common.getJSONObject(params.getInt(0)).getString("name"));
		
		// Label
		case 118: return String.format("%sLabel: %s", ANSI.BLUE, params.get(0));
		
		// Change Switch (first, last, on/off)
		case 121: {
			String switches;
			if(params.getInt(0) == params.getInt(1)) {
				switches = getSwitchFormatted(params.getInt(0));
			}
			else {
				switches = String.format("#%04d..#%04d", params.get(0), params.get(1));
			}
			return String.format("Control Switches: %s = %s", switches, params.getInt(2) == 0 ? "ON" : "OFF");
		}
		
		// Change Variable (first, last, operation, source, value, [value2])
		case 122: {
			String left, operation, right;
			if(params.getInt(0) == params.getInt(1)) {
				left = getVariableFormatted(params.getInt(0));
			}
			else {
				left = String.format("#%04d..#%04d", params.get(0), params.get(1));
			}
			
			switch(params.getInt(2)) {
			case 0: operation = "="; break;
			case 1: operation = "+="; break;
			case 2: operation = "-="; break;
			default: operation = "OP_" + params.get(2);
			}
			
			switch(params.getInt(3)) {
			// Direct assignment
			case 0: right = String.valueOf(params.get(4)); break;
			// From variable
			case 1: right = variables[params.getInt(4)]; break;
			// Random
			case 2: right = String.format("Random: %d..%d", params.getInt(4), params.getInt(5)); break;
			// Eval
			case 4: right = String.format("eval: %s", params.get(4)); break;
			
			default: right = String.format("SOURCE_%s %s", params.get(3), params.toList().subList(4, params.length()));
			}
			
			return String.format("Control Variables: %s %s %s", left, operation, right);
			
		}
		
		// Control Self Switch
		case 123: return String.format("Control Self Switch: %s = %s", params.get(0), params.getInt(1) == 0 ? "ON" : "OFF");
		
		// Transfer Player (direct, map, x, y, direction, fade)
		case 201: {
			String map, x, y;
			if(params.getInt(0) == 0) {
				map = mapinfo.getJSONObject(params.getInt(1)).getString("name");
				x = String.valueOf(params.get(2));
				y = String.valueOf(params.get(3));
			}
			else {
				map = getVariableFormatted(params.getInt(1));
				x = getVariableFormatted(params.getInt(2));
				y = getVariableFormatted(params.getInt(3));
			}
			
			// TODO: direction and fade
			return String.format("Transfer Player: %s (%s, %s) (Direction: %s, Fade: %s) // TODO", map, x, y, params.get(4), params.get(5));
		}
		
		// Set Movement Route (target, {repeat, skippable, wait, [cmds]})
		case 205: {
			StringBuilder out = new StringBuilder("Set Movement Route: ");
			int target = params.getInt(0);
			if(target == -1) {
				out.append("Player");
			}
			else if(target == 0) {
				out.append("This Event");
			}
			else if(mapID > 0) {
				JSONArray events = getMap(mapID).getJSONArray("events");
				out.append(events.getJSONObject(target).getString("name"));
			}
			else {
				out.append(String.format("NOCTX_EV%03d", target));
			}
			
			JSONObject opts = params.getJSONObject(1);
			List<String> options = new ArrayList<>();
			if(opts.getBoolean("wait")) {
				options.add("Wait");
			}
			if(opts.getBoolean("skippable")) {
				options.add("Skip");
			}
			if(opts.getBoolean("repeat")) {
				options.add("Repeat");
			}
			
			if(!options.isEmpty()) {
				out.append(' ');
				out.append(options);
			}
			
			JSONObject move;
			JSONArray list = opts.getJSONArray("list");
			MovementCommand[] cmds = MovementCommand.values();
			MovementCommand current;
			for(int i = 0; i < list.length(); i++) {
				move = list.getJSONObject(i);
				current = cmds[move.getInt("code")];
				out.append("\n                  : ");
				out.append(current);
				
				// TODO: handle parameters correctly
				if(move.has("parameters")) {
					out.append(": ");
					if(current == MovementCommand.ROUTE_PLAY_SE) {
						out.append(formatSound(move.getJSONArray("parameters").getJSONObject(0)));
					}
					else {
						out.append(move.getJSONArray("parameters"));
					}
				}
			}
			return out.toString();
		}
		
		// Movement Command (handled above)
		case 505: return null;
		
		//Change Transparency (on/off)
		case 211: return String.format("Change Transparency: %s", params.getInt(0) == 0 ? "ON" : "OFF");
		
		// Change Player Followers (on/off)
		case 216: return String.format("Change Player Followers: %s", params.getInt(0) == 0 ? "ON" : "OFF");
		
		// Tint Screen ((RGBA?), speed, wait)
		case 223: return String.format(
				"Tint Screen: %s, %d frame%s %s",
				params.get(0), 
				params.get(1),
				params.getInt(1) == 1 ? "" : "s",
				params.getBoolean(2) ? "(Wait)" : ""
		);
		
		// Wait
		case 230: return String.format("Wait: %s frames", params.get(0)); 
		
		// Show picture (index, filename, origin, coordtype, x, y, xscale, yscale, alpha, blendmode)
		case 231: {
			String origin = params.getInt(2) == 1 ? "Center" : "Upper Left";
			String x, y, blend;
			
			// 0 = static, 1 = variable
			if(params.getInt(3) == 0) {
				x = String.valueOf(params.get(4));
				y = String.valueOf(params.get(5));
			}
			else {
				x = getVariableFormatted(params.getInt(4));
				y = getVariableFormatted(params.getInt(5));
			}
			
			switch(params.getInt(9)) {
			case 0: blend = "Normal"; break;
			case 1: blend = "Additive"; break;
			case 2: blend = "Multiply"; break;
			case 3: blend = "Screen"; break;
			default: blend = "BLEND_" + params.get(9);
			}
			
			return String.format(
					"Show Picture: #%d, %s, %s (%s, %s), (%d%%, %d%%), %d, %s",
					params.getInt(0),
					params.getString(1),
					origin,
					x,
					y,
					params.getInt(6),
					params.getInt(7),
					params.getInt(8),
					blend
			);
		}
		
		// Erase picture
		case 235: return String.format("Erase Picture: %s", params.get(0));
		
		// Play BGM
		case 241: return String.format("Play BGM: %s", formatSound(params.getJSONObject(0)));
		
		// Fadeout BGM
		case 242: return String.format("Fadeout BGM: %d seconds", params.getInt(0));
		
		// Play BGS
		case 245: return String.format("Play BGS: %s", formatSound(params.getJSONObject(0)));
		
		// Play SE
		case 250: return String.format("Play SE: %s", formatSound(params.getJSONObject(0)));
		
		// Recover All (direct, target)
		case 314: {
			String target;
			
			// 0 = static, 1 = variable
			if(params.getInt(0) == 1) {
				target = getVariableFormatted(params.getInt(1));
			}
			else if(params.getInt(1) == 0) {
				target = "Entire Party";
			}
			else {
				target = actors.getJSONObject(params.getInt(1)).getString("name");
			}
			return String.format("Recover All: %s", target);
		}
		
		case 340: return String.format("%sAbort Battle", ANSI.PINK);
		
		// Script - basically just eval
		case 355: return String.format("Script: %s", params.get(0));
		
		// Plugin Command
		case 356: return String.format("Plugin Command: %s", params.get(0));
		
		// <in file but unused(?)>
		case 412: return null;
			
		// Unknown
		default: return String.format("CMD_%d(%s)", cmd.getInt("code"), params);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwUnchecked(Throwable t) throws T {
	  throw (T) t;
	}
}
