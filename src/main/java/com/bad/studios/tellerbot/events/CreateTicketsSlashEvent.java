package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.models.Ticket;
import com.bad.studios.tellerbot.service.TicketService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@PropertySource("classpath:application.yaml")
public class CreateTicketsSlashEvent extends ReactiveEventAdapter {

    @Autowired
    private TicketService ticketService;

    @Value("${discord.roles.admin}")
    private String adminRole;

    @Override
    public Publisher<?> onSlashCommand(SlashCommandEvent event) {
        if (event.getCommandName().equals("create-tickets") &&
                event.getInteraction().getMember().get().getRoleIds().contains(Snowflake.of(adminRole))) {

            /* REQUIRED OPTIONS */
            var user = event.getOption("user").get().getValue().get().asUser().block();
            var amount = Integer.parseInt(event.getOption("amount").get().getValue().get().getRaw());

            /* OPTIONAL OPTIONS */
            var reasonOption = event.getOption("reason");
            String reason = "";
            if(reasonOption.isPresent())
                reason = reasonOption.get().getValue().get().asString();

            /* SERVICE CALL */
            ticketService.saveTickets(new Ticket(
                    user,
                    amount
            ));

            /* EVENT REPLY */
            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(
                            EmbedCreateSpec.builder()
                                    .title("Nice!")
                                    .description("You used the create-tickets command because " + reason)
                                    .build()
                    );
        }
        return Mono.empty();
    }
}
