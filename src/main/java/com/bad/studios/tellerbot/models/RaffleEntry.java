package com.bad.studios.tellerbot.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "Raffle_Entry")
public class RaffleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NonNull
    private Integer raffleId;
    @NonNull
    private String userId;
    @NonNull
    private Integer amount;

}
