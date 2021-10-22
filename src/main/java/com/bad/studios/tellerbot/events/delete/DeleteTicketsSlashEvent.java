package com.bad.studios.tellerbot.events.delete;

import com.bad.studios.tellerbot.events.LogEvent;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.yaml")
public class DeleteTicketsSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("remove-tickets"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        if (ticketService.getUserData(event.getInteraction().getUser().getId().asString()) == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* PERMISSIONS CHECK */
        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return logEvent.logError(event, "Insufficient permissions");

        /* REQUIRED OPTIONS */
        val userInput = event.getOption("user").get().getValue().get().asUser().block();
        val amount = Integer.parseInt(event.getOption("amount").get().getValue().get().getRaw());

        /* OPTIONAL OPTIONS */
        val reason = event.getOption("reason");
        val reasonValue = reason.map(applicationCommandInteractionOptionValue ->
                applicationCommandInteractionOptionValue.getValue().get().getRaw()).orElse("No reason provided");

        /* ACCESS LOGIC */
        val user = ticketService.getUserData(userInput.getId().asString());
        if(user == null)
            return logEvent.logError(event, "User not present in database");

        val savedUser = ticketService.removeTicketsFromUser(userInput.getId().asString(), amount);
        if (savedUser == null)
            return logEvent.logError(event, "Failed to save user ticket data");

        val netLossClamped = Math.min(user.getTickets(), amount);

        /* INTENDED EVENT RESPONSE */
        return logEvent.logWarningReason(
                event,
                user.getPreferredName() + " has lost " + netLossClamped + " ticket" + (netLossClamped != 1 ? "s" : ""),
                reasonValue,
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.warningEmbed(
                                        "Oh no!",
                                        user.getPreferredName() + " has lost " + netLossClamped + " ticket(s)",
                                        reason.map(applicationCommandInteractionOptionValue ->
                                                Collections.singletonList(
                                                        EmbedCreateFields.Field.of(
                                                            "Reason",
                                                            applicationCommandInteractionOptionValue.getValue().get().getRaw(),
                                                            true
                                                        )
                                                )
                                        ).orElse(Collections.emptyList())
                                )
                        )
        );
    }
}
