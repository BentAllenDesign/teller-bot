package com.bad.studios.tellerbot.events.create;

import com.bad.studios.tellerbot.models.RaffleEntry;
import com.bad.studios.tellerbot.service.RaffleService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import discord4j.common.util.Snowflake;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.MessageEditSpec;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@PropertySource("classpath:application.yaml")
public class CreateRaffleMessageSyncEvent extends ReactiveEventAdapter {

    @Autowired
    private RaffleService raffleService;
    @Autowired
    private TicketService ticketService;

    @Value("#{'${discord.roles.admin}'.split(',')}")
    private List<String> adminRoles;

    @SneakyThrows
    @Override
    public Publisher<?> onMessageCreate(MessageCreateEvent event) {

        if(event.getMessage().getEmbeds().isEmpty())
            return Mono.empty();
        if(!event.getMessage().getEmbeds().get(0).getTitle().map(x -> x.equals("Raffle Info")).orElse(false))
            return Mono.empty();


        event.getMessage().pin().block();


        val raffleEmbed = event.getMessage().getEmbeds().get(0);
        val raffleEmbedFields = raffleEmbed.getFields();
        val raffle = raffleService.getRaffleByTitle(raffleEmbedFields.get(0).getName());
        if(raffle == null)
            return event.getMessage()
                    .edit(MessageEditSpec
                            .builder()
                            .embeds(Collections.singletonList(Embeds.errorEmbed("Failed to fetch raffle data - contact system admin - `CreateRaffleMessageSyncEvent#onMesageCreate`")))
                            .build());


        val endTime = raffle.getEndTime();
        var duration = Duration.between(LocalDateTime.now(), endTime);


        long currentSecond = 0;
        long seconds;


        while(raffleService.getRaffleByIdSoft(raffle.getId()) != null && duration.getSeconds() >= 0) {

            seconds = duration.getSeconds();

            if(currentSecond != seconds && seconds % 2 == 0) {
                final List<RaffleEntry> currentRaffleEntries = raffleService.getRaffleEntriesByRaffleId(raffle.getId());

                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("```\n");
                currentRaffleEntries.stream()
                        .filter(x -> x.getAmount() > 0)
                        .forEach(x ->{
                            val user = ticketService.getUserData(x.getUserId());
                            if(user == null)
                                return;

                            val name = user.getPreferredName();
                            stringBuilder
                                    .append(name)
                                    .append("...................................".substring(name.length()))
                                    .append(x.getAmount())
                                    .append("\n");
                        });
                stringBuilder.append("```");

                event.getMessage()
                        .edit(MessageEditSpec
                                .builder()
                                .embeds(Arrays.asList(
                                        Embeds.infoEmbed(
                                                raffleEmbed.getTitle().get(),
                                                raffleEmbed.getDescription().get(),
                                                raffleEmbedFields.stream().map(x -> EmbedCreateFields.Field.of(x.getName(), x.getValue(), x.isInline())).collect(Collectors.toList())
                                        ),
                                        Embeds.infoEmbed(
                                                "Entries",
                                                stringBuilder.toString().length() > 10 ? stringBuilder.toString() : "No entries to display for right now"
                                        ),
                                        Embeds.infoEmbed(
                                                String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60)),
                                                ""
                                        )
                                ))
                                .build()
                        )
                        .block();

                currentSecond = seconds;
            }
            Thread.sleep(100);

            duration = Duration.between(LocalDateTime.now(), endTime);

        }


        if (raffleService.getRaffleByIdSoft(raffle.getId()) == null)
            return event.getMessage()
                    .edit(MessageEditSpec
                            .builder()
                            .embeds(Collections.singletonList(Embeds.errorEmbed("This raffle was deleted")))
                            .build());


        val winnerId = raffleService.declareRaffleWinner(raffle.getId());

        if(raffleService.getRaffleEntriesByRaffleId(raffle.getId()).isEmpty())
            return event.getMessage()
                    .edit(MessageEditSpec
                            .builder()
                            .embeds(Collections.singletonList(Embeds.warningEmbed("That's weird", "No one participated in this raffle")))
                            .build());

        if (winnerId == null)
            return event.getMessage()
                    .edit(MessageEditSpec
                            .builder()
                            .embeds(Collections.singletonList(Embeds.errorEmbed("Failed to declare raffle winner - contact system admin - `CreateRaffleMessageSyncEvent#onMesageCreate`")))
                            .build());

        val winner = ticketService.getUserData(winnerId);
        if (winner == null)
            return event.getMessage()
                    .edit(MessageEditSpec
                            .builder()
                            .embeds(Collections.singletonList(Embeds.errorEmbed("Failed to fetch winner details - contact system admin - `CreateRaffleMessageSyncEvent#onMesageCreate`")))
                            .build());

        val user = event.getClient().getUserById(Snowflake.of(winner.getId())).block();
        return event.getMessage()
                .edit(MessageEditSpec
                        .builder()
                        .embeds(Arrays.asList(
                                Embeds.successEmbed(
                                        "Raffle Info",
                                        raffleEmbed.getDescription().get(),
                                        raffleEmbedFields.stream().map(x -> EmbedCreateFields.Field.of(x.getName(), x.getValue(), x.isInline())).collect(Collectors.toList())
                                ),
                                Embeds.successEmbed(
                                        "Congratulations!",
                                        user.getMention() + " is the winner of this raffle"
                                )
                        ))
                        .build());

    }

}
