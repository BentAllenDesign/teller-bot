package com.bad.studios.tellerbot.events;

import com.bad.studios.tellerbot.models.UserData;
import com.bad.studios.tellerbot.service.TicketService;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import lombok.var;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@PropertySource("classpath:application.yaml")
public class GuildJoinEvent extends ReactiveEventAdapter {

    @Value("${discord.guildid}")
    private String guildId;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private LogEvent logEvent;

    @Override
    public Publisher<?> onMemberJoin(MemberJoinEvent event) {

        /* COMMAND CHECK */
        if (!event.getGuildId().asString().equals(guildId))
            return Mono.empty();

        /* SYSTEM ENTITY CHECK && ACCESS LOGIC */
        var joiningUser = ticketService.createUserData(new UserData(event.getMember(), 0));
        if(joiningUser == null)
            return logEvent.logError(event);

        return Mono.empty();
    }

}
