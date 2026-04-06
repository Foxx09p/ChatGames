package dev.rarehyperion.chatgames.platform.impl;

import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import java.util.UUID;

public class PaperPlatformPlayer implements PlatformPlayer {

    private final Player player;

    public PaperPlatformPlayer(final Player player) {
        this.player = player;
    }

    @Override
    public void sendMessage(final Component component) {
        this.player.sendMessage(component);
    }

    @Override
    public String name() {
        return this.player.getName();
    }

    @Override
    public UUID id() {
        return this.player.getUniqueId();
    }

    @Override
    public void showTitle(final Title title) {
        this.player.showTitle(title);
    }
}
