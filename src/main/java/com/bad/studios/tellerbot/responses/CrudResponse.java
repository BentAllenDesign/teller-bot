package com.bad.studios.tellerbot.responses;

import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.Arrays;

//@Component
public abstract class CrudResponse {

    @Autowired
    private TicketService service;

    public Mono<Void> crudCommand(Message message, Member member) {
        return Mono.just(message)
                .filter(m -> m.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(m -> m.getContent().equalsIgnoreCase("!crud"))
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> Embeds.infoEmbed(
                        spec,
                        member,
                        new Embeds.EmbedContents(
                                "**CRUD INFO**",
                                "Commands list",
                                "CRUD info delivered",
                                Arrays.asList(
                                        Triplet.with(
                                                "create",
                                                " - create tickets <MENTION> <AMOUNT>\n" +
                                                " - create raffle <TITLE> <DESCRIPTION> <HOURS>\n",
                                                false
                                        ),
                                        Triplet.with(
                                                "read",
                                                " - read tickets <MENTION>\n" +
                                                " - read tickets\n",
                                                false
                                        ),
                                        Triplet.with(
                                                "update",
                                                " - update tickets <MENTION> <AMOUNT> <REASON>\n" +
                                                " - update raffle <ID> <NEW_TITLE> <NEW_DESC>\n" +
                                                " - update raffle title <ID> <NEW_TITLE>\n" +
                                                " - update raffle description <ID> <NEW_DESC>\n",
                                                false
                                        ),
                                        Triplet.with(
                                                "delete",
                                                " - delete tickets <MENTION> <AMOUNT> <REASON>\n" +
                                                " - delete raffle <ID> <REASON>\n",
                                                false
                                        )
                                )
                        )
                )))
                .then();
    }
}
