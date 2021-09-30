package com.bad.studios.tellerbot.events.delete;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.spec.EmbedCreateFields;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class DeleteRaffleEntrySlashEvent extends ReactiveEventAdapter {

    @Autowired
    private RaffleService raffleService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("remove-raffle-entries"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        var interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (interactingUser == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* REQUIRED OPTIONS */
        var amount = Integer.parseInt(event.getOption("amount").get().getValue().get().getRaw());

        if (raffleService.getAllRafflesSoft().isEmpty())
            return logEvent.logError(event, "There are currently no active raffles");

        /* ACCESS LOGIC */
        // TODO: Remove blocker for more than one raffle to remove tickets from
        var raffle = raffleService.getAllRafflesEager().get(0);
        var raffleEntry = raffle.getTickets().stream()
                .filter(x -> x.getUserId().equals(interactingUser.getId()))
                .findFirst();
        if (!raffleEntry.isPresent())
            return logEvent.logError(event, "Does not have an entry in this raffle");

        if (amount > raffleEntry.get().getAmount())
            return logEvent.logError(event, "Does not have that many tickets entered in this raffle\n```go\nrequested: " + amount + "\n" +
                            "entered:      " + raffleEntry.get().getAmount() + "\n```");

        var savedUser = ticketService.updateUser(interactingUser.setTickets(interactingUser.getTickets() + amount));

        raffleEntry.ifPresent(x -> {
            if (amount == x.getAmount()) {
                raffleService.deleteRaffleEntry(x);
            }
            else {
                x.setAmount(x.getAmount() - amount);
                raffleService.updateRaffleEntry(x);
            }
        });

        /* INTENDED EVENT RESPONSE */
        return logEvent.logInfo(
                event,
                Mono.just(savedUser)
                        .map(UserData::getPreferredName)
                        .defaultIfEmpty(interactingUser.getUsername())
                        .block() + " took " + amount + " ticket" + (amount != 1 ? "s" : "") + " out of " + raffle.getTitle(),
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(Embeds.infoEmbed(
                                "Got it!",
                                "Your tickets have been returned to you",
                                Collections.singletonList(
                                        EmbedCreateFields.Field.of(
                                                "Amount",
                                                amount + " ticket" + (amount != 1 ? "s" : ""),
                                                true
                                        )
                                )))
        );
    }

}
