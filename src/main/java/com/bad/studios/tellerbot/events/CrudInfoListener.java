package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.utils.EventListener;
import com.bad.studios.tellerbot.responses.CrudResponse;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

//@Service
public class CrudInfoListener extends CrudResponse implements EventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        if(!event.getMember().isPresent())
            return Mono.just(false).then();
        return crudCommand(event.getMessage(), event.getMember().orElse(null));
    }
}
