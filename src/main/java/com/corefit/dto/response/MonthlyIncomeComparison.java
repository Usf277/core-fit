package com.corefit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MonthlyIncomeComparison {
    private String month;
    private double storeIncome;
    private double reservationIncome;
}