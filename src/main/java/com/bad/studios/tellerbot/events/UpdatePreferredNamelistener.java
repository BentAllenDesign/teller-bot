package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.responses.UpdateResponses;
import com.bad.studios.tellerbot.utils.EventListener;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

//@Service
public class UpdatePreferredNamelistener extends UpdateResponses implements EventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return updatePreferredNameCommand(event.getMessage(), event.getMessage().getAuthor().orElse(null));
    }
}
