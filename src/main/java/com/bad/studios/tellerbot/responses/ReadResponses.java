package com.bad.studios.tellerbot.responses;

import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.var;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@PropertySource("classpath:application.yaml")
public abstract class ReadResponses {

    @Autowired
    private TicketService service;

    @Value("${discord.roles.admin}")
    private String adminRole;

    public Mono<Void> readAllTicketsCommand(Message message, User user) {
        return Mono.just(message)
                .filter(m -> m.getAuthor().map(u -> !u.isBot()).orElse(false) &&
                            m.getContent().equalsIgnoreCase("!read all tickets")
                )
                .filterWhen(m -> {
                    System.out.println("Read All Tickets Command Called");
                    return m.getAuthorAsMember()
                                    .map(Member::getRoleIds)
                                    .map(x -> x.contains(Snowflake.of(adminRole)))
                                    .hasElement();
                })
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> {
                    var allTickets = service.getAllTickets();
                    List<Triplet<String, String, Boolean>> ticketTriplets = new ArrayList<>();
                    for (int i = 0; i < allTickets.size(); i++) {
                        var ticket = allTickets.get(i);
                        if (i != 0 && i % 3 == 0) {
                            ticketTriplets.add(Triplet.with(
                                    "** **",
                                    "** **",
                                    false
                            ));
                        }
                        ticketTriplets.add(Triplet.with(
                                ticket.getPreferredName() == null ? ticket.getUsername() : ticket.getPreferredName(),
                                ticket.getTickets().toString() + " ticket" + (ticket.getTickets() != 1 ? "s" : ""),
                                true
                        ));
                    }
                    Embeds.infoEmbed(
                            spec,
                            user,
                            new Embeds.EmbedContents(
                                    "",
                                    "",
                                    "Delivered",
                                    ticketTriplets
                            )
                    );
                }))
                .then();
    }

    public Mono<Void> readTicketsCommand(Message message, User user) {

        AtomicBoolean error = new AtomicBoolean(false);
        AtomicInteger amount = new AtomicInteger();

        AtomicReference<Mono<Void>> errorEmbed = new AtomicReference<>();

        return Mono.just(message)
                .filter(m -> m.getAuthor().map(u -> !u.isBot()).orElse(false))
                .filter(m -> {
                    if(m.getContent().equalsIgnoreCase("!read tickets")) {
                        System.out.println("Read Tickets Command Called");
                        try {
                            amount.set(service.getTicketsByUserId(user.getId().asString()).getTickets());
                        } catch (Throwable e) {
                            /*errorEmbed.set(Embeds.errorEmbed(m, e));
                            errorEmbed.get().then();*/
                            Embeds.errorEmbed(m, e);
                            error.set(true);
                        }
                        return true;
                    }
                    return false;
                })
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> Embeds.infoEmbed(
                        spec,
                        user,
                        new Embeds.EmbedContents(
                                "Oh, nice!",
                                "You have " + amount.get() + " ticket(s), " + user.getMention(),
                                "Delivered",
                                new ArrayList<>()
                ))))
                .then();
    }

    public Mono<Void> readTicketsByMentionCommand(Message message, User user) {

        AtomicReference<Embeds.ErrorEmbedContents> errorDetails = new AtomicReference<>();
        AtomicInteger amount = new AtomicInteger();

        return Mono.just(message)
                .filter(m -> {
                    if(m.getAuthor().map(u -> !u.isBot()).orElse(false) &&
                            m.getContent().split(" ").length == 3) {

                        var mention = m.getUserMentions();

                        if(m.getContent().startsWith("!read tickets")) {
                            System.out.println("Read Tickets By Mention Command Called");
                            if(mention.isEmpty()) {
                                errorDetails.set(new Embeds.ErrorEmbedContents()
                                        .setMessage(m)
                                        .setException(new EntityNotFoundException(
                                                "Could not find ticket object by supplied mention string: " +
                                                m.getContent().split(" ")[2]
                                        ))
                                );
                                return true;
                            }
                            try {
                                amount.set(service.getTicketsByUserId(mention.get(0).getId().asString()).getTickets());
                            } catch (Exception e) {
                                errorDetails.set(new Embeds.ErrorEmbedContents()
                                        .setMessage(m)
                                        .setException(e)
                                );
                            }
                            return true;
                        }
                    }
                    return false;
                })
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> {
                    var error = errorDetails.get();
                    if(error != null) {
                        Embeds.errorEmbed(spec, error.getMessage(), error.getException());
                        return;
                    }
                    Embeds.infoEmbed(
                            spec,
                            user,
                            new Embeds.EmbedContents(
                                    "Oh, nice!",
                                    "You have " + amount.get() + " ticket(s), " + user.getMention(),
                                    "Delivered",
                                    new ArrayList<>()
                            )
                    );
                }))
                .then();
    }
}
