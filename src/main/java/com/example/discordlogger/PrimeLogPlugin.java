package com.example.discordlogger;

import com.example.discordlogger.listeners.PlayerActivityListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class PrimeLogPlugin extends JavaPlugin {

    private static PrimeLogPlugin instance;
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        String token = getConfig().getString("bot-token");
        if (token == null || token.isEmpty() || token.equals("BURAYA_BOT_TOKENINI_YAZ")) {
            getLogger().severe("=====================================================");
            getLogger().severe(" config.yml içine geçerli bir Discord bot token'ı");
            getLogger().severe(" girmediniz! Plugin devre dışı bırakılıyor.");
            getLogger().severe("=====================================================");
            setEnabled(false);
            return;
        }

        discordBot = new DiscordBot(this);
        boolean started = discordBot.start(token);
        if (!started) {
            getLogger().severe("Discord bot başlatılamadı. Token'ınızı kontrol edin.");
            setEnabled(false);
            return;
        }

        // Oyuncu ve sunucu olaylarını dinleyen listener'ı kaydet
        getServer().getPluginManager().registerEvents(new PlayerActivityListener(this), this);

        getLogger().info("PrimeLog başarıyla etkinleştirildi.");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
        getLogger().info("PrimeLog devre dışı bırakıldı.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("primelog")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                if (discordBot != null) {
                    discordBot.shutdown();
                }
                discordBot = new DiscordBot(this);
                boolean ok = discordBot.start(getConfig().getString("bot-token"));
                sender.sendMessage(ok
                        ? "§a[PrimeLog] Yeniden yüklendi ve Discord botu bağlandı."
                        : "§c[PrimeLog] Yeniden yükleme başarısız, token'ı kontrol edin.");
                return true;
            }
            sender.sendMessage("§eKullanım: /primelog reload");
            return true;
        }
        return false;
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public static PrimeLogPlugin getInstance() {
        return instance;
    }
}
