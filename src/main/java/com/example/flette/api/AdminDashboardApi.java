package com.example.flette.api;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminDashboardApi {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();

        // ===== 리뷰 =====
        Integer totalReviews = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM review", Integer.class
        );
        Integer todayReviews = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM review WHERE DATE(review_date) = CURDATE()", Integer.class
        );

        // ===== 회원 =====
        Integer totalMembers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM member", Integer.class
        );
        Integer todayMembers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM member WHERE DATE(joined_at) = CURDATE()", Integer.class
        );

        // ===== 주문 =====
        Integer totalOrders = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders", Integer.class
        );
        Integer todayOrders = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE DATE(order_date) = CURDATE()", Integer.class
        );

        // ===== 문의 =====
        Integer totalQuestions = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM question", Integer.class
        );
        Integer unansweredQuestions = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM question WHERE status = b'0'", Integer.class
        );

        // ===== 결과 =====
        result.put("totalReviews", totalReviews);
        result.put("todayReviews", todayReviews);
        result.put("totalMembers", totalMembers);
        result.put("todayMembers", todayMembers);
        result.put("totalOrders", totalOrders);
        result.put("todayOrders", todayOrders);
        result.put("totalQuestions", totalQuestions);
        result.put("unansweredQuestions", unansweredQuestions);

        return result;
    }
}
