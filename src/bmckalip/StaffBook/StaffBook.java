package bmckalip.StaffBook;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;


public class StaffBook extends JavaPlugin implements Listener{
	Configuration config = getConfig();
	PluginDescriptionFile plugin;
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	private List<String> noteModeUsers = new ArrayList<String>();
	private List<String> offenseList = new ArrayList<String>();
	
	public void onEnable(){
		config.options().copyDefaults(true);
		saveConfig();
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		String command = cmd.getName();
		
		if(!(sender instanceof Player)){
			return true;
		}
		
		if(!sender.hasPermission("staffbook.commands")){
			sender.sendMessage(ChatColor.RED + "You do not have permission to do this command");
			return true;
		}
		//commands:
		//Command: notemode. used to set notemode to on or off as a toggle.
		if(command.equalsIgnoreCase("NM")){
			if(args.length == 0){
				if(!noteModeUsers.contains(sender.getName())){
					noteModeUsers.add(sender.getName());
					sender.sendMessage("NoteMode is now " + ChatColor.GREEN + "on");
				}else{
					noteModeUsers.remove(sender.getName());
					sender.sendMessage("NoteMode is now " + ChatColor.RED  + "off");
				}
			}else{
				sender.sendMessage("Usage: /nm");
			}
		//Command: log starts a manual log of a player. used when we want to add info to a player, but they havent logged in since
		//plugin instalation to get on the list.
		}else if(command.equalsIgnoreCase("log")){
			if(args.length == 1){
				sender.sendMessage(newPlayer(args[0]));
				return true;
			}else{
				sender.sendMessage("Usage: /log <player>");
			}
		//command: addnote. adds a note to a player. requires notemode.
		}else if(command.equalsIgnoreCase("addnote")){
			//requires notemote to execute this command
			if(!noteModeUsers.contains(sender.getName())){
				sender.sendMessage(ChatColor.RED + "You must be in note mode to use this command");
				sender.sendMessage("Usage: /NoteMode <on/off>");
				return true;
			}
			if(args.length >= 2){
				String note = "";
				for(int i = 1; i < args.length; i++){
					note += args[i] + " ";
				}
				note = note.trim();
				sender.sendMessage(addNote(args[0], note, sender.getName()));
				return true;
			}else{
				sender.sendMessage("Usage: /AddNote <player> <message>");
			}
		//command: delnote. removes a note from a player
		}else if(command.equalsIgnoreCase("delnote")){
			if(args.length == 2){
				sender.sendMessage(deleteNote(args[0], args[1]));
				return true;
			}else{
				sender.sendMessage("Usage: /DelNote <player> <id>");
			}
		//command: setstanding. sets the standing of a player to 1 of the 4 options. requires notemode.
		}else if(command.equalsIgnoreCase("setstanding")){
			//requires notemote
			if(!noteModeUsers.contains(sender.getName())){
				sender.sendMessage(ChatColor.RED + "You must be in note mode to use this command");
				sender.sendMessage("Usage: /NoteMode <on/off>");
				return true;
			}
			
			if(args.length == 2){
				sender.sendMessage(setStanding(args[0], args[1]));
				return true;
			}else{
				sender.sendMessage("Usage: /SetStanding <player> <Great/Good/Poor/Awful>");
			}
		//command: setage. sets the age of a player.
		}else if(command.equalsIgnoreCase("setAge")){
			if(args.length == 2){
				sender.sendMessage(setAge(args[0], args[1]));
				return true;
			}else{
				sender.sendMessage("Usage: /SetAge <player> <age>");
			}
		}else if(command.equalsIgnoreCase("getinfo")){
			if(args.length == 1){
				getInfo((Player)sender, args[0]);
				return true;
			}else{
				sender.sendMessage("Usage: /GetInfo <player>");
			}
		//command: nooblist.
			//no args: returns a list of all noobs and hopefuls
			//nooblist add player: manually adds a player to the nooblist, defaults rank to noob. used for current noobs.
			//nooblist add player <noob/hopeful>: manually adds a player to the nooblist, defining rank aswell. used for current noobs.
		}else if(command.equalsIgnoreCase("nooblist")){
			if(args.length == 0){
					getNoobList((Player)sender, 1);
				return true;
			}else if(args.length == 3 && args[0].equalsIgnoreCase("add") && (args[2].equalsIgnoreCase("noob") || args[2].equalsIgnoreCase("hopeful"))){
				if(!Bukkit.getServer().getOfflinePlayer(args[1]).equals(null) && Bukkit.getServer().getOfflinePlayer(args[1]).hasPlayedBefore()){
					sender.sendMessage(addNoob(args[1], args[2]));
				}else{
					sender.sendMessage("That player has not played here before. Maybe you mispelled something?");
				}
				return true;
			}else if(args.length == 2 && args[0].equalsIgnoreCase("add")){
				if(!Bukkit.getServer().getOfflinePlayer(args[1]).equals(null) && Bukkit.getServer().getOfflinePlayer(args[1]).hasPlayedBefore()){
					sender.sendMessage(addNoob(args[1], "Noob"));
				}else{
					sender.sendMessage("That player has not played here before. Maybe you mispelled something?");
				}
				return true;
			}else if(args.length == 2 && args[0].equalsIgnoreCase("remove")){
				sender.sendMessage(delNoob(args[1]));
				return true;
			}else if(args.length == 1 && args[0].matches("\\d+")){
				getNoobList((Player)sender, Integer.parseInt(args[0]));
			}else{
				//change usage
					sender.sendMessage("Usage: /NoobList <Add/Remove/Page> <Player> <Noob/Hopeful>");
			}
		}
		return true;
}
	
