package com.exclnetworks.chatMentionsPro;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.*;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.ArrayList;

import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Main extends JavaPlugin implements Listener {

    private FileConfiguration config = getConfig();

    /*
        final int MAX_MENTIONS = 2;
        final Permission ADMIN = new Permission("skytags.admin");
        final Permission USETAG = new Permission("skytags.use");
        final ChatColor ALERT_COLOR = ChatColor.RED;
        final ChatColor MENTION_COLOR = ChatColor.AQUA;
        final String ALERT_PREFIX = ChatColor.LIGHT_PURPLE + "[SkyPvp Tags] ";
        */
    private int MAX_MENTIONS;
    private Permission ADMIN;
    private Permission USETAG;
    private String ALERT_COLOR;
    private String MENTION_COLOR;
    private String ALERT_PREFIX;
    private ArrayList<Player> punishedPlayers = new ArrayList<>();
    private ArrayList<Player> optedOutPlayers = new ArrayList<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this); //Register the listener
        setupConfig();
        System.out.println(ChatColor.RED + "ChatMentionsPro ENABLING...");

    }

    @Override
    public void onDisable() {
        System.out.println(ChatColor.RED + "ChatMentionsPro Disabling...");
    }


    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        String tailoredMessage = "";
        ChatColor playerChatColor = getColor(message);
        String[] splitMessage = message.split(" ");
        ArrayList<Player> mentionedPlayers = new ArrayList<>();
        if (event.getPlayer().hasPermission(USETAG)) {

            for (int i = 0; i < splitMessage.length; i++) {
                for (Player playerToMention : Bukkit.getServer().getOnlinePlayers()) { // Verify if player is online and valid
                    if (playerToMention.getName().equalsIgnoreCase(splitMessage[i]) && !splitMessage[i].equalsIgnoreCase(event.getPlayer().getName())) { // Stop duplicate names, self-tagging and ensure the user matches the tag.
                        if (!punishedPlayers.contains(event.getPlayer()) && !optedOutPlayers.contains(playerToMention) || event.getPlayer().hasPermission(ADMIN)) {
                            if (!mentionedPlayers.contains(Bukkit.getPlayer(splitMessage[i])))
                                splitMessage[i] = MENTION_COLOR + splitMessage[i] + playerChatColor; // Only change color for one of each name
                            mentionedPlayers.add(Bukkit.getPlayer(ChatColor.stripColor(splitMessage[i]))); //Add the player to list of mentioned players
                        }

                    }

                }
                tailoredMessage += splitMessage[i] + " ";
            }


            if (mentionedPlayers.size() <= MAX_MENTIONS) {
                for (int z = 0; z < mentionedPlayers.size(); z++) {
                    Player pl = mentionedPlayers.get(z);
                    pl.playSound(pl.getLocation(), Sound.LEVEL_UP, 1, 1);

                }
            } else if (mentionedPlayers.size() > MAX_MENTIONS) { //Avoid spam tagging
                event.getPlayer().sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Sorry but you can only mention up to " + MAX_MENTIONS + " players. :(");
                event.setCancelled(true); //Cancel the send to chat event
                event.isCancelled();
            }
        } else tailoredMessage = event.getMessage();
        event.setMessage(tailoredMessage);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        int length = args.length;
        boolean succsess = false;
        if (cmd.getName().equalsIgnoreCase("chattags")) {
            if (length == 0) { //If it is the barebones command send info message
                player.sendMessage(ChatColor.STRIKETHROUGH + "----------------------------------------------");
                player.sendMessage(ChatColor.BLUE + "You can use the following commands");
                player.sendMessage(ChatColor.BLUE + "/ct off : Opts you out of chattagging");
                player.sendMessage(ChatColor.BLUE + "/ct on  : Opts you in to chattagging");
                if (player.hasPermission(ADMIN)) { //Admin Commands Only Shown To Admins
                    player.sendMessage(ChatColor.RED + "/ct glmute <player> : mutes the player from chattagging globally.");
                    player.sendMessage(ChatColor.RED + "/ct glunmute <player> : unmutes the player from chattagging globally.");
                }
                player.sendMessage(ChatColor.STRIKETHROUGH + "----------------------------------------------");
                succsess = true;
            } else if (length == 1) {
                if (args[0].equalsIgnoreCase("on")) {
                    if (optedOutPlayers.contains(player)) optedOutPlayers.remove(player);
                    player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "You were opted back into chat tagging.");
                    succsess = true;
                } else if (args[0].equalsIgnoreCase("off")) {
                    if (optedOutPlayers.contains(player)) {
                        player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "You are already opted out of being tagged.");
                        succsess = true;
                    } else {
                        optedOutPlayers.add(player);
                        player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "You were opted out of chat tagging.");
                        succsess = true;
                    }
                }
            } else if (length == 2) {
                if (player.hasPermission(ADMIN)) {
                    if (args[0].equals("glmute")) {
                        if (!punishedPlayers.contains(Bukkit.getServer().getPlayer(args[1]))) {
                            for (Player playerToMute : Bukkit.getServer().getOnlinePlayers()) {
                                if (playerToMute.getName().equalsIgnoreCase(args[1])) {
                                    player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Player was muted from chattagging successfully.");
                                    punishedPlayers.add(playerToMute);
                                    succsess = true;
                                }
                            }

                        } else if (punishedPlayers.contains(Bukkit.getServer().getPlayer(args[1]))) {
                            player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Player is already muted :(");
                            succsess = true;
                        }

                    }
                    if (args[0].equals("glunmute")) {
                        if (punishedPlayers.contains(Bukkit.getServer().getPlayer(args[1]))) {
                            player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Player was unmuted from chattagging successfully.");
                            punishedPlayers.remove(Bukkit.getServer().getPlayer(args[1]));
                            succsess = true;
                        } else if (!punishedPlayers.contains(Bukkit.getServer().getPlayer(args[1]))) {
                            player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Player is not muted.:(");
                            succsess = true;
                        }
                    }
                } else if (!player.hasPermission(ADMIN)) {
                    player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Sorry but you don't have permission to run the selected command!");
                    succsess = true;
                }
            }
        }
        if (succsess == false)
            player.sendMessage(ALERT_PREFIX + ChatColor.BOLD + ALERT_COLOR + "Invalid Syntax. Please do /ct for more information.");

        return true;
    }

    private ChatColor getColor(String message) {
        for (ChatColor color : ChatColor.values()) {
            if (message.contains(color.toString()))
                return color; //if the message contains this color then we'll return it
        }
        return ChatColor.WHITE; //returns black by default (if it can't find a valid color)
    }

    private void setupConfig() {
        config.addDefault("MaxMentions", 2);
        config.addDefault("AdminPermission", "skytags.admin");
        config.addDefault("UsePermission", "skytags.use");
        config.addDefault("AlertColor", "&c");
        config.addDefault("MentionColor", "&a");
        config.addDefault("AlertPrefix", "&b[ChatMentionsPro]");
        config.addDefault("NotificationSoundType", "LEVEL_UP");
        config.options().copyDefaults(true);
        saveConfig();
        MAX_MENTIONS = config.getInt("MaxMentions");
        ADMIN = new Permission(config.getString("AdminPermission"));
        USETAG = new Permission(config.getString("UsePermission"));
        ALERT_COLOR = ChatColor.translateAlternateColorCodes('&', config.getString("AlertColor"));
        MENTION_COLOR = ChatColor.translateAlternateColorCodes('&', config.getString("MentionColor"));
        ALERT_PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("AlertPrefix"));
    }

}
