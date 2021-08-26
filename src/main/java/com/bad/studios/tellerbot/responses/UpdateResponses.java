package com.bad.studios.tellerbot.responses;

import com.bad.studios.tellerbot.service.PreferredNameRequestService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.MessageReferenceData;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

@PropertySource("classpath:application.yaml")
public abstract class UpdateResponses {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private PreferredNameRequestService preferredNameRequestService;

    public Mono<Void> updatePreferredNameCommand(Message message, User user) {

        AtomicReference<Embeds.ErrorEmbedContents> errorDetails = new AtomicReference<>();
        AtomicReference<String> preferredName = new AtomicReference<>();

        return Mono.just(message)
                .filter(msg ->
                        !msg.getAuthor().get().isBot() &&
                        msg.getContent().startsWith("!update name ")
                )
                .map(msg -> {
                    var name = msg.getContent().substring("!update name ".length() - 1);

                    if(name.length() > 0) {

                        preferredName.set(name);

                        var ticket = ticketService.getOptionalTicketsById(msg.getAuthor().get().getId().asString());
                        if(ticket.isPresent()) {
                            var updateTicket = ticket.get();
                            updateTicket.setPreferredName(name);

                            try {
                                ticketService.updateTicket(updateTicket);
                            } catch (Exception e) {
                                errorDetails.set(new Embeds.ErrorEmbedContents()
                                        .setMessage(msg)
                                        .setException(e)
                                );
                            }
                        }
                        else {
                            errorDetails.set(new Embeds.ErrorEmbedContents()
                                    .setMessage(msg)
                                    .setException(new EntityNotFoundException(
                                            "Could not find ticket object by supplied id: **" +
                                                    msg.getAuthor().get().getId().asString() + "**"
                                    ))
                            );
                        }
                    }

                    return msg;
                })
                .flatMap(Message::getChannel)
                .flatMap(channel -> channel.createEmbed(spec -> {
                    var error = errorDetails.get();
                    if(error != null) {
                        Embeds.errorEmbed(spec, error.getMessage(), error.getException());
                        return;
                    }

                    Embeds.infoEmbed(
                            spec,
                            user,
                            new Embeds.EmbedContents(
                                    "Great!",
                                    "We've changed your name to **" + preferredName.get() + "**",
                                    "Delivered",
                                    new ArrayList<>()
                            )
                    );
                }))
                .then();
    }

}
