package com.bad.studios.tellerbot.events.read;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import lombok.val;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static discord4j.core.spec.EmbedCreateFields.Field;

@Service
@PropertySource("classpath:application.yaml")
public class ReadTicketsSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("get-my-tickets"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK && ACCESS LOGIC */
        val interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (interactingUser == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* INTENDED EVENT RESPONSE */
        return logEvent.logInfo(
                event,
                interactingUser.getPreferredName() + " viewed their own ticket data",
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(Embeds.infoEmbed(Collections.singletonList(
                                Field.of(
                                        interactingUser.getPreferredName() == null ? interactingUser.getUsername() : interactingUser.getPreferredName(),
                                        interactingUser.getTickets().toString() + " ticket" + (interactingUser.getTickets() != 1 ? "s" : ""),
                                        false
                                )
                        )))
        );
    }
}
