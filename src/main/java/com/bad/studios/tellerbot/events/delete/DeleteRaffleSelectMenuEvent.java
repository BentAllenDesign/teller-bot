package com.bad.studios.tellerbot.events.delete;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.models.RaffleDeleteRequest;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * This class is set up for the planned raffle deletion request
 * functionality in {@link RaffleService}
 *
 */
//@Service
//@PropertySource("classpath:application.yaml")
public class DeleteRaffleSelectMenuEvent extends ReactiveEventAdapter {

    /*
    @Autowired
    private RaffleService raffleService;
    @Autowired
    private LogEvent logEvent;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @Override
    public Publisher<?> onSelectMenuInteract(SelectMenuInteractEvent event) {
        var embed = event.getMessage().get().getEmbeds();
        if(embed.isEmpty() || !embed.get(0).getTitle().get().equals("Delete Raffle"))
            return Mono.empty();

        if (Collections.disjoint(event.getInteraction().getMember().get().getRoleIds(), adminRoles.stream().map(Snowflake::of).collect(Collectors.toList())))
            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(Embeds.errorEmbed("Insufficient permissions"));

        if (raffleService.getAllRafflesSoft().isEmpty())
            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(Embeds.errorEmbed("There doesn't seem to be any raffle data"));

        raffleService.createRaffleDeleteRequest(new RaffleDeleteRequest(event.getMessage().get().getId().asLong(), event.getInteraction().getMember().get().getId().asLong()));

        return event.getInteractionResponse().editInitialResponse(
                WebhookMessageEditRequest.builder()
                        .addEmbed(Embeds.dangerEmbedData("Are you sure you want to delete this raffle?", "This action cannot be undone"))
                        .components(Collections.singletonList(
                                ActionRow.of(
                                        Button.danger(event.getValues().get(0), "Do it"),
                                        Button.primary("no-comply", "Maybe not")
                                ).getData())
                        )
                        .build()
        );
    }
     */
}
