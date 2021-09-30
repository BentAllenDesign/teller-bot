package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static discord4j.core.spec.EmbedCreateFields.Field;

@Service
public class HelpSlashEvent extends ReactiveEventAdapter {

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if(!event.getCommandName().equals("help"))
            return Mono.empty();

        /* INTENDED EVENT RESPONSE */
        return event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.infoEmbed("Crud Help", "** **",
                                        Arrays.asList(
                                                Field.of(
                                                        "Admin",
                                                        "```" +
                                                                "/set-name-of-user <USER> <NAME>\n" +
                                                                "/get-all-tickets\n" +
                                                                "/get-user-tickets <USER>" +
                                                                "/add-tickets <USER> <AMOUNT> <[Optional] REASON>\n" +
                                                                "/remove-tickets <USER> <AMOUNT> <[Optional] REASON>\n" +
                                                                "/create-raffle <TITLE> <DESCRIPTION> <LENGTH [Minutes]>\n" +
                                                                "/delete-raffle" +
                                                                "```",
                                                        false
                                                ),
                                                Field.of(
                                                        "General",
                                                        "```" +
                                                                "/get-my-tickets\n" +
                                                                "/add-raffle-entries <AMOUNT>\n" +
                                                                "/remove-raffle-entries <AMOUNT>\n" +
                                                                "/set-name <NAME>" +
                                                                "```",
                                                        false
                                                )
                                        )
                                )
                        );
    }
}
