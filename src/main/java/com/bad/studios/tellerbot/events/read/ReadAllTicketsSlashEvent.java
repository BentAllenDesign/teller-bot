package com.bad.studios.tellerbot.events.read;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import lombok.val;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@PropertySource("classpath:application.yaml")
public class ReadAllTicketsSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("get-all-tickets"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        val interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (interactingUser == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* ACCESS LOGIC */
        val allUsers = ticketService.getAllUserData();
        if (allUsers.isEmpty())
            return logEvent.logError(event, "There doesn't seem to be any ticket records");

        val ticketListString = new StringBuilder();
        ticketListString.append("```\n");
        allUsers.stream()
                .filter(x -> x.getTickets() > 0)
                .forEach(x ->
                        ticketListString
                                .append(x.getPreferredName())
                                .append("...................................".substring(x.getPreferredName().length()))
                                .append(x.getTickets())
                                .append("\n")
                );
        ticketListString.append("```");

        /* INTENDED EVENT RESPONSE */
        return logEvent.logInfo(
                event,
                interactingUser.getPreferredName() + " viewed the ticket list",
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.infoEmbed("Ticket List", allUsers.size() == 0 ? "No one has any tickets right now" : ticketListString.toString())
                        )
        );
    }
}
