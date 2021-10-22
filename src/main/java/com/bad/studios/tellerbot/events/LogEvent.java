package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateFields;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@PropertySource("classpath:application.yaml")
public class LogEvent {

    @Value("${discord.logchannel}")
    private String logChannelId;

    public Publisher<?> logError(SlashCommandEvent event, String message) {

        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.dangerEmbed(
                "Error with user: " + event.getInteraction().getMember().get().getNickname().orElse(event.getInteraction().getMember().get().getUsername()),
                "Tried to use command: `" + event.getCommandName() + "`",
                Collections.singletonList(EmbedCreateFields.Field.of(
                        "Error Reason",
                        message,
                        true
                ))
        )).block();

        return event.reply()
                .withEphemeral(true)
                .withEmbeds(Embeds.errorEmbed(message));
    }

    public Publisher<?> logError(MemberJoinEvent event) {

        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.dangerEmbed(
                "Error with user: " + event.getMember().getUsername(),
                "Failed to add them to the database",
                Collections.emptyList()
        )).block();

        return Mono.empty();
    }

    public Publisher<?> logInfo(SlashCommandEvent event, String logMessage, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.infoEmbed(
                logMessage,
                "** **"
        )).block();

        return reply;
    }

    public void logInfo(MemberUpdateEvent event, String logMessage) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.infoEmbed(
                logMessage,
                "** **"
        )).block();
    }

    public Publisher<?> logSuccess(SlashCommandEvent event, String logMessage, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.successEmbed(
                logMessage,
                "** **"
        )).block();

        return reply;
    }

    public Publisher<?> logSuccessReason(SlashCommandEvent event, String logMessage, String reason, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.successEmbed(
                logMessage,
                "** **",
                Collections.singletonList(EmbedCreateFields.Field.of(
                        "Reason",
                        reason,
                        true
                ))
        )).block();

        return reply;
    }

    public Publisher<?> logWarningReason(SlashCommandEvent event, String logMessage, String reason, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.warningEmbed(
                logMessage,
                "** **",
                Collections.singletonList(EmbedCreateFields.Field.of(
                        "Reason",
                        reason,
                        true
                ))
        )).block();

        return reply;
    }

    public Publisher<?> logDanger(SlashCommandEvent event, String logMessage, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.dangerEmbed(
                logMessage,
                "** **"
        )).block();

        return reply;
    }

    public Publisher<?> logDanger(ButtonInteractEvent event, String logMessage, Publisher<?> reply) {
        var channel = event.getClient().getChannelById(Snowflake.of(logChannelId)).cast(TextChannel.class).block();
        channel.createMessage(Embeds.dangerEmbed(
                logMessage,
                "** **"
        )).block();

        return reply;
    }

}
