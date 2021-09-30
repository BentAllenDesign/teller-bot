package com.bad.studios.tellerbot.models;

import com.bad.studios.tellerbot.events.create.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "Raffle")
public class Raffle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    private Integer length;

    // TODO: Change to Ticket type for consistency
    @OneToMany
    @NonNull
    private List<RaffleEntry> tickets;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy-MM-dd hh:mm:ss a")
    private LocalDateTime startTime;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yy-MM-dd hh:mm:ss a")
    private LocalDateTime endTime;

    /**
     * If you change plusMinutes to any other time variant such as plusHours or plusDays,
     * make sure to repeat the change in:
     * @see CreateRaffleSlashEvent Second TODO item
     */
    public Raffle(String title, String description, Integer length, List<RaffleEntry> tickets, boolean start) {
        this.title = title;
        this.description = description;
        this.length = length;
        this.tickets = tickets;

        startTime = LocalDateTime.now();
        endTime = startTime.plusMinutes(length);
    }
}
