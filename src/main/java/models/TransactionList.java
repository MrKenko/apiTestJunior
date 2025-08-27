package models;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TransactionList {
    private int id;
    private double amount;
    private TransactionType type;
    private String timestamp;
    private int relatedAccountId;
}
