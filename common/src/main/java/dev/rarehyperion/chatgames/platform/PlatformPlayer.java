package dev.rarehyperion.chatgames.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.util.UUID;

public interface PlatformPlayer {
    void sendMessage(final Component component);
    String name();
    UUID id();
    void showTitle(final Title title);
}
