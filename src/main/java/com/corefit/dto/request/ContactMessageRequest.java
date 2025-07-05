package com.corefit.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessageRequest {
    private String name;
    private String email;
    private String subject;
    private String message;
}
