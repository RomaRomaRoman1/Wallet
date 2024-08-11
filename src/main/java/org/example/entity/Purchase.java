package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@Data
@AllArgsConstructor
public abstract class Purchase {
    @Id
    private UUID id;
    private BigDecimal amount;
    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;
}
