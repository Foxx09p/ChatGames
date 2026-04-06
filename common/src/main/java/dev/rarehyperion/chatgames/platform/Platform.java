package dev.rarehyperion.chatgames.platform;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.config.Config;
import dev.rarehyperion.chatgames.game.EndReason;
import dev.rarehyperion.chatgames.game.GameType;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Platform {

    String name();
    PlatformPluginMeta pluginMeta();
    void broadcast(final Component component);
    void dispatchCommand(final String command);
    void registerCommands(final ChatGamesCore core);
    void registerListeners(final ChatGamesCore core);
    Collection<UUID> getOnlinePlayers();
    Optional<PlatformPlayer> getPlayer(final UUID uuid);
    PlatformTask runTask(final Runnable task);
    PlatformTask runTaskLater(final Runnable task, final long delay);
    PlatformTask runTaskTimer(final Runnable task, final long initialDelay, final long periodTicks);
    void saveDefaultConfig();
    void reloadConfig();
    PlatformSender wrapSender(final Object sender);
    <T> T getConfigValue(final String path, final Class<T> type, final T defaultValue);
    void setConfigValue(final String path, final Object value);
    void saveConfig();
    Config loadConfig(final File file);
    File getDataFolder();
    InputStream getResource(final String resourcePath);
    PlatformLogger getLogger();
    void dispatchStart(final GameType type, final String question, final String answer, final List<String> rewards);
    void dispatchEnd(final GameType type, final String question, final String answer, final List<String> rewards, final EndReason reason);
    void dispatchWin(final PlatformPlayer player, final GameType type, final String question, final String answer, final List<String> rewards);

}
