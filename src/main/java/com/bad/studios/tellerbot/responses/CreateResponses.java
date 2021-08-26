package com.bad.studios.tellerbot.responses;

import com.bad.studios.tellerbot.models.PreferredNameRequest;
import com.bad.studios.tellerbot.models.Ticket;
import com.bad.studios.tellerbot.service.PreferredNameRequestService;
import com.bad.studios.tellerbot.service.TicketService;
import com.bad.studios.tellerbot.utils.Embeds;
import com.bad.studios.tellerbot.utils.MentionMatcher;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.MessageReference;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.MessageReferenceData;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import lombok.var;
import org.javatuples.Triplet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@PropertySource("classpath:application.yaml")
public abstract class CreateResponses {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private PreferredNameRequestService preferredNameRequestService;

    @Value("${discord.roles.admin}")
    private String adminRole;

    public Mono<Void> test(ReactionAddEvent reaction) {
        return Mono.just(reaction)
                .flatMap(ReactionAddEvent::getMessage)
                .filter(m -> m.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(m -> m.getContent().equalsIgnoreCase("Whatever..."))
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createMessage("This was something."))
                .then();
    }

    public Mono<Void> createTicketsCommand(Message message, User user, GatewayDiscordClient client) {

        AtomicInteger amount = new AtomicInteger();
        AtomicReference<List<User>> grantedUser = new AtomicReference<>();
        AtomicReference<Embeds.ErrorEmbedContents> errorDetails = new AtomicReference<>();
        AtomicReference<List<User>> newEntries = new AtomicReference<>();

        return Mono.just(message)
                .filterWhen(m -> m.getAuthorAsMember()
                        .map(Member::getRoleIds)
                        .map(x -> x.contains(Snowflake.of(adminRole)))
                        .hasElement()
                )
                .filter(m -> {
                    if(m.getAuthor().map(a -> !a.isBot()).orElse(false) &&
                            m.getContent().startsWith("!create tickets")) {
                        System.out.println("Create Tickets Command Called");

                        var mentions = m.getUserMentions();
                        if(mentions.isEmpty()) {
                            errorDetails.set(new Embeds.ErrorEmbedContents()
                                    .setMessage(m)
                                    .setException(new EntityNotFoundException(
                                            "Could not find ticket object by supplied mention string: " +
                                                    m.getContent().split(" ")[2]
                                    ))
                            );
                            return true;
                        }
                        var split = m.getContent().split(" ");
                        var args = Arrays.stream(split)
                                .skip(2)
                                .filter(mention -> !MentionMatcher.matchMentionOrFullName(mention));

                        amount.set(Integer.parseInt(args.findFirst().orElse("0")));
                        grantedUser.set(mentions);

                        List<User> newUsers = new ArrayList<>();

                        mentions.forEach(x -> {
                            if(!ticketService.getOptionalTicketsById(x.getId().asString()).isPresent())
                                newUsers.add(x);

                            ticketService.saveTickets(
                                    new Ticket()
                                            .setId(x.getId().asString())
                                            .setUsername(x.getUsername())
                                            .setMentionString(x.getMention())
                                            .setTickets(amount.get())
                            );
                        });

                        newEntries.set(newUsers);

                        return true;
                    }
                    return false;
                })
                .flatMap(Message::getChannel)
                .flatMap(c -> c.createEmbed(spec -> {

                    // Catches errors that may happen in the filter step
                    var error = errorDetails.get();
                    if(error != null) {
                        Embeds.errorEmbed(spec, error.getMessage(), error.getException());
                        return;
                    }

                    // Successful command logic
                    var grantedUsers = grantedUser.get();
                    List<Triplet<String, String, Boolean>> userTriplets = new ArrayList<>();
                    for (User u : grantedUsers) {
                        userTriplets.add(Triplet.with(
                                u.getUsername(),
                                "** **",
                                false
                        ));
                    }
                    Embeds.successEmbed(
                            spec,
                            user,
                            new Embeds.EmbedContents(
                                    "Congrats!",
                                    amount + " ticket(s) given to users:",
                                    "Awarded",
                                    userTriplets
                            )
                    );

                }))
                .flatMapIterable(x -> newEntries.get())
                .flatMap(User::getPrivateChannel)
                .flatMap(c -> {
                    var sentUser = c.getRecipients().stream().filter(x -> !x.isBot()).findFirst().get();
                    var embed = c.createMessage(spec -> spec
                            .addEmbed(embedSpec -> Embeds.infoEmbed(
                                    embedSpec,
                                    sentUser,
                                    new Embeds.EmbedContents(
                                            "Hey there!",
                                            "Looks like you were just added to our system! We have your name set as **" + sentUser.getUsername() + "**.\n\nWould you like to set a preferred name instead?\n",
                                            "Asked"
                            )))
                            .setComponents(
                                    ActionRow.of(
                                            Button.primary(sentUser.getId().asString() + "-preferred-yes-button", "Set Preferred Name"),
                                            Button.secondary(sentUser.getId().asString() + "-preferred-no-button", "No Thanks")
                                    )
                            ));
                    return embed
                            .map(Message::getId)
                            .flatMapMany(buttonMessageId -> client.on(ButtonInteractEvent.class, event ->
                                    Mono.justOrEmpty(event.getInteraction().getMessage())
                                            .map(Message::getId)
                                            .filter(buttonMessageId::equals)
                                            .flatMap(x -> event.edit(spec -> {
                                                if(event.getCustomId().equals(event.getInteraction().getUser().getId().asString() + "-preferred-yes-button")) {
                                                    spec.addEmbed(embedSpec -> Embeds.infoEmbed(
                                                            embedSpec,
                                                            user,
                                                            new Embeds.EmbedContents(
                                                                    "We can do that!",
                                                                    "Type `!update name <NAME_HERE>` to set your preferred name.",
                                                                    "Delivered"
                                                            )));
                                                }
                                                else {
                                                    spec.addEmbed(embedSpec -> Embeds.infoEmbed(
                                                            embedSpec,
                                                            user,
                                                            new Embeds.EmbedContents(
                                                                    "Cool!",
                                                                    "We'll stick with " + user.getUsername() + " for now! You can change this at any time using the command:\n```!update preferred name```",
                                                                    "Delivered"
                                                            )));
                                                }
                                                spec.setComponents(new ArrayList<>());
                                            }))
                                    )
                            )
                            .then();
                })
                .then();
    }
}
