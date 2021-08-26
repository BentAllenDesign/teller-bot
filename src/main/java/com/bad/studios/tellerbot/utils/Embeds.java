package com.bad.studios.tellerbot.utils;

import com.bad.studios.tellerbot.config.BotConfig;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.*;
import org.javatuples.Triplet;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Embeds {

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmbedContents {

        @NonNull
        private String title;
        @NonNull
        private String description;
        @NonNull
        private String footer;
        private List<Triplet<String, String, Boolean>> fields = new ArrayList<>();

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorEmbedContents {

        private Message message;
        private Exception exception;

        public ErrorEmbedContents setException(Exception exception) {
            this.exception = exception;
            return this;
        }

        public ErrorEmbedContents setMessage(Message message) {
            this.message = message;
            return this;
        }

    }

    public static Mono<Void> sendEmbedToChannel(Mono<Message> filteredMessage, User user, String title, String description, String footer, List<Triplet<String, String, Boolean>> fields) {
        return filteredMessage
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> Embeds.infoEmbed(
                        spec,
                        user,
                        new Embeds.EmbedContents(
                                "",
                                "",
                                "",
                                new ArrayList<>()
                        )
                )))
                .then();
    }

    public static LegacyEmbedCreateSpec errorEmbed(LegacyEmbedCreateSpec spec, Message message, Exception exception) {
        StackTraceElement stackTrace = exception.getStackTrace()[0];
        return dangerEmbed(
                                spec,
                                message.getAuthor().get(),
                                new EmbedContents(
                                        "Oops!",
                                        "Seems like we ran into some trouble with this command string:\n" + message.getContent(),
                                        "Reported",
                                        Collections.singletonList(Triplet.with(
                                                "Exception",
                                                stackTrace.getFileName() + "\n" +
                                                        stackTrace.getMethodName() + " :: Line " +
                                                        stackTrace.getLineNumber() + "\n" +
                                                        "***" + exception.getMessage() + "***",
                                                false
                                        ))
                                )
                        );
    }

    public static Mono<Void> errorEmbed(Message message, Throwable exception) {
        return Mono.just(message)
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed( spec ->
                        dangerEmbed(
                                spec,
                                message.getAuthor().get(),
                                new EmbedContents(
                                        "Oops!",
                                        "Seems like we ran into some trouble with this command:\n" + message,
                                        "Reported",
                                        Collections.singletonList(Triplet.with(
                                                "Exception",
                                                exception.getMessage(),
                                                false
                                        ))
                                )
                        )
                ))
                .then();
    }

    public static LegacyEmbedCreateSpec successEmbed(LegacyEmbedCreateSpec spec, User user, EmbedContents contents) {
        return setContents(spec, user, contents)
                .setColor(Color.MEDIUM_SEA_GREEN);
    }

    public static LegacyEmbedCreateSpec dangerEmbed(LegacyEmbedCreateSpec spec, User user, EmbedContents contents) {
        return setContents(spec, user, contents)
                .setColor(Color.JAZZBERRY_JAM);
    }

    public static LegacyEmbedCreateSpec infoEmbed(LegacyEmbedCreateSpec spec, User user, EmbedContents contents) {
        return setContents(spec, user, contents)
                .setColor(Color.of(56, 128, 235));
    }

    private static LegacyEmbedCreateSpec setContents(LegacyEmbedCreateSpec spec, User user, EmbedContents contents) {
        contents.getFields().forEach(t -> {
            spec.addField(
                    t.getValue(0).toString(),
                    t.getValue(1).toString(),
                    Boolean.parseBoolean(t.getValue(2).toString())
            );
        });

        return spec
                .setAuthor(user.getUsername() + "#" + user.getDiscriminator(),
                user.getAvatarUrl(),
                user.getAvatarUrl())
                .setTitle(contents.getTitle())
                .setDescription(contents.getDescription())
                .setFooter("━━━━━━━━━━\n" + contents.getFooter(), BotConfig.footerPicUri.get())
                .setTimestamp(Instant.now());
    }
}
