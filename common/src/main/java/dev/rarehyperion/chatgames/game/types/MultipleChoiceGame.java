package dev.rarehyperion.chatgames.game.types;

import dev.rarehyperion.chatgames.ChatGamesCore;
import dev.rarehyperion.chatgames.game.AbstractGame;
import dev.rarehyperion.chatgames.game.GameConfig;
import dev.rarehyperion.chatgames.game.GameType;
import dev.rarehyperion.chatgames.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MultipleChoiceGame extends AbstractGame {

    private static final Pattern OPTION_PATTERN = Pattern.compile("^([A-H])\\.");

    private final GameConfig.MultipleChoiceQuestion question;
    private final List<String> answerOptions;

    public MultipleChoiceGame(final ChatGamesCore plugin, final GameConfig config) {
        super(plugin, config, GameType.TRIVIA);
        this.question = this.selectRandom(config.getMultipleChoiceQuestions());
        this.answerOptions = extractAnswerOptions(this.question.answers());
    }

    @Override
    public void onStart() {
        this.plugin.platform().dispatchStart(this.type, MessageUtil.plainText(this.getQuestion()), this.getCorrectAnswer().orElse(null), this.config.getRewardCommands());

        final Component titleText = MessageUtil.parse(this.config.getDisplayName())
                .decoration(TextDecoration.BOLD, true);

        // Build full question + answers for subtitle
        final String fullQuestion = this.question.question() + "\n" + String.join("\n", this.question.answers());
        final Component subtitleText = MessageUtil.parse(fullQuestion);

        final Title.Times times = Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofSeconds(6),
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
        // Send full question + answers to chat in same format as screen
        final String fullQuestion = this.question.question() + "\n" + String.join("\n", this.question.answers());
        final Component chatMessage = this.config.getStartMessage(MessageUtil.parse(fullQuestion));
        this.plugin.broadcast(chatMessage);
    }

    @Override
    public boolean checkAnswer(final String answer) {
        return answer.equalsIgnoreCase(this.question.correctAnswer());
    }

    @Override
    public Component getQuestion() {
        return MessageUtil.parse(this.question.question());
    }

    @Override
    public List<String> getAnswerOptions() {
        return this.answerOptions;
    }

    @Override
    public Optional<String> getCorrectAnswer() {
        return Optional.of(this.question.correctAnswer());
    }

    private List<String> extractAnswerOptions(final List<String> answers) {
        return answers.stream()
                .map(answer -> {
                    final Matcher matcher = OPTION_PATTERN.matcher(answer);
                    if (matcher.find()) {
                        return matcher.group(1).toLowerCase();
                    }
                    return answer.trim().toLowerCase();
                }).collect(Collectors.toList());
    }

}
