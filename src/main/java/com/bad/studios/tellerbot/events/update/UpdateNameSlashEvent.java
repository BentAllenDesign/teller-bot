package com.bad.studios.tellerbot.events.update;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import lombok.val;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UpdateNameSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {

        /* COMMAND CHECK */
        if (!event.getCommandName().equals("set-name"))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK */
        val interactingUser = ticketService.getUserData(event.getInteraction().getUser().getId().asString());
        if (interactingUser == null)
            return event.reply().withEphemeral(true).withContent("Hey there, you don't seem to be in our system just yet! " +
                    "Give us some time to open up functionality to the general public!");
        val oldName = interactingUser.getPreferredName();

        /* REQUIRED OPTIONS */
        val newName = event.getOption("name").get().getValue().get().asString();

        /* ACCESS LOGIC */
        val savedUser = ticketService.createUserData(interactingUser.setPreferredName(newName));
        if(savedUser == null)
            return logEvent.logError(event, "Failed to update user ticket object");

        /* INTENDED EVENT RESPONSE */
        return logEvent.logSuccess(
                event,
                oldName + " changed their name to " + newName,
                event.reply()
                        .withEphemeral(true)
                        .withEmbeds(
                                Embeds.successEmbed("Got it!", "We now have your name set as **" + newName + "**")
                        )
        );
    }
}