	@EventHandler
	public void cancelChat(AsyncPlayerChatEvent event){
		if(noteModeUsers.contains(event.getPlayer().getName())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Message not sent, please leave notemode first.");
			event.getPlayer().sendMessage("Usage: /NM");
		}
	}
	
	@EventHandler 
	public void preprocessCommands(PlayerCommandPreprocessEvent event){
		//if it's not manPromote or manDemote or ban, ignore.
		if(!(event.getMessage().toLowerCase().startsWith("/manpromote") || 
			 event.getMessage().toLowerCase().startsWith("/mandemote") || 
			 event.getMessage().toLowerCase().startsWith("/ban"))){
				return;
		}
		
		String[] args = event.getMessage().split(" ");
		
		if(args[0].equalsIgnoreCase("/manpromote") || args[0].equalsIgnoreCase("/mandemote")){
			if(args.length != 3){
				return;
			}
			
			if(args[2].equalsIgnoreCase("Builder") || 
			   args[2].equalsIgnoreCase("Noble") ||
			   args[2].equalsIgnoreCase("Expert") ||
			   args[2].equalsIgnoreCase("Veteran") ||
			   args[2].equalsIgnoreCase("Moderator") ||
			   args[2].equalsIgnoreCase("Admin") ||
			   args[2].equalsIgnoreCase("Owner") ){
				setRank(args[1], args[2]);
				delNoob(args[1]);
			}else if(args[2].equalsIgnoreCase("Default")){
				setRank(args[1], args[2]);
				addNoob(args[1], "noob");
			}else if(args[2].equalsIgnoreCase("Hopeful")){
				setRank(args[1], args[2]);
				addNoob(args[1], "noob");
			}
		}else if(args[0].equalsIgnoreCase("/ban")){
			if(args.length != 3){
				return;
			}
				delNoob(args[1]);
		}
	}
	
