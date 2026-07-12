package com.example.discordlogger;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class DiscordBot extends ListenerAdapter {

    private final PrimeLogPlugin plugin;
    private JDA jda;

    private String logChannelId;
    private String consoleChannelId;
    private boolean restrictConsole;
    private List<String> authorizedUsers;

    public DiscordBot(PrimeLogPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean start(String token) {
        try {
            logChannelId = plugin.getConfig().getString("log-channel-id");
            consoleChannelId = plugin.getConfig().getString("console-channel-id");
            restrictConsole = plugin.getConfig().getBoolean("sadece-yetkili-idler-calissin", true);
            authorizedUsers = plugin.getConfig().getStringList("authorized-console-users");

            jda = JDABuilder.createDefault(token)
                    // Konsol kanalındaki mesaj içeriğini okuyabilmek için gerekli
                    // (Discord Developer Portal'da bu intent'i botun ayarlarından da açman lazım)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                    .addEventListeners(this)
                    .build();

            jda.awaitReady();

            String activity = plugin.getConfig().getString("bot-activity", "sunucuyu izliyor");
            jda.getPresence().setActivity(Activity.watching(activity));

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("JDA başlatma hatası: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    // ================= LOG GÖNDERME =================

    public void sendLog(String title, String description, int color) {
        if (jda == null || logChannelId == null) return;
        TextChannel channel = jda.getTextChannelById(logChannelId);
        if (channel == null) return;

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(description);
        eb.setColor(new Color(color));
        eb.setTimestamp(java.time.Instant.now());
        channel.sendMessageEmbeds(eb.build()).queue();
    }

    // ================= KONSOL KÖPRÜSÜ =================

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (jda == null || consoleChannelId == null) return;

        MessageChannel channel = event.getChannel();
        if (!channel.getId().equals(consoleChannelId)) return;

        String userId = event.getAuthor().getId();
        if (restrictConsole && (authorizedUsers == null || !authorizedUsers.contains(userId))) {
            channel.sendMessage(":no_entry: Bu komutu çalıştırma yetkiniz yok.").queue();
            return;
        }

        final String commandText = event.getMessage().getContentRaw().trim();
        if (commandText.isEmpty()) return;

        // Konsol çıktısını yakalamak için geçici log handler'ı ekle
        final List<String> capturedLines = new ArrayList<>();
        Handler captureHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                capturedLines.add(record.getMessage());
            }
            @Override
            public void flush() {}
            @Override
            public void close() {}
        };

        Logger bukkitLogger = Bukkit.getServer().getLogger();
        bukkitLogger.addHandler(captureHandler);

        // Komut ana sunucu thread'inde çalıştırılmalı
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandText);
                } catch (Exception ex) {
                    capturedLines.add("Hata: " + ex.getMessage());
                }

                // Çıktının toplanması için kısa bir gecikme sonrası gönder
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        bukkitLogger.removeHandler(captureHandler);

                        StringBuilder output = new StringBuilder();
                        if (capturedLines.isEmpty()) {
                            output.append("(çıktı yok)");
                        } else {
                            for (String line : capturedLines) {
                                output.append(line).append("\n");
                            }
                        }

                        String result = output.toString();
                        if (result.length() > 1900) {
                            result = result.substring(0, 1900) + "\n... (kısaltıldı)";
                        }

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle(":computer: Konsol Komutu Çalıştırıldı");
                        eb.addField("Komut", "```" + commandText + "```", false);
                        eb.addField("Çıktı", "```" + result + "```", false);
                        eb.setFooter("Çalıştıran: " + event.getAuthor().getName(), null);
                        eb.setColor(new Color(plugin.getConfig().getInt("colors.console", 9807270)));

                        channel.sendMessageEmbeds(eb.build()).queue();
                    }
                }.runTaskLater(plugin, 10L); // ~0.5 saniye sonra
            }
        }.runTask(plugin);
    }
}
