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

        // Only show game name as title, no subtitle — answers are too long for screen
        final Component titleText = MessageUtil.parse(this.config.getDisplayName())
                .decoration(TextDecoration.BOLD, true);

        final Title.Times times = Title.Times.times(
