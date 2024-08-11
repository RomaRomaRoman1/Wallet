package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@ToString(exclude = {"email"})
public class Client {
    @Id
    private UUID id;

    public Client(UUID id, String username, String userSurname, String email) {
        this.id = id;
        this.username = username;
        this.userSurname = userSurname;
    }
    private String email;
    private String username;
    private String userSurname;
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Wallet> wallets;
}
