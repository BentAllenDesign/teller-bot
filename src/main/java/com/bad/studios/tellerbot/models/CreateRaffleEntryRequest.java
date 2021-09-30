package com.bad.studios.tellerbot.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "Create_Raffle_Entry_Request")
public class CreateRaffleEntryRequest {

    @Id
    @NonNull
    private Long messageId;
    @NonNull
    private Long userId;
    private Long timeout = Instant.now().getEpochSecond() + 300;

}
