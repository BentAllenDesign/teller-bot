package com.bad.studios.tellerbot.events.create;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.models.RaffleEntry;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.spec.EmbedCreateFields;
import lombok.val;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@PropertySource("classpath:application.yaml")
public class CreateRaffleEntrySlashEvent extends ReactiveEventAdapter {

    @Autowired
    private RaffleService raffleService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("add-raffle-entries"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        val interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (ticketService.getUserData(event.getInteraction().getUser().getId().asString()) == null)
            return event.reply().withEphemeral(true).withContent("Oops, you don't seem to be in our system just yet!");

        /* REQUIRED OPTIONS */
        val amount = Integer.parseInt(event.getOption("amount").get().getValue().get().getRaw());
        if (amount <= 0)
            return logEvent.logError(event, "Wager must be greater than 0");

        /* ACCESS LOGIC */
        val raffles = raffleService.getAllRafflesEager();
        if (raffles.isEmpty())
            return logEvent.logError(event, "There are currently no raffles");

        if (amount > interactingUser.getTickets())
            return logEvent.logError(event, "Not enough tickets\n```go\nrequested: " + amount + "\n" +
                            "held:      " + interactingUser.getTickets() + "\n```");

        // TODO remove blocker for having only one active raffle
        if (raffles.size() != 1)
            return Mono.empty();

        // TODO make the following two service calls transactional will a rollback on exception thrown
        val savedUser = ticketService.removeTicketsFromUser(interactingUser.getId(), amount);
        if (savedUser == null)
            return logEvent.logError(event, "Failed to update user ticket data");

        val savedRaffleEntry = raffleService.createRaffleEntry(new RaffleEntry(raffles.get(0).getId(), interactingUser.getId(), amount));
        if(savedRaffleEntry == null) {
            ticketService.addTicketsToUser(interactingUser.getId(), amount);
            return logEvent.logError(event, "Failed to save raffle entry data");
        }

        val raffle = raffleService.getRaffleByIdSoft(savedRaffleEntry.getRaffleId());

        /* INTENDED EVENT RESPONSE */
        return logEvent.logSuccess(
                event,
                (interactingUser.getPreferredName() != null ? interactingUser.getPreferredName() : interactingUser.getUsername()) + " entered " + amount + " ticket" + (amount != 1 ? "s" : "") + " into " + raffle.getTitle(),
                event.reply()
                    .withEphemeral(true)
                    .withEmbeds(Embeds.successEmbed(
                            "Nice!",
                            "Your entry has been submitted into " + raffles.get(0).getTitle(),
                            Collections.singletonList(EmbedCreateFields.Field.of("Entry", amount + " ticket(s)", false)))));
    }

}
