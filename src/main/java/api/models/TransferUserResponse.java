package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class TransferUserResponse extends BaseModel{
    private String message;
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;

}
