package com.bad.studios.tellerbot.events.update;

import com.bad.studios.tellerbot.events.LogEvent;
import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.service.TicketService;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.MemberUpdateEvent;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@PropertySource("classpath:application.yaml")
public class UpdateNicknameGuildEvent extends ReactiveEventAdapter {

    @Value("${discord.guildid}")
    private String guildId;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Override
    public Publisher<?> onMemberUpdate(MemberUpdateEvent event) {

        /* COMMAND CHECK */
        if (!event.getGuildId().asString().equals(guildId))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK && ACCESS LOGIC */
        var interactingUser = ticketService.getUserData(event.getMemberId().asString());
        if(interactingUser == null) {
            ticketService.createUserData(new UserData(event.getMember().block(), 0));
            return Mono.empty();
        }

        /* INTENDED EVENT RESPONSE */
        event.getCurrentNickname()
                .filter(x -> !x.equals(interactingUser.getPreferredName()))
                .ifPresent(x -> {
                    logEvent.logInfo(event, interactingUser.getPreferredName() + " changed their name to " + x);
                    ticketService.updateUser(interactingUser.setPreferredName(x));
                });
        return Mono.empty();
    }
}