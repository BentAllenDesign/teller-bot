package com.bad.studios.tellerbot.events.create;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.models.Raffle;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.yaml")
public class CreateRaffleSlashEvent extends ReactiveEventAdapter {

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
        if (!event.getCommandName().equals("create-raffle"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        if (ticketService.getUserData(event.getInteraction().getUser().getId().asString()) == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* PERMISSIONS CHECK */
        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return logEvent.logError(event, "Insufficient permissions");

        /* REQUIRED OPTIONS */
        val title = event.getOption("title").get().getValue().get().getRaw();
        val description = event.getOption("description").get().getValue().get().getRaw();
        val time = Integer.parseInt(event.getOption("time").get().getValue().get().getRaw());

        /* ACCESS LOGIC */
        // TODO: Remove blocker for multiple raffle instances
        if (!raffleService.getAllRafflesSoft().isEmpty())
            return logEvent.logError(event, "Cannot create more than one raffle at this time");

        val savedRaffle = raffleService.createRaffle(
                new Raffle(title, description, time, Collections.emptyList(), true)
        );
        if(savedRaffle == null)
            return logEvent.logError(event, "Failed to save raffle data");

        /* INTENDED EVENT RESPONSE */
        return logEvent.logSuccess(
                event,
                event.getInteraction().getMember().map(x -> x.getNickname().orElse(x.getUsername())).orElse("Unknown user") + " created raffle " + savedRaffle.getTitle(),
                event.reply().withEmbeds(
                        Embeds.successEmbed(
                                "Raffle Info",
                                "** **",
                                Arrays.asList(
                                        EmbedCreateFields.Field.of(
                                                title,
                                                description,
                                                false
                                        ),
                                        EmbedCreateFields.Field.of(
                                                "Raffle Closing Time",
                                                // TODO: Create passthrough for time value (seconds/minutes/hours etc..)
                                                ZonedDateTime.now().plusMinutes(time).format(DateTimeFormatter.ofPattern("MMMM d, h:mm a z")),
                                                false
                                        )
                                )
                        )
                )
        );

    }
}
