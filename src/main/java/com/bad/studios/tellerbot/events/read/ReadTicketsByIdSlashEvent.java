package com.bad.studios.tellerbot.events.read;

import com.bad.studios.tellerbot.events.LogEvent;
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

import static discord4j.core.spec.EmbedCreateFields.Field;

@Service
@PropertySource("classpath:application.yaml")
public class ReadTicketsByIdSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("get-user-tickets"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        if (ticketService.getUserData(event.getInteraction().getUser().getId().asString()) == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* PERMISSIONS CHECK */
        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return logEvent.logError(event, "Insufficient permissions");

        /* REQUIRED OPTIONS */
        var userInput = event.getOption("user").get().getValue().get().asUser().block();

        /* ACCESS LOGIC */
        var user = ticketService.getUserData(userInput.getId().asString());
        if (user == null)
            return logEvent.logError(event,"User not present in database");

        var callerData = ticketService.getUserData(event.getInteraction().getUser().getId().asString());

        /* INTENDED EVENT RESPONSE */
        return logEvent.logInfo(
                event,
                callerData.getPreferredName() + " viewed " + user.getPreferredName() + (user.getPreferredName().endsWith("s") ? "'" : "'s") + " ticket data",
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.infoEmbed(
                                        Collections.singletonList(
                                                Field.of(
                                                        user.getPreferredName(),
                                                        user.getTickets().toString() + " ticket" + (user.getTickets() != 1 ? "s" : ""),
                                                        false
                                                )
                                        )
                                )
                        )
        );
    }
}
