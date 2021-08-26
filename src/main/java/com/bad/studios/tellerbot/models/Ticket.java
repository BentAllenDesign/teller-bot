package com.bad.studios.tellerbot.models;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "Ticket")
public class Ticket {

    @Id
    private String id;
    private String preferredName;
    private String username;
    private String mentionString;
    private Integer tickets;

    public Ticket (User user, Integer amount) {
        id = user.getId().asString();
        username = user.getUsername();
        mentionString = user.getMention();
        tickets = amount;
    }

    public Ticket setId(String id) {
        this.id = id;
        return this;
    }

    public Ticket setUsername(String username) {
        this.username = username;
        return this;
    }

    public Ticket setTickets(Integer amount) {
        this.tickets = amount;
        return this;
    }

    public Ticket setMentionString(String mentionString) {
        this.mentionString = mentionString;
        return this;
    }

    public Ticket setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        return this;
    }

}
