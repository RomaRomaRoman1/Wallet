package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Wallet {

    @Id
    private UUID id;
    private BigDecimal balance;
    // Обозначает поле, используемое для оптимистичной блокировки. Это поле автоматически увеличивается при каждом обновлении.
    @Version
    private Long version;

    public Wallet(UUID id, BigDecimal balance, Long version, Client client) {
        this.id = id;
        this.balance = balance;
        this.version = version;
        this.client = client;
    }

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;
    @OneToMany(mappedBy = "wallet")
    private List<Purchase> purchases;
}