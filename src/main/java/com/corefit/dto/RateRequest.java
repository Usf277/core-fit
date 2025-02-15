package com.corefit.dto;

import com.corefit.entity.Market;
import com.corefit.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
public class RateRequest {
    private long id;
    private String comment;
    private Integer rate; // 0 : 5
    private long userId;
    private long marketId;
}
