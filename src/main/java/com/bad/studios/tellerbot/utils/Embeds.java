package com.bad.studios.tellerbot.utils;

import com.bad.studios.tellerbot.config.BotConfig;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.EmbedFieldData;
import discord4j.rest.util.Color;
import lombok.*;
import org.javatuples.Triplet;
import org.springframework.messaging.MessageChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static discord4j.core.spec.EmbedCreateFields.Field;

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

    public static EmbedCreateSpec infoEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.of(66, 133, 244))
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedData infoEmbedData(String title, String description) {
        return EmbedData.builder()
                .color(Color.of(66, 133, 244).getRGB())
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedCreateSpec infoEmbed(String title, String description, List<Field> fields) {
        return EmbedCreateSpec.builder()
                .color(Color.of(66, 133, 244))
                .title(title)
                .description(description)
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec infoEmbed(List<Field> fields) {
        return EmbedCreateSpec.builder()
                .color(Color.of(66, 133, 244))
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec dangerEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.of(234, 67, 53))
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedData dangerEmbedData(String title, String description) {
        return EmbedData.builder()
                .color(Color.of(234, 67, 53).getRGB())
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedData dangerEmbedData(String title, String description, List<EmbedFieldData> fields) {
        return EmbedData.builder()
                .color(Color.of(234, 67, 53).getRGB())
                .title(title)
                .description(description)
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec dangerEmbed(String title, String description, List<Field> fields) {
        return EmbedCreateSpec.builder()
                .color(Color.of(234, 67, 53))
                .title(title)
                .description(description)
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec successEmbed(String title, String description, List<Field> fields) {
        return EmbedCreateSpec.builder()
                .color(Color.of(52, 168, 83))
                .title(title)
                .description(description)
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec successEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.of(52, 168, 83))
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedCreateSpec warningEmbed(String title, String description) {
        return EmbedCreateSpec.builder()
                .color(Color.of(251, 188, 5))
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedData warningEmbedData(String title, String description) {
        return EmbedData.builder()
                .color(Color.of(251, 188, 5).getRGB())
                .title(title)
                .description(description)
                .build();
    }

    public static EmbedCreateSpec warningEmbed(String title, String description, List<Field> fields) {
        return EmbedCreateSpec.builder()
                .color(Color.of(251, 188, 5))
                .title(title)
                .description(description)
                .addAllFields(fields)
                .build();
    }

    public static EmbedCreateSpec errorEmbed(String reason) {
        return dangerEmbed(
                "(╯°□°）╯︵ ┻━┻",
                "Seems like we had an issue with that command!",
                Collections.singletonList(Field.of("Reason", reason, false))
        );
    }

    public static EmbedData errorEmbedData(String reason) {
        return dangerEmbedData(
                "(╯°□°）╯︵ ┻━┻",
                "Seems like we had an issue with that command!",
                Collections.singletonList(EmbedFieldData.builder().name("Reason").value(reason).inline(false).build())
        );
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
