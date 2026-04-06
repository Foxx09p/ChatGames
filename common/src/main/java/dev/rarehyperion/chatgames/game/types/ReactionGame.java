package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionGame extends AbstractGame {

    private static final Pattern BUTTON_PATTERN = Pattern.compile("<button(?:\\s+([^>]*?))?>(.*?)</button>", Pattern.DOTALL);

    private final GameConfig.ReactionVariant variant;
    public final String clickToken;

    public ReactionGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.variant = this.selectRandom(config.getReactionVariants());
        this.clickToken = UUID.randomUUID().toString();
    }

    @Override
    public void onStart() {
        this.plugin.platform().dispatchStart(this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands());

        final Component titleText = MessageUtil.parse(this.config.getDisplayName())
                .decoration(TextDecoration.BOLD, true);

        // For click variants show "Check chat!" as subtitle since buttons can't show on screen
        // For type variants show what they need to type as subtitle
        final Component subtitleText = "click".equalsIgnoreCase(this.variant.answer())
                ? MessageUtil.parse("<yellow>Check chat!</yellow>")
                : this.getQuestion();

        final Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(4),
                Duration.ofMillis(500)
        );

        final Title title = Title.title(titleText, subtitleText, times);

        for (final UUID uuid : this.plugin.platform().getOnlinePlayers()) {
            this.plugin.platform().getPlayer(uuid).ifPresent(player -> player.showTitle(title));
        }

        this.start();
    }

    @Override
    public void start() {
        // Broadcast the challenge to chat so clickable buttons work
        final Component chatMessage = this.config.getStartMessage(this.getQuestion());
        this.plugin.broadcast(chatMessage);
    }

    @Override
    public boolean checkAnswer(final String answer) {
        if ("click".equalsIgnoreCase(this.variant.answer())) {
            return answer.equalsIgnoreCase(this.clickToken);
        }
        if (this.variant.answer().isEmpty()) return true;
        return answer.equalsIgnoreCase(this.variant.answer());
    }

    @Override
    public Component getQuestion() {
        final String challenge = this.normalizeEmojis(this.variant.challenge());
        if ("click".equalsIgnoreCase(this.variant.answer())) {
            return this.parseButton(challenge);
        }
        return MessageUtil.parse(challenge);
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        if (this.variant.answer().isEmpty() || "click".equalsIgnoreCase(this.variant.answer())) {
            return Optional.empty();
        }
        return Optional.of(this.variant.answer());
    }

    private Component parseButton(final String challenge) {
        final Matcher matcher = BUTTON_PATTERN.matcher(challenge);
        final TextComponent.Builder builder = Component.text();

        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(MessageUtil.parse(challenge.substring(lastEnd, matcher.start())));
            }

            final String attrString = matcher.group(1);
            final String buttonText = matcher.group(2);
            final Map<String, String> attributes = this.parseAttributes(attrString);
            final String hoverText = attributes.get("hover");

            Component button = MessageUtil.parse(buttonText);

            if (attributes.containsKey("win")) {
                button = button.clickEvent(ClickEvent.runCommand("/chatgames answer " + this.clickToken));
            } else {
                button = button.clickEvent(ClickEvent.runCommand("/chatgames answer " + UUID.randomUUID()));
            }

            if (hoverText != null && !hoverText.isEmpty()) {
                button = button.hoverEvent(MessageUtil.parse(hoverText));
            }

            builder.append(button);
            lastEnd = matcher.end();
        }

        if (lastEnd < challenge.length()) {
            builder.append(MessageUtil.parse(challenge.substring(lastEnd)));
        }

        return builder.build();
    }

    private Map<String, String> parseAttributes(final String attrString) {
        final Map<String, String> map = new HashMap<>();
        if (attrString == null || attrString.trim().isEmpty()) return map;

        final Pattern pattern = Pattern.compile("(\\w+)(?:='([^']*)')?");
        final Matcher matcher = pattern.matcher(attrString);

        while (matcher.find()) {
            final String key = matcher.group(1), value = matcher.group(2);
            map.put(key, value);
        }

        return map;
    }

    private String normalizeEmojis(final String text) {
        if (text == null) return null;
        return text.replaceAll("\\uFE0F", "");
    }

}
