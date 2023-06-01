package mc.naru.mctrpgmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class OutputJson {
	
	public void writeJson(List<CharaData> playerList, File file) {
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		String pljson = gson.toJson(playerList); // not include SAN => null 
		
		FileWriter filewriter;
		try {
			filewriter = new FileWriter(file, false);
			filewriter.write(pljson);
		    filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
