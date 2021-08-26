package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.service.TicketService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.var;
import org.javatuples.Triplet;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.yaml")
public class ReadAllTicketsSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;

    @Value("${discord.roles.admin}")
    private String adminRole;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {
        if (event.getCommandName().equals("read-all-tickets") &&
                event.getInteraction().getMember().get().getRoleIds().contains(Snowflake.of(adminRole))) {

            var allTickets = ticketService.getAllTickets();
            List<Triplet<String, String, Boolean>> ticketTriplets = new ArrayList<>();
            for (int i = 0; i < allTickets.size(); i++) {
                var ticket = allTickets.get(i);
                if(i != 0 && i % 3 == 0) {
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

            EmbedCreateSpec.Builder spec = EmbedCreateSpec.builder()
                    .title("Ticket List");

            for(var triplet : ticketTriplets) {
                spec = spec.addField(
                        triplet.getValue0(),
                        triplet.getValue1(),
                        triplet.getValue2()
                );
            }

            /* EVENT REPLY */
            return event.reply()
                    .withEmbeds(spec.build());
        }
        return Mono.empty();
    }
}
