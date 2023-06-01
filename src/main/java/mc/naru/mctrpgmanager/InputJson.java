package mc.naru.mctrpgmanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



// pieces.jsonを読み込んで結果をList<PlayerData>で返す
public class InputJson {

	
	public List<CharaData> initPD( Path jsonPath ) {
		String json = localJson(jsonPath);
		if (json.length()>0) {
			return initParser(json);
		}
		return null;
	}
    
    
    
	
	public List<CharaData> updatePD( Path jsonPath ) {
		String json = localJson(jsonPath);
		if (json.length()>0) {
			return updateParser(json);
		}
		return null;
	}
	
	
	
	
    
    public String localJson( Path jsonPath ) {
        
		try (Stream<String> stream = Files.lines(jsonPath)) {

			StringBuilder sb = new StringBuilder();
			stream.forEach(sb::append);
			return sb.toString();
			
		} catch (IOException e) {
		}
		return null;
	}
    
    
    
    
    public List<CharaData> initParser(String json) {
    	
		List<CharaData> list = new ArrayList<>();
		
		GetCommandList getCommand = new GetCommandList();
		
		JsonArray pieces = (JsonArray) new Gson().fromJson(json, JsonArray.class);
		
		JsonObject current_player, data, hp, mp ,san;
		JsonArray status;

		
		for (int i = 0; i < pieces.size(); i++) {
			
			current_player = pieces.get(i).getAsJsonObject();
			data = current_player.get("data").getAsJsonObject();
			
			status = data.get("status").getAsJsonArray();
			
			hp = status.get(0).getAsJsonObject();
			mp = status.get(1).getAsJsonObject();
			san = status.get(2).getAsJsonObject();
		
			CharaData playerdata = new CharaData();
			
			playerdata.kind = current_player.get("kind").getAsString();
			
			playerdata.name = data.get("name").getAsString();
			
			playerdata.playerName = "NULL";
			
			playerdata.tex_val = "NULL";
			playerdata.tex_sig = "NULL";
			
			playerdata.commands = getCommand.get_commands(data.get("commands").getAsString());
			
			playerdata.current_hp = hp.get("value").getAsInt();
			playerdata.max_hp = hp.get("max").getAsInt();
			playerdata.current_mp = mp.get("value").getAsInt();
			playerdata.max_mp = mp.get("max").getAsInt();
			playerdata.current_san =san.get("value").getAsInt();
			playerdata.max_san = san.get("max").getAsInt();
			
			list.add(playerdata);
		}

		return list;
	}
    
    
    
    
    
    
    public List<CharaData> updateParser(String json) {
    	
		List<CharaData> list = new ArrayList<>();
		
		JsonArray pieces = (JsonArray) new Gson().fromJson(json, JsonArray.class);
		
		JsonObject current_player, commands;
		
		
		for (int i = 0; i < pieces.size(); i++) {
			
			Map<String, Integer> commandMap = new LinkedHashMap<String, Integer>();
			
			CharaData playerdata = new CharaData();
			
			current_player = pieces.get(i).getAsJsonObject();
			
			playerdata.kind = current_player.get("kind").getAsString();
			
			playerdata.name = current_player.get("name").getAsString();
			
			playerdata.playerName = current_player.get("playerName").getAsString();
			
			playerdata.tex_val = current_player.get("tex_val").getAsString();
			playerdata.tex_sig = current_player.get("tex_sig").getAsString();
			
			playerdata.current_hp = current_player.get("current_hp").getAsInt();
			playerdata.max_hp = current_player.get("max_hp").getAsInt();
			playerdata.current_mp = current_player.get("current_mp").getAsInt();
			playerdata.max_mp = current_player.get("max_mp").getAsInt();
			playerdata.current_san = current_player.get("current_san").getAsInt();
			playerdata.max_san = current_player.get("max_san").getAsInt();
			
			
			
			commands = current_player.get("commands").getAsJsonObject();
			
			for (String tag : commands.keySet()) {
	    		commandMap.put(tag, commands.get(tag).getAsInt());
	    	}
			
			playerdata.commands = commandMap;
			
			
			
			list.add(playerdata);
		}

		return list;
	}
}
