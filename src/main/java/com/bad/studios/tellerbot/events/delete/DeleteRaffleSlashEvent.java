package com.bad.studios.tellerbot.events.delete;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.yaml")
public class DeleteRaffleSlashEvent extends ReactiveEventAdapter {

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
        if (!event.getCommandName().equals("delete-raffle"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        if (ticketService.getUserData(event.getInteraction().getUser().getId().asString()) == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* PERMISSIONS CHECK */
        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return logEvent.logError(event, "Insufficient permissions");

        /* ACCESS LOGIC */
        var raffles = raffleService.getAllRafflesEager();
        if (raffles.isEmpty())
            return logEvent.logError(event, "There doesn't seem to be any raffle data");

        var raffle = raffles.get(0);
        if (!raffle.getTickets().isEmpty())raffle.getTickets().forEach( entry -> {
                if(ticketService.getUserData(entry.getUserId()) != null)
                    ticketService.addTicketsToUser(entry.getUserId(), entry.getAmount());
            });

        raffleService.deleteRaffle(raffle.getId());

        /* INTENDED EVENT RESPONSE */
        return event.reply()
                .withEmbeds(
                        Embeds.warningEmbed("Raffle deleted", "This action cannot be reversed")
                );
    }

}


