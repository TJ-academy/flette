package com.example.flette.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPageStatsDTO {
    private int ordersCount;
    private int reviewsCount;
    private int questionsCount;
}
