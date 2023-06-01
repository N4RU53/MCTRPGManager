package mc.naru.mctrpgmanager;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import dev.sergiferry.playernpc.api.NPC;
import dev.sergiferry.playernpc.api.NPCLib;
import net.md_5.bungee.api.ChatColor;


/* todo
 * - show image or other way to express monster
 *  - MOB ENTITY WITH ABOVE TEXT
 * - titleraw on several sceans
 * - change display name and remove playername from board
 * - set commands permission
 */


public class Main extends JavaPlugin {
	
	
	
	public File configYml = new File(getDataFolder(), "config.yml");
    public File playerData = new File(getDataFolder(), "pieces.json");
    public File charaStat = new File(getDataFolder(), "character_stat.json");
    
	public List<CharaData> charaList = new ArrayList<>(); //character data list
    
    public FileConfiguration config;
    
    public ScoreboardManager scoreboardManager;
    public Scoreboard scoreboard;
    
    public Objective objective;
    
    public String n_prefix = ".";
    
    public Plugin plugin = this;
    
    
    
    
	@Override
    public void onEnable() {
		
		getLogger().info("MCTRPGManager が有効化されました");
        
		NPCLib.getInstance().registerPlugin(plugin);
        
        scoreboardManager = Bukkit.getScoreboardManager();
        
        //config.ymlが無かったら作成
        if (!configYml.exists()) {
            saveResource(configYml.getName(), false); 
        }        
        config = getConfig();  
        
        
        //pieces.jsonが無かったら作成
        if (!playerData.exists()) {
        	saveResource(playerData.getName(), false);
        }
        
        
        //character_stat.jsonの有無でcharaListの初期化内容を分岐
        if (!charaStat.exists()) {
            //pieces.jsonの内容でcharaListを初期化
            Path jsonPath = Paths.get("./" + getDataFolder() + "/" + playerData.getName());
        	InputJson inputJson = new InputJson();
            charaList = inputJson.initPD(jsonPath);
        } else {
        	addScheduler();
        }        
    }
    
	
	
	
	
