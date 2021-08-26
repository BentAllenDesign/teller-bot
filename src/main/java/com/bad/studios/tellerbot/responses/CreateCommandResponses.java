package com.bad.studios.tellerbot.responses;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class CreateCommandResponses {

    public Mono<Void> test(ReactionAddEvent reaction) {
        return Mono.just(reaction)
                .flatMap(ReactionAddEvent::getMessage)
                .filter(m -> m.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(m -> m.getContent().equalsIgnoreCase("Whatever..."))
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createMessage("This was something."))
                .then();
    }

}
