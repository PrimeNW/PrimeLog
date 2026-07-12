package com.example.discordlogger.listeners;

import com.example.discordlogger.PrimeLogPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerActivityListener implements Listener {

    private final PrimeLogPlugin plugin;
    private final FileConfiguration cfg;

    public PlayerActivityListener(PrimeLogPlugin plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
    }

    private boolean enabled(String key) {
        return cfg.getBoolean("log-events." + key, false);
    }

    private int color(String key) {
        return cfg.getInt("colors." + key, 3447003);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (!enabled("join-quit")) return;
        Player p = e.getPlayer();
        plugin.getDiscordBot().sendLog(":green_circle: Sunucuya Katıldı",
                p.getName() + " sunucuya giriş yaptı.", color("join"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        if (!enabled("join-quit")) return;
        Player p = e.getPlayer();
        plugin.getDiscordBot().sendLog(":red_circle: Sunucudan Ayrıldı",
                p.getName() + " sunucudan çıkış yaptı.", color("quit"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        if (!enabled("kick-ban")) return;
        plugin.getDiscordBot().sendLog(":boot: Oyuncu Atıldı",
                e.getPlayer().getName() + " atıldı.\nSebep: " + e.getReason(), color("quit"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!enabled("chat")) return;
        plugin.getDiscordBot().sendLog(":speech_balloon: Chat",
                "**" + e.getPlayer().getName() + "**: " + e.getMessage(), color("chat"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!enabled("command")) return;
        plugin.getDiscordBot().sendLog(":keyboard: Komut Kullanıldı",
                "**" + e.getPlayer().getName() + "**: " + e.getMessage(), color("command"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!enabled("death")) return;
        plugin.getDiscordBot().sendLog(":skull: Oyuncu Öldü",
                e.getDeathMessage(), color("death"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!enabled("block-break")) return;
        plugin.getDiscordBot().sendLog(":pick: Blok Kırıldı",
                e.getPlayer().getName() + " -> " + e.getBlock().getType()
                        + " @ " + formatLoc(e.getBlock().getLocation()), color("command"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!enabled("block-place")) return;
        plugin.getDiscordBot().sendLog(":bricks: Blok Kondu",
                e.getPlayer().getName() + " -> " + e.getBlock().getType()
                        + " @ " + formatLoc(e.getBlock().getLocation()), color("command"));
    }

    private String formatLoc(org.bukkit.Location loc) {
        return loc.getWorld().getName() + " (" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