    @Override
    public void onDisable() {
        getLogger().info("MCTRPGManager が無効化されました");
        Bukkit.getScheduler().cancelTasks(this);
    }
    
    
    
    
    
    
    public void addScheduler() {
    	//毎秒処理
    	new BukkitRunnable() {
            @Override
            public void run() {
            	genScoreBoard();
            	refreshCharList();
            }
        }.runTaskTimer(this, 0L, 20L);
    }
    
    
    
    
    
    
    
//======================================== COMMANDS ===========================================
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)  {
    	
    	String inactiveSessionMsg = "Error：セッションが開始されていません";	
    	
    	if (cmd.getName().equalsIgnoreCase("session")) {
    		if(args.length != 0) {
    			if (args[0].equalsIgnoreCase("start")) {
    				
    		        if (!charaStat.exists()) {
    		        	saveResource(charaStat.getName(), false); //character_stat.jsonが無かったら作成
    		        	updateStatJson();
    		        	broadCastMsg("セッション開始");
    		        	addScheduler();
                        
    		        } else broadCastMsg("既にセッションが開始されています\n先に終了して下さい");
    				
    			} else if (args[0].equalsIgnoreCase("end")) {
    				
    				if (charaStat.exists()) {
    		        	charaStat.delete(); //character_stat.jsonがあれば削除
    		        	
    		        	Bukkit.getScheduler().cancelTasks(this);
            			scoreboard = scoreboardManager.getNewScoreboard();
            			for (Player player : Bukkit.getOnlinePlayers()) {
            	            player.setScoreboard(scoreboard);
            	        }
            			
    		        	broadCastMsg("セッション終了");
    		        } else broadCastMsg(inactiveSessionMsg);
    			}
				return true;
    		} return false;
    	}
    	
    	
    		
    	
    	else if (cmd.getName().equalsIgnoreCase("registchar")) {
    		if(args.length != 0) {
    			if(charaStat.exists()) {
	    			try {
						updateCharaList(args[1], "playerName", null, args[0]);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
    			} else {
        			if (sender instanceof Player) {
        				Player player = (Player) sender;
        				player.sendMessage(inactiveSessionMsg);
            		} else {
            			getLogger().info(inactiveSessionMsg);
            		}
    			}
				broadCastMsg(args[0] + " を " + args[1] + " に設定しました");
				return true;
    		} return false;
    	}
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("removechar")) {
    		if(args.length != 0) {
    			if(charaStat.exists()) {
	    			try {
						updateCharaList(args[0], "playerName", null, null);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
    			} else {
        			if (sender instanceof Player) {
        				Player player = (Player) sender;
        				player.sendMessage(inactiveSessionMsg);
            		} else {
            			getLogger().info(inactiveSessionMsg);
            		}
    			}
				broadCastMsg(args[0] + "のプレイヤー情報を削除しました");
				return true;
    		} return false;
    	}
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("dice")) {
    		//セッションが開始されていれば
    		if(charaStat.exists()) {
    			if(args.length != 0) {
        			Map<Integer, Boolean> result = dice(args[0]);
        			for(Integer key : result.keySet()) {
        				if (result.get(key)) {
                			broadCastMsg("(" + args[0] + ")" + " > " + key);
                		} else return false;
                    }
        		} else return false;
    		} else {
    			if (sender instanceof Player) {
    				Player player = (Player) sender;
    				player.sendMessage(inactiveSessionMsg);
        		} else {
        			getLogger().info(inactiveSessionMsg);
        		}
    			return false;
    		}
            return true;
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("giveskillbook")) {
    		if (sender instanceof Player) {
				Player player = (Player) sender;
				giveCommandBook(player);
				return true;
    		} else {
    			return false;
    		}
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("secretdice")) {
    		//セッションが開始されていれば
    		if(charaStat.exists()) {
    			if(args.length != 0) {
        			Player player = (Player) sender;
        			Map<Integer, Boolean> result = dice(args[0]);
        			for(Integer key : result.keySet()) {
        				if (result.get(key)) {
        					player.sendMessage("Secret Dice == > (" + args[0] + ")" + " > " + key);
                			broadCastMsg("Secret Dice by Keeper");
                		} else return false;
                    }
        		} else return false;
    		} else {
    			if (sender instanceof Player) {
    				Player player = (Player) sender;
    				player.sendMessage(inactiveSessionMsg);
        		} else {
        			getLogger().info(inactiveSessionMsg);
        		}
    			return false;
    		}
    		return true;
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("tnpc")) {
    		if(args.length != 0) {
				if (args[0].equalsIgnoreCase("summon")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						Location loc = player.getLocation();
						try {
							summonNPC(loc, args[1]);
						} catch (IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					} else {
						broadCastMsg("このコマンドはプレイヤーが使用して下さい");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					removeNPC(args[1]);
				} else if (args[0].equalsIgnoreCase("register")) {
					try {
						return registNPC(args[1], args[2]);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
	    		} else {
	    			return false;
	    		}
    		} else return false;
    		return true;
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("updatestat")) {
    		//セッションが開始されていれば
    		if(charaStat.exists()) {
    			if(args.length != 0) {
        			if (args[1].equalsIgnoreCase("commands")) {
        				try {
    						return updateCharaList(args[0], args[1], args[2], args[3]);
    					} catch (IllegalArgumentException | IllegalAccessException e) {
    						e.printStackTrace();
    					}
        			} else {
        				try {
    						return updateCharaList(args[0], args[1], null, args[2]);
    					} catch (IllegalArgumentException | IllegalAccessException e) {
    						e.printStackTrace();
    					}
        			}
        		} else return false;
    		} else {
    			if (sender instanceof Player) {
    				Player player = (Player) sender;
    				player.sendMessage(inactiveSessionMsg);
        		} else {
        			getLogger().info(inactiveSessionMsg);
        		}
    			return false;
    		}
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("commanddice")) {
    		if (sender instanceof Player) {
    			Player player = (Player) sender;
    			if(args.length != 0) {
    				commandDice(player, args[0]);
    			} else return false;
    		} else return false;
    		return true;
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("hp")) {
    		Player player = (Player) sender;
    		String charName = whoIsChar(player);
    		if(args.length != 0) {
    			try {
					if (updateCharaList(charName, "current_hp", null, args[0])) {
						player.sendMessage("HPを " + args[0] + " に設定しました");
					} else return false;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
    		} else return false;
    		return true;
        }
    	
        
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("mp")) {
    		Player player = (Player) sender;
    		String charName = whoIsChar(player);
    		if(args.length != 0) {
    			try {
					if (updateCharaList(charName, "current_mp", null, args[0])) {
						player.sendMessage("MPを " + args[0] + " に設定しました");
					} else return false;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
    		} else return false;
    		return true;
        }
    	
    	
    	
    	
    	else if (cmd.getName().equalsIgnoreCase("san")) {
    		Player player = (Player) sender;
    		String charName = whoIsChar(player);
    		if(args.length != 0) {
    			try {
					if (updateCharaList(charName, "current_san", null, args[0])) {
						player.sendMessage("SAN値を " + args[0] + " に設定しました");
					} else return false;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
    		} else {
    			return checkSan(player);
    		}
    		return true;
        }
    	
    	
    	
    	
    	return false;
    }
    
    
    
    
    
    
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equalsIgnoreCase("trpginfo")){
            if (args.length == 1) {
                if (args[0].length() == 0) { 
                    return Arrays.asList("enable","disable");
                } else {
                	if("enable".startsWith(args[0])){
                        return Collections.singletonList("enable");
                    }
                    else if("disable".startsWith(args[0])){
                        return Collections.singletonList("disable");
                    }
                }
            }
            
            
            
        } else if(command.getName().equalsIgnoreCase("session")){
        	if (args.length == 1) {
        		if (args[0].length() == 0) { 
                    return Arrays.asList("start","end");
                } else {
                	if("start".startsWith(args[0])){
                        return Collections.singletonList("start");
                    }
                    else if("end".startsWith(args[0])){
                        return Collections.singletonList("end");
                    }
                }
        	}
        	
        	
        	
        	
        } else if(command.getName().equalsIgnoreCase("tnpc")){
        	List<String> npcID = getNpcID();
        	List<String> npcs = getNpcList();
        	if (args.length == 1) {
        		if (args[0].length() == 0) { 
                    return Arrays.asList("summon", "remove", "register");
                } else {
                	if("summon".startsWith(args[0])){
                        return Collections.singletonList("summon");
                    }
                    else if("remove".startsWith(args[0])){
                        return Collections.singletonList("remove");
                    }
                    else if("register".startsWith(args[0])){
                        return Collections.singletonList("register");
                    }
                }
        	} else if (args.length == 2) {
        		if ("summon".startsWith(args[0]) || "remove".startsWith(args[0])) {
        			if (args[1].length() == 0) { 
                        return npcID;
                    } else {
                    	for (String npc : npcID) {
                    		if(npc.startsWith(args[1])) {
                    			return Collections.singletonList(npc);
                    		}
                    	}
                    }
        		} 
        	} else if (args.length == 3) {
        		if ("register".startsWith(args[0])) {
        			if (args[2].length() == 0) { 
        				return npcs;
        			} else {
                    	for (String charName : npcs) {
                    		if(charName.startsWith(args[2])) {
                    			return Collections.singletonList(charName);
                    		}
                    	}
                    }
        		}
        	}
        	
        	
        	
            
        } else if(command.getName().equalsIgnoreCase("registchar")){
        	List<String> players = getOnlinePlayersList();
        	List<String> chars = getAllChars("char");
            if (args.length == 1) {
                if (args[0].length() == 0) { 
                    return players;
                } else {
                	for (String player : players) {
                		if(player.startsWith(args[0])) {
                			return Collections.singletonList(player);
                		}
                	}
                }
            } else if (args.length == 2) {
            	if (args[1].length() == 0) { 
                    return chars;
                } else {
                	for (String char_name : chars) {
                		if(char_name.startsWith(args[1])) {
                			return Collections.singletonList(char_name);
                		}
                	}
                }
            } 
            
            
            
            
        } else if(command.getName().equalsIgnoreCase("removechar")){
        	List<String> chars = getAllChars("char");
            if (args.length == 1) {
            	if (args[0].length() == 0) { 
                    return chars;
                } else {
                	for (String char_name : chars) {
                		if(char_name.startsWith(args[0])) {
                			return Collections.singletonList(char_name);
                		}
                	}
                }
            }
            
            
            
            
        } else if(command.getName().equalsIgnoreCase("updatestat")){
        	
        	List<String> chars = getActiveChars();
        	List<String> tags = getAllTags();
        	List<String> commands = getAllCommands();
        	
        	if (args.length == 1) {
        		if (args[0].length() == 0) { 
                    return chars;
                } else {
                	for (String char_name : chars) {
                		if(char_name.startsWith(args[0])) {
                			return Collections.singletonList(char_name);
                		}
                	}
                }
        	} else if (args.length == 2) {
        		if (args[1].length() == 0) { 
                    return tags;
                } else {
                	for (String tag : tags) {
                		if(tag.startsWith(args[0])) {
                			return Collections.singletonList(tag);
                		}
                	}
                }
        	} else if (args.length == 3) {
        		if (args[1].equalsIgnoreCase("commands")) {
        			if (args[2].length() == 0) { 
                        return commands;
                    } else {
                    	for (String current_command : commands) {
                    		if(current_command.startsWith(args[2])) {
                    			return Collections.singletonList(current_command);
                    		}
                    	}
                    }
        		}
        	}        		
        		
        	
        	
        		
    	} else if(command.getName().equalsIgnoreCase("commanddice")){

        	List<String> commands = getAllCommands();
        	
        	if (args.length == 1) {
        		if (args[0].length() == 0) { 
                    return commands;
                } else {
                	for (String now_command : commands) {
                		if(now_command.startsWith(args[0])) {
                			return Collections.singletonList(now_command);
                		}
                	}
                }
        	}
    	}        
        
        return super.onTabComplete(sender, command, alias, args);
    }
    
    
    
//==========================================================================================   
    
    
    
    
    
    
    
    
    //14lines
    public void giveCommandBook (Player player) {
    	
    	String[] content = new String[6];
        int page_cnt = 0, cnt14 = 0;
    	
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta)item.getItemMeta();
        meta.setAuthor("KP");
        meta.setDisplayName("技能リスト");
        
        if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if(Objects.equals(current_player.playerName, player.getName())) {
		    		Map<String, Integer> current_commands = current_player.commands;
		    		
    				for (String command : current_commands.keySet()) {
    					
    					String tag = command;
    					Integer val = current_commands.get(command);
    					
    					if (command.equals("STR") || command.equals("CON") || command.equals("POW") || command.equals("DEX") ||
		    				command.equals("APP") || command.equals("SIZ") || command.equals("INT") || command.equals("EDU")) {
    						
	    					val = val * 5;
	    					tag = tag + "×5";
		    			}
    					
    					if (cnt14 >= 14) {
    						page_cnt++;
    						cnt14 = 0;
    					}
    					if (cnt14 == 0) {
    						content[page_cnt] = "【" + tag + "】" + "CCB<=" + val + "\n";
    					} else content[page_cnt] += "【" + tag + "】" + "CCB<=" + val + "\n";
    					cnt14++;
    				}
		    	}
		    }
        }
        
        meta.addPage(content);
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
        player.sendMessage("技能リストを付与しました");
    }
    
    
    
    
    
    
    //オンラインのメンバー全員にメッセージ送信
    public void broadCastMsg (String msg) {
    	for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(
            	ChatColor.GRAY + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "MCTRPGManager: " +
                ChatColor.RESET + "" + ChatColor.GRAY + "" + ChatColor.ITALIC +  msg);
            getLogger().info(msg);
        }
    }
    
    
    
    
    
    
    //オンラインのメンバー全員にTitleRaw送信
    public void broadCastTitle ( String title, Integer fadeIn, Integer stay, Integer fadeOut ) {
    	for (Player player : Bukkit.getOnlinePlayers()) {
    		player.sendTitle(title, null, fadeIn, stay, fadeOut);
    	}
    }
    
    
    
    
    
    
    public List<String> getActiveChars() {
    	List<String> chars = new ArrayList<>();
    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	//プレイヤー紐づけのされているもののみ
		    	if (!current_player.playerName.equals("NULL")) {
		    		chars.add(current_player.name);
		    	}
		    }
        }
    	return chars;
    }
    
    
    
    
    
    
    public String whoIsChar(Player player) {
    	String charName = null;
    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if (Objects.equals(current_player.playerName, player.getName())) {
		    		charName = current_player.name;
		    	}
		    }
        }
    	return charName;
    }
    
    
    
    
    
    
    
    public List<String> getAllChars(String mode) {
    	
    	String kind = "";
    	
    	if (mode == "char") {
    		kind = "character";
    	} else {
    		kind = "npc";
    	}
    	
    	List<String> chars = new ArrayList<>();
    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if (current_player.kind.equals(kind)) {
		    		chars.add(current_player.name);
		    	}
		    }
        }
    	return chars;
    }
    
    
    
    
    
    
    public List<String> getOnlinePlayersList() {
    	List<String> players = new ArrayList<>();
    	for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
    	return players;
    }
    
    
    
    
    
    
    public List<String> getNpcID() {
    	List<String> ids = new ArrayList<>();

    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if (current_player.kind.equals("npc") && !current_player.playerName.equals("NULL")) {
		            ids.add(current_player.playerName);
		    	}
		    }
    	}
    	return ids;
    }
    
    
    
    
    
    
    public List<String> getNpcList() {
    	List<String> npcs = new ArrayList<>();

    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if (current_player.kind.equals("npc")) {
		            npcs.add(current_player.name);
		    	}
		    }
    	}
    	return npcs;
    }
    
    
    
    
    
    
    public List<String> getAllTags() {
    	List<String> tags = new ArrayList<>();
    	if(charaList != null) {
    		CharaData exPlayer = charaList.get(0);
	    	for (Field field : exPlayer.getClass().getDeclaredFields()) {
	    		if (!field.getName().equals("playerName")) {
		    		tags.add(field.getName());
	    		}
	    	}
	    }
    	return tags;
    }
    
    
    
    
    
    
    public List<String> getAllCommands() {
    	List<String> commands = new ArrayList<>();
    	if(charaList != null) {
	    	CharaData exPlayer = charaList.get(0);
			Map<String, Integer> commandMap = exPlayer.commands;
			for (String command : commandMap.keySet()) {
				commands.add(command);
			}
        }
    	return commands;
    }
    
    
    
    
    
    
    public Map<Integer, Boolean> dice(String dmsg) {
    	
    	Random rand = new Random();    	
    	String regex = "([1-9]?[0-9]?[0-9])([dD])([1-9]?[0-9]?[0-9])";
    	
    	Map<Integer, Boolean> result = new LinkedHashMap<Integer, Boolean>();
    	
    	if (dmsg.matches(regex)) {
    		Pattern p = Pattern.compile(regex);
    		Matcher m = p.matcher(dmsg);
    		
    		Integer res_num = 0;
    		if (m.find()) {
    			for (int i = 0; i < Integer.parseInt(m.group(1)); i++) {
    				res_num += rand.nextInt(Integer.parseInt(m.group(3)))+1;
    			}
    		}
    		result.put(res_num, true);
    	} else {
    		result.put(null, false);
    	}
    	return result;
    }
    
    
    
    
    
    
    public boolean checkSan(Player player) {
    	Map<Integer, Boolean> dice_val = dice("1d100");
    	for (Integer num : dice_val.keySet()) {
    		if (!dice_val.get(num)) {
    			return false;
    		} else {
		    	if(charaList != null) {
				    for (int i = 0; i < charaList.size(); i++) {
				    	CharaData current_player = charaList.get(i);
				    	if (current_player.playerName.equals(player.getName())) {
		    				String judge = null;
		    				if(num > current_player.current_san) {
		    					if(96 <= num) {
		    						judge = ChatColor.RED + "致命的失敗";
		    					} else judge = ChatColor.RED + "失敗";
		    				} else {
		    					if(num <= 5) {
		    						judge = ChatColor.BLUE + "決定的成功";
		    					} else if(num <= (current_player.current_san/5)) {
		    						judge = ChatColor.BLUE + "クリティカル";
		    					} else judge = ChatColor.BLUE + "成功";
		    				}
		    				
		    				for (Player p : Bukkit.getOnlinePlayers()) {
		    					p.sendMessage("［" + current_player.name + "］"  +
		    		    				         "CCB<=" + current_player.current_san + "【SAN値チェック】(1D100<=" + current_player.current_san + ")＞ " + num + "＞ " + judge);
		    		        }
		    				return true;
				    	}
				    }
		    		player.sendMessage("先にキャラクターを登録して下さい");
		    		return false;
		        }
    		} 
    	}
    	return true;
    }
    
    
    
    
    
    
    public void commandDice(Player player, String command) {
    	
    	Map<Integer, Boolean> dice_val = dice("1d100");
    	
    	for (Integer num : dice_val.keySet()) {
    		if(charaList != null) {
    		    for (int i = 0; i < charaList.size(); i++) {
    		    	CharaData current_player = charaList.get(i);
    		    	if(Objects.equals(current_player.playerName, player.getName())) {
    		    		Map<String, Integer> current_commands = current_player.commands;
    		    		for (String commandTag : current_commands.keySet()) {
    		    			if(commandTag.equals(command)) {
    		    				Integer commandNum = current_commands.get(commandTag);
    		    				
    		    				if (commandTag.equals("STR") || commandTag.equals("CON") || commandTag.equals("POW") || commandTag.equals("DEX") ||
        		    				commandTag.equals("APP") || commandTag.equals("SIZ") || commandTag.equals("INT") || commandTag.equals("EDU")) {
    		    					
    		    					commandNum = commandNum * 5;
    		    					commandTag = commandTag + "×5";
        		    			}
    		    				
    		    				String judge = null;
    		    				if(num > commandNum) {
    		    					if(96 <= num) {
    		    						judge = ChatColor.RED + "致命的失敗";
    		    					} else judge = ChatColor.RED + "失敗";
    		    				} else {
    		    					if(num <= 5) {
    		    						judge = ChatColor.BLUE + "決定的成功";
    		    					} else if(num <= (commandNum/5)) {
    		    						judge = ChatColor.BLUE + "クリティカル";
    		    					} else judge = ChatColor.BLUE + "成功";
    		    				}
    		    				
    		    				for (Player p : Bukkit.getOnlinePlayers()) {
    		    					p.sendMessage("［" + current_player.name + "］"  +
    		    		    				         "CCB<=" + commandNum + "【" + commandTag + "】(1D100<=" + commandNum + ")＞ " + num + "＞ " + judge);
    		    		        }
    		    			}
    		    		}
    		    	}
    		    }
    		}
		}
    }
    
    
    
    
    
    
    public boolean registNPC(String id, String charName) throws IllegalArgumentException, IllegalAccessException {
    	
    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	if (current_player.name.equals(charName) && current_player.kind.equals("npc")) {
		    		updateCharaList(charName, "playerName", null, id);
		    		getLogger().info(charName + " is setted " + id);
		    	}
		    }
    	} else getLogger().info("no char data");
    	return true;
    }
    
    
    
    
    
    
    
    public void summonNPC(Location loc, String id) throws IllegalArgumentException, IllegalAccessException {
    	
    	if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
	    		if (current_player.playerName.equals(id) && current_player.kind.equals("npc")) {
	    			NPC.Global npc = NPCLib.getInstance().generateGlobalNPC(plugin, id, loc);
		    		npc.setText("§l" + current_player.name, "§4[HP]§f" + current_player.current_hp + "/" + current_player.max_hp
    						                + "§9[MP]§f" + current_player.current_mp + "/" + current_player.max_mp
    						                + "§a[SAN]§f"+ current_player.current_san + "/" + current_player.max_san
    						    );
		    		if (!current_player.tex_val.equals("NULL") && !current_player.tex_sig.equals("NULL")) {
		    			npc.setSkin(current_player.tex_val, current_player.tex_sig);
		    		}
		    		npc.forceUpdate();
		    	}
		    }
    	}
    }
    
    
    
    
    
    
    
    @SuppressWarnings("deprecation")
	public void removeNPC( String id ) {
    	NPC.Global npc = NPCLib.getInstance().getGlobalNPC(plugin, id);
    	NPCLib.getInstance().removeGlobalNPC(npc);
    }
    
    
    
    
    
    
    public void refreshCharList () {
    	//character_stat.jsonの内容でcharaListを初期化
    	Path jsonPath = Paths.get("./" + getDataFolder() + "/" + charaStat.getName());
    	InputJson inputJson = new InputJson();
    	charaList = inputJson.updatePD(jsonPath);
    }
    
    
    
    
    
    
	@SuppressWarnings("deprecation")
	public void genScoreBoard() { //スコアボードの生成
    	
        scoreboard = scoreboardManager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("ObjectiveName", "dummy", "メンバーステータス");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    	
        if(charaList != null) {
        	int cnt = 1;
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		    	
		    	//プレイヤー紐づけのされているPCのみ
		    	if (!current_player.playerName.equals("NULL") && current_player.kind.equals("character")) {
		    		Score playerNameScore = objective.getScore(
			        		" " + n_prefix + ChatColor.YELLOW + current_player.name + ChatColor.WHITE + " (" + current_player.playerName + ")");
			        Score playerScore = objective.getScore(" " +
			            ChatColor.RED + "H" + ChatColor.WHITE + "[" + current_player.current_hp + "/" + current_player.max_hp + "]" +
			            ChatColor.BLUE + "M" + ChatColor.WHITE + "[" + current_player.current_mp + "/" + current_player.max_mp + "]" +
			            ChatColor.GREEN + "S" + ChatColor.WHITE + "[" + current_player.current_san + "/" + current_player.max_san + "] "
			        );
			        playerNameScore.setScore(cnt);
			        playerScore.setScore(cnt);
			        cnt++;
		    	}
		    }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
    }
    
	
	
	
	
	public boolean updateCharaList(String charName, String tag, String commandTag, String newValue) throws IllegalArgumentException, IllegalAccessException {
		if(charaList != null) {
		    for (int i = 0; i < charaList.size(); i++) {
		    	CharaData current_player = charaList.get(i);
		        
		    	if (charName.equals(current_player.name)) {
		    		
		    		for (Field field : current_player.getClass().getDeclaredFields()) {
		    			
			    		if (field.getName().equals(tag)) {
			    			if (tag.equals("commands")) {
			    				Map<String, Integer> current_commands = current_player.commands;
			    				for (String command : current_commands.keySet()) {
			    	    			if (command.equals(commandTag)) {
			    	    				current_commands.replace(command, Integer.parseInt(newValue));
			    	    			}
			    	    		}
			    			} else if (tag.equals("name")) {
			    				field.set(current_player, newValue);
			    			} else if (tag.equals("playerName")) {
			    				field.set(current_player, newValue);
			    			} else {
			    				Matcher valmat = Pattern.compile("(\\d{1,3})").matcher(newValue);
			    	    		if(valmat.find()) {
			    	    			field.set(current_player, Integer.parseInt(newValue));
			    	    		} else return false;
			    			}
			    		}
			    	}
		    		if (current_player.kind.equals("npc") && !current_player.playerName.equals("NULL")) {
		    			@SuppressWarnings("deprecation")
						NPC.Global npc = NPCLib.getInstance().getGlobalNPC(plugin, current_player.playerName);
		    			if (npc != null) {
		    				npc.setText("§l" + current_player.name, "§4[HP]§f" + current_player.current_hp + "/" + current_player.max_hp
					                + "§9[MP]§f" + current_player.current_mp + "/" + current_player.max_mp
					                + "§a[SAN]§f"+ current_player.current_san + "/" + current_player.max_san
		    						);
		    				npc.forceUpdate();
		    			}
		    		}
		    	}
		    }
        }
		updateStatJson();
		return true;
	}
	
	
    
	
	
    
    public void updateStatJson() {
    	
    	//character_stat.jsonが無ければ何もしない
		if (charaStat.exists()) {
			OutputJson outputJson = new OutputJson();
			outputJson.writeJson(charaList, charaStat);
        }
    }
    
    
}