	@EventHandler
	public void logPvp(PlayerDeathEvent event){
		Player victim = event.getEntity().getPlayer();
		Player killer = event.getEntity().getKiller();
		
		if(!(killer instanceof Player && victim instanceof Player)){
			return;
		}
		if(!config.isSet("Players." + killer.getName().toLowerCase() + ".Rank")){
			return;
		}
		if(! (config.getString("Players." + killer.getName().toLowerCase() + ".Rank").equalsIgnoreCase("Noob") 
	      || config.getString("Players." + killer.getName().toLowerCase() + ".Rank").equalsIgnoreCase("Hopeful"))){
			return;
		}
		
			config.set("Players." + killer.getName().toLowerCase() + ".Kills",(config.getInt("Players." + killer.getName().toLowerCase() + ".Kills")) + 1);
			Date date = new Date();
			String selfDefence;
			if(!isSelfDefence(killer.getName(), victim.getName())){
				selfDefence = "not in";
				offenseList.remove(killer.getName() + victim.getName());
			}else{
				selfDefence = "in";
				offenseList.remove(victim.getName() + killer.getName());
			}
			
			String note = killer.getName() + " Killed " + victim.getName() + " at " + sdf.format(date) + ", EST. It was " + selfDefence + " self defense.";
			addNote(killer.getName().toLowerCase(), note, "Automated Note");
			saveConfig();
	}
	@EventHandler
	public void logHits(EntityDamageByEntityEvent event){
		
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player){
			Player damager = (Player)event.getDamager();
			Player victim = (Player)event.getEntity();
			
			if(isSelfDefence(damager.getName(), victim.getName())){
				return;
			}
			if(offenseList.contains(damager.getName() + victim.getName())){
				return;
			}
			
			offenseList.add(damager.getName() + victim.getName());
		}
	}
	
	public boolean isSelfDefence(String damager, String victim){
		
		if(offenseList.contains(victim + damager)){
			return true;
		}
		return false;
	}
	public String newPlayer(String player){
		if(config.getString("Players." + player.toLowerCase()) == null){
			config.createSection("Players." + player.toLowerCase());
			config.set("Players." + player.toLowerCase() + ".Standing", "Good");
			saveConfig();
			return "Now Logging " + player;
		}else{
			return player + " is already being logged!";
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void newPlayer(PlayerJoinEvent event){
		Player player = event.getPlayer();
		String playerName = player.getName().toLowerCase();
		Date date = new Date();
		
			if(config.getString("Players." + playerName.toLowerCase()) == null){
				config.createSection("Players." + playerName.toLowerCase());
			}
			if(config.getString("Players." + playerName + ".LastOnline") == null){
				config.set("Players." + playerName + ".LastOnline", sdf.format(date));
			}
			if(config.getString("Players." + playerName + ".Standing") == null){
				config.set("Players." + playerName + ".Standing", "Good");
			}
			if(!player.hasPlayedBefore()){
				addNoob(playerName, "Noob");
				if(config.getString("Players." + playerName + ".Rank") == null){
					config.set("Players." + playerName + ".Rank", "Noob");
			}
		}
			saveConfig();
	}
	@EventHandler(priority = EventPriority.LOW)
	public void setLastOnline(PlayerQuitEvent event){
		Player player = event.getPlayer();
			//knock them out of notemode if they are in it on logout.
			if(noteModeUsers.contains(player.getName().toLowerCase())){
				noteModeUsers.remove(player.getName().toLowerCase());
			}
			
			if(!config.isSet("Players." + player.getName().toLowerCase())){
				newPlayer(player.getName().toLowerCase());
			}
		Date date = new Date();
		config.set("Players." + event.getPlayer().getName().toLowerCase() + ".LastOnline", sdf.format(date));
		saveConfig();
	}
	
	public String setStanding(String player, String standing){
		if(!config.isSet("Players." + player.toLowerCase())){
			newPlayer(player);
		}
			if(
				standing.equalsIgnoreCase("Great") ||
				standing.equalsIgnoreCase("Good") || 
				standing.equalsIgnoreCase("Poor") || 
				standing.equalsIgnoreCase("Awful")
			){
				config.set("Players." + player + ".Standing", Character.toUpperCase(standing.charAt(0)) + standing.substring(1));
				saveConfig();
				return "Updated!";
			}else{
				return "Usage: /SetStanding <player> <Great/Good/Poor/Awful>";
			}
	}
	
	public void setRank(String player, String rank){
			if(!config.isSet("Players." + player.toLowerCase())){
				newPlayer(player.toLowerCase());
			}
		config.set("Players." + player.toLowerCase() + ".Rank", Character.toUpperCase(rank.charAt(0)) + rank.substring(1));
		saveConfig();
	}
	
	public String setAge(String player, String age){
			if(!config.isSet("Players." + player.toLowerCase())){
				newPlayer(player.toLowerCase());
			}
			if(!age.matches("\\d+")){
				return "Usage: /SetAge <player> <age>";
			}
		config.set("Players." + player.toLowerCase() + ".Age", Integer.parseInt(age));
		saveConfig();
		return player + "'s age has been updated";
	}
	
	public String addNote(String player, String message, String sender){
			if(!config.isSet("Players." + player.toLowerCase())){
				newPlayer(player.toLowerCase());
			}
		List<String> configList = (List<String>)config.getStringList("Players." + player.toLowerCase() + ".Notes");
		configList.add(message + " - " + sender);
		config.set("Players." + player.toLowerCase() + ".Notes", configList);
		saveConfig();
		return "Successfuly added Note to " + player + "!";
	}
	
	public String deleteNote(String player, String id){
		List<String> configList = (List<String>)config.getStringList("Players." + player.toLowerCase() + ".Notes");
		int messageID = Integer.parseInt(id);
			if(!id.matches("\\d+")){
				return "Usage: /DelNote <player> <id>";
			}
			if(!config.isSet("Players." + player.toLowerCase())){
				newPlayer(player.toLowerCase());
			}
			if(messageID <= configList.size() && messageID != 0){
				configList.remove(messageID - 1);
				config.set("Players." + player.toLowerCase() + ".Notes", configList);
				saveConfig();
				return "Successfully deleted note ";
			}else{
				return "Failed to delete note";
			}
	}
	
	public void getInfo(Player sender, String player){
		int messageNum = 1;
		String playerName = player.toLowerCase();
			if(!config.isSet("Players." + playerName)){
				sender.sendMessage("That player is not being logged. Did you mispell something?");
				return;
			}
		sender.sendMessage("");
		sender.sendMessage(ChatColor.GREEN + "Info for " + player + ":");
		sender.sendMessage(ChatColor.RED + "---------------");
			
			if(!config.isSet("Players." + player.toLowerCase() + ".LastOnline")){
				sender.sendMessage(ChatColor.YELLOW + "Last Online: " + ChatColor.WHITE + "Never");
			}else{
			sender.sendMessage(ChatColor.YELLOW + "Last Online: " + ChatColor.WHITE + config.getString("Players." + playerName + ".LastOnline"));
			}
			
			if(!config.isSet("Players." + player.toLowerCase() + ".Rank")){
				sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + "None");
			}else{
			sender.sendMessage(ChatColor.YELLOW + "Rank: " + ChatColor.WHITE + config.getString("Players." + playerName + ".Rank"));
			}
			
			if(!config.isSet("Players." + player.toLowerCase() + ".Age")){
				sender.sendMessage(ChatColor.YELLOW + "Age: " + ChatColor.WHITE + "Unknown");
			}else{
				if(config.getInt("Players." + playerName + ".Age") < 15){
					sender.sendMessage(ChatColor.YELLOW + "Age: " + ChatColor.RED + config.getInt("Players." + playerName + ".Age"));
				}else{
					sender.sendMessage(ChatColor.YELLOW + "Age: " + ChatColor.WHITE + config.getInt("Players." + playerName + ".Age"));
				}
			}
			
			if(!config.isSet("Players." + player.toLowerCase() + ".Kills")){
				sender.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + "None");
			}else{
				if(config.getInt("Players." + playerName + ".Kills") > 0){
					sender.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + "" + ChatColor.ITALIC + config.getInt("Players." + playerName + ".Kills"));
				}else if(config.getInt("Players." + playerName + ".Kills") >= 3){
					sender.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + "" + ChatColor.RED + config.getInt("Players." + playerName + ".Kills"));
				}else{
					sender.sendMessage(ChatColor.YELLOW + "Kills: " + ChatColor.WHITE + config.getInt("Players." + playerName + ".Kills"));
				}
			}
			
		sender.sendMessage(ChatColor.YELLOW + "Standing: " + ChatColor.WHITE + config.getString("Players." + playerName + ".Standing"));
        List<String> messages = config.getStringList("Players." + playerName + ".Notes");
        
	        if(messages.size() > 0){
	        	sender.sendMessage(ChatColor.YELLOW + "Notes:");
		        for (String s : messages){
		            sender.sendMessage("   " + ChatColor.GREEN + Integer.toString(messageNum) + ": "+ ChatColor.WHITE + s);
		            messageNum++;
		        }
	        }else{
	        	sender.sendMessage(ChatColor.YELLOW + "Notes: " + ChatColor.WHITE + "None");
	        }

	}

	public void getNoobList(Player sender, int page){
		//all the random spaces in strings is an attempt to format the list as a table, 
		//this is hard to do because spaces have a smaller width than characters.
		int messagesPerPage = 10;
		int messageNum = 1;
		int startSpot = (page * messagesPerPage) - messagesPerPage + 1;
		int endSpot = startSpot + 10;
		//1 weeks in seconds
		int offlineTime = 1 * 7 * 24 * 60 * 60;
		String name, rank, lastOnline, label, divider;
		ChatColor color;
		ChatColor color1 = ChatColor.WHITE;
        List<String> messages = config.getStringList("Noobs");
        
	        if(messages.size() > 0){
	        	//Header:
	        	sender.sendMessage(ChatColor.YELLOW +                             "                                 Noob List:                              ");
	        	sender.sendMessage(ChatColor.AQUA   + "" + ChatColor.UNDERLINE +  "                 Name              Rank          Last Online        ");
		        sender.sendMessage("");
		        //Messages:
	        	for (String playerName : messages){
	        		
		        	name = playerName;
		        	Long unixLastOnline = null;
		        	Long unixToday =  System.currentTimeMillis() / 1000L;
		        	int numOfSpaces = 22 - playerName.length();
		        	rank = config.getString("Players." + playerName + ".Rank");
		        	int kills = config.getInt("Players." + playerName + ".Kills");
		        	lastOnline = config.getString("Players." + playerName + ".LastOnline");
		        	label ="  " + Integer.toString(messageNum);
		        	divider = ChatColor.AQUA + " | ";
		        	
		    			if(!config.isSet("Players." + playerName.toLowerCase() + ".LastOnline")){
		    				lastOnline = "Never                   ";
		    			}else{
		    				try {
								unixLastOnline = sdf.parse(lastOnline).getTime() / 1000;
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    				lastOnline = config.getString("Players." + playerName + ".LastOnline");
		    			}
		    			
	    			//add formatting to various pieces of info
		        	if(config.isSet("Players." + playerName + ".Kills")){
			        	if(kills > 0 && kills < 3){ 
			        		color1 = ChatColor.ITALIC;
			        		name += "(" + kills + ")";
			        		numOfSpaces -= 5;
			        	}else if(config.getInt("Players." + playerName + ".Kills") > 2){ 
			        		color1 = ChatColor.RED;
			        		name += "(" + kills + ")";
			        		numOfSpaces -= 5;
			        	}else{ 
			        		color1 = ChatColor.WHITE;}
		        	}else{
		        		color1 = ChatColor.WHITE;
		        	}
		        		for(int i = 0; i <= numOfSpaces; i++) name += " ";
			        	if(config.isSet("Players." + playerName + ".Rank") && rank.length() == 4) rank += "    ";
			        	if(label.trim().length() == 1) label = " " + label;
			        	if(config.isSet("Players." + playerName + ".Rank") && config.getString("Players." + playerName + ".Rank").equalsIgnoreCase("noob")) color = ChatColor.YELLOW;
			        	else color = ChatColor.GOLD;
		        	//echo the table
		        	if(messageNum >= startSpot && messageNum < endSpot){
			        	if(unixToday - unixLastOnline < offlineTime){
				            sender.sendMessage(ChatColor.GREEN + label      + divider + 
				            				   ChatColor.WHITE + "" + color1          + name       + divider + 
				            				   color           + rank       + divider + 
				            				   ChatColor.WHITE + lastOnline + divider );
				        messageNum++;
			        	}
		        	}
		        }
	        }else{
	        	sender.sendMessage(ChatColor.YELLOW + "Noobs: " + ChatColor.WHITE + "None");
	        }
	        double maxPages = Math.ceil((double)messageNum / (double)messagesPerPage);
	        sender.sendMessage(ChatColor.AQUA  + "" + ChatColor.UNDERLINE + "Page " + page + " / " +(int)maxPages + "                                                   ");
	        sender.sendMessage("");

	}
	
	public String addNoob(String player, String rank){
			if(!config.isSet("Players." + player.toLowerCase())){
				newPlayer(player);
			}
			
		List<String> configList = (List<String>)config.getStringList("Noobs");
		
			if(configList.contains(player.toLowerCase())){
				if(config.getString("Players." + player.toLowerCase() + ".Rank") != Character.toUpperCase(rank.charAt(0)) + rank.substring(1)){
					config.set("Players." + player.toLowerCase() + ".Rank", Character.toUpperCase(rank.charAt(0)) + rank.substring(1));
					saveConfig();
					return player + " is already on noob List. Rank updated";
				}
				return player + " is already on noob List";
			}
			
		configList.add(player.toLowerCase());
		config.set("Noobs", configList);
		config.set("Players." + player.toLowerCase() + ".Rank", Character.toUpperCase(rank.charAt(0)) + rank.substring(1));
		saveConfig();
		return "Successfuly added " + player + " to Noob list";
	}
	
	public String delNoob(String player){
		List<String> configList = (List<String>)config.getStringList("Noobs");
			if(configList.contains(player.toLowerCase())){
				configList.remove(player.toLowerCase());
				config.set("Noobs", configList);
				saveConfig();
				return "Successfuly removed " + player + " from Noob list";
			}else{
				return player + " is not on the Noob list";
			}
	}
}