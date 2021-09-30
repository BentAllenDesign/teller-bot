package com.bad.studios.tellerbot.events.update;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import lombok.val;
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
public class UpdateUserNameSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("set-name-of-user"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        val interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (interactingUser == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");

        /* PERMISSIONS CHECK */
        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return logEvent.logError(event, "Insufficient permissions");

        /* REQUIRED OPTIONS */
        var newName = event.getOption("name").get().getValue().get().asString();
        var userInput = event.getOption("user").get().getValue().get().asUser().block();

        /* ACCESS LOGIC */
        var user = ticketService.getUserData(userInput.getId().asString());
        if (user == null)
            return logEvent.logError(event,"User not present in database");
        var oldName = user.getPreferredName();

        var savedUser = ticketService.createUserData(user.setPreferredName(newName));
        if(savedUser == null)
            return logEvent.logError(event, "Failed to update user ticket object");

        /* INTENDED EVENT RESPONSE */
        return logEvent.logSuccess(
                event,
                interactingUser.getPreferredName() + " has changed " + oldName + (oldName.endsWith("s") ? "'" : "'s") + " name to " + newName,
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.successEmbed("Got it!", oldName + (oldName.endsWith("s") ? "'" : "'s") + " name is now set as **" + newName + "**")
                        )
        );
    }
}