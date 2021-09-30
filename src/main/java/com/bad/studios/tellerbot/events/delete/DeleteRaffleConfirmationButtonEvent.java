package com.bad.studios.tellerbot.events.delete;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * This class is set up for the planned raffle deletion request
 * functionality in {@link RaffleService}
 *
 */
//@Service
//@PropertySource("classpath:application.yaml")
public class DeleteRaffleConfirmationButtonEvent extends ReactiveEventAdapter {

    /*@Autowired
    private RaffleService raffleService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;
    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onButtonInteract(ButtonInteractEvent event) {

        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(Embeds.errorEmbed("Insufficient permissions"));

        var embed = event.getMessage().get().getEmbeds();
        if(embed.isEmpty() || !embed.get(0).getTitle().get().equals("Are you sure you want to delete this raffle?"))
            return Mono.empty();

        var message = event.getMessage().get();
        var raffleDeleteRequest = raffleService.getRaffleDeleteRequestById(message.getId().asLong());
        if(!raffleDeleteRequest.isPresent())
            return Mono.empty();

        var user = event.getInteraction().getUser();
        if(raffleDeleteRequest.get().getUserId() != user.getId().asLong())
            return Mono.empty();

        if(Instant.now().getEpochSecond() >= raffleDeleteRequest.get().getTimeout()) {
            raffleService.deleteRaffleDeleteRequestById(message.getId().asLong());
            return event.getInteractionResponse().editInitialResponse(
                    WebhookMessageEditRequest.builder()
                            .addEmbed(Embeds.infoEmbedData("Interaction timed out", "Nothing has changed"))
                            .components(Collections.emptyList())
                            .build()
            );
        }

        if(event.getCustomId().equals("no-comply")) {
            raffleService.deleteRaffleDeleteRequestById(message.getId().asLong());
            return event.getInteractionResponse().editInitialResponse(
                    WebhookMessageEditRequest.builder()
                            .addEmbed(Embeds.infoEmbedData("You're good to go!", "Nothing has changed"))
                            .components(Collections.emptyList())
                            .build()
            );
        }
        else {
            var raffle = raffleService.getRaffleByIdEager(Integer.parseInt(event.getCustomId()));
            if (raffle == null)
                return event.getInteractionResponse().editInitialResponse(
                        WebhookMessageEditRequest.builder()
                                .addEmbed(Embeds.errorEmbedData("Could not fetch raffle data"))
                                .build()
                );

            var userTickets = raffle.getTickets();
            if (!userTickets.isEmpty())
                userTickets.forEach( entry -> {
                    if(ticketService.getUserData(entry.getUserId()).isPresent())
                        ticketService.addTicketsToUser(entry.getUserId(), entry.getAmount());
                });

            raffleService.deleteRaffle(Integer.parseInt(event.getCustomId()));
            raffleService.deleteRaffleDeleteRequestById(message.getId().asLong());

            var userData = ticketService.getUserData(user.getId().asString()).get();

            return logEvent.logDanger(
                    event,
                    userData.getPreferredName() + " deleted raffle " + raffle.getTitle(),
                    event.getInteractionResponse().editInitialResponse(
                            WebhookMessageEditRequest.builder()
                                    .addEmbed(Embeds.dangerEmbedData("Raffle Deleted", "This action cannot be undone"))
                                    .components(Collections.emptyList())
                                    .build()
                    )
            );
        }
    }*/
}
