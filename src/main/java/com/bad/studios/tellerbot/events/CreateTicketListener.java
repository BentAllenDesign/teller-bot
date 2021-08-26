package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.responses.CreateResponses;
import com.bad.studios.tellerbot.utils.EventListener;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

//@Service
public class CreateTicketListener extends CreateResponses implements EventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        if(!event.getMember().isPresent())
            return Mono.just(false).then();
        return createTicketsCommand(event.getMessage(), event.getMember().get(), event.getClient());
    }
}
