package mc.naru.mctrpgmanager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetCommandList {
	
	/* for debug
	 * 
	 
	public static void main(String[] args){
	    String com = "1d100<={SAN} 【SAN値チェック】\nCCB<=70 【アイデア】\nCCB<=30 【幸運】\nCCB<=100 【知識】\nCCB<=25 【目星】\nCCB<=25 【図書館】\nCCB<=75 【聞き耳】\nCCB<=52 【回避】\nCCB<=50 【こぶし（パンチ）】\nCCB<=25 【キック】\nCCB<=25 【組み付き】\nCCB<=10 【頭突き】\nCCB<=25 【投擲】\nCCB<=1 【マーシャルアーツ】\nCCB<=20 【拳銃】\nCCB<=15 【サブマシンガン】\nCCB<=30 【ショットガン】\nCCB<=15 【マシンガン】\nCCB<=25 【ライフル】\nCCB<=30 【応急手当】\nCCB<=1 【鍵開け】\nCCB<=15 【隠す】\nCCB<=10 【隠れる】\nCCB<=10 【忍び歩き】\nCCB<=10 【写真術】\nCCB<=1 【精神分析】\nCCB<=10 【追跡】\nCCB<=40 【登攀】\nCCB<=20 【運転】\nCCB<=20 【機械修理】\nCCB<=1 【重機械操作】\nCCB<=5 【乗馬】\nCCB<=25 【水泳】\nCCB<=5 【製作】\nCCB<=1 【操縦】\nCCB<=25 【跳躍】\nCCB<=10 【電気修理】\nCCB<=10 【ナビゲート】\nCCB<=1 【変装】\nCCB<=99 【言いくるめ】\nCCB<=75 【信用】\nCCB<=85 【説得】\nCCB<=5 【値切り】\nCCB<=100 【母国語】\nCCB<=75 【医学】\nCCB<=5 【オカルト】\nCCB<=1 【化学】\nCCB<=0 【クトゥルフ神話】\nCCB<=85 【芸術(音楽)】\nCCB<=10 【経理】\nCCB<=1 【考古学】\nCCB<=66 【コンピューター】\nCCB<=25 【心理学】\nCCB<=1 【人類学】\nCCB<=1 【生物学】\nCCB<=1 【地質学】\nCCB<=1 【電子工学】\nCCB<=1 【天文学】\nCCB<=10 【博物学】\nCCB<=1 【物理学】\nCCB<=5 【法律】\nCCB<=1 【薬学】\nCCB<=20 【歴史】\n1d3+0 【ダメージ判定】\n1d4+0 【ダメージ判定】\n1d6+0 【ダメージ判定】\nCCB<=9*5 【STR × 5】\nCCB<=11*5 【CON × 5】\nCCB<=6*5 【POW × 5】\nCCB<=11*5 【DEX × 5】\nCCB<=10*5 【APP × 5】\nCCB<=14*5 【SIZ × 5】\nCCB<=14*5 【INT × 5】\nCCB<=20*5 【EDU × 5】\n";
	    Map<String, Integer> commap = get_commands(com);
	    
	    for (String key: commap.keySet()) {
	    	System.out.println(key + ": " + commap.get(key));
	    }
	}
*/
	
	
	public Map<String, Integer> get_commands(String command_str) {
	    
	    String[] commands;
	    
	    String tag = null;
	    Integer value = null;
	    
	    Map<String, Integer> player_commands = new LinkedHashMap<String, Integer>();
	    
    	commands = command_str.split("\n");
    	
    	for(String command : commands) {		    		
    		Matcher tags = Pattern.compile("【([^×]+)】").matcher(command);
    		Matcher tags_stat = Pattern.compile("【(.+?)( × )(\\d{1,3})】").matcher(command);
    		if(tags.find()) {
    			tag = tags.group(1);
    		} else if (tags_stat.find()) {
    			tag = tags_stat.group(1);
    		}
    		Matcher values = Pattern.compile("=(\\d{1,3})").matcher(command); // SAN's val = null
    		if(values.find()) {
    			value = Integer.parseInt(values.group(1));
    		}
			player_commands.put(tag, value);
    	}
	    return player_commands; 
	}
}
