package com.bad.studios.tellerbot.models;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "User_Data")
public class UserData {

    @Id
    @NonNull
    private String id;
    private String preferredName;
    private String username;
    private String mentionString;
    @NonNull
    private Integer tickets;

    public UserData(User user, Integer tickets) {
        id = user.getId().asString();
        username = user.getUsername();
        mentionString = user.getMention();
        this.tickets = tickets;
    }

    public UserData(Member user, Integer tickets) {
        preferredName = user.getNickname().orElse(user.getUsername());
        id = user.getId().asString();
        username = user.getUsername();
        mentionString = user.getMention();
        this.tickets = tickets;
    }

    public UserData setId(String id) {
        this.id = id;
        return this;
    }

    public UserData setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserData setTickets(Integer amount) {
        this.tickets = amount;
        return this;
    }

    public UserData setMentionString(String mentionString) {
        this.mentionString = mentionString;
        return this;
    }

    public UserData setPreferredName(String preferredName) {
        this.preferredName = preferredName;
        return this;
    }

}
