package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Version;


import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    private UUID id;
    private BigDecimal balance;
    // Обозначает поле, используемое для оптимистичной блокировки. Это поле автоматически увеличивается при каждом обновлении.
    @Version
    private Long version;
}