package com.corefit.dto.response.market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopProductStats {
    private Long id;
    private String label;
    private Long value;
}
