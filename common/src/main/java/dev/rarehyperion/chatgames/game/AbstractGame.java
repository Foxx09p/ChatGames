package dev.rarehyperion.chatgames.game;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.platform.PlatformPlayer;
import dev.rarehyperion.chatgames.util.MessageUtil;
import dev.rarehyperion.chatgames.util.Templater;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class AbstractGame implements Game {

    protected final ChatGamesCore plugin;
    protected final GameConfig config;
    protected final GameType type;

    protected AbstractGame(final ChatGamesCore plugin, final GameConfig config, GameType type) {
        this.plugin = plugin;
        this.config = config;
        this.type = type;
    }

    @Override
    public GameType getType() {
        return this.type;
    }

    @Override
    public void onStart() {
        this.plugin.platform().dispatchStart(this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands());

        final Component titleText = MessageUtil.parse(this.config.getDisplayName())
                .decoration(TextDecoration.BOLD, true);

        final Component subtitleText = this.getQuestion();

        final Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(5),
                Duration.ofMillis(500)
        );

        final Title title = Title.title(titleText, subtitleText, times);

        for (final PlatformPlayer player : this.plugin.platform().getOnlinePlayers()) {
            player.showTitle(title);
        }

        this.start();
    }

    @Override
    public void onEnd() {
        this.plugin.platform().dispatchEnd(this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands(), EndReason.COMMAND);
    }

    @Override
    public void onWin(final PlatformPlayer winner) {
        final Component message = this.config.getWinMessage(winner.name(), this.getCorrectAnswer().orElse("Unknown"));
        this.plugin.broadcast(message);
        this.plugin.platform().runTask(() -> {
            for (final String command : this.config.getRewardCommands()) {
                final String processed = Templater.process(command, winner);
                this.plugin.platform().dispatchCommand(processed);
            }
            this.plugin.platform().dispatchWin(winner, this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands());
        });
    }

    @Override
    public void onTimeout() {
        final Component message = this.config.getTimeoutMessage(this.getCorrectAnswer().orElse("Unknown"));
        this.plugin.broadcast(message);
        this.plugin.platform().dispatchEnd(this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands(), EndReason.TIMEOUT);
    }

    @Override
    public List<String> getAnswerOptions() {
        return Collections.emptyList();
    }

    protected Component createStartMessage() {
        return this.config.getStartMessage(this.getQuestion());
    }

    protected <T> T selectRandom(final List<T> list) {
        return list.get(new Random().nextInt(list.size()));
    }

}
