package com.corefit.dto.request.playground;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaygroundRateRequest {
    private long id;
    private String comment;
    private Integer rate; // 0 : 5
    private Long playgroundId;
}
