package com.corefit.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class WalletTransactionResponse {
    private Long id;
    private Long userId;
    private String userName;

    private String type;
    private double amount;

    private String purpose;
    private LocalDateTime timestamp;
}
