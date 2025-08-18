package com.example.flette.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "decoration", indexes = @Index(name = "idx_decoration_id", columnList = "decorationId"))
public class Decoration {
    @Id
    private int decorationId;

    private String decorationName;
    private Integer utilPrice; // 단가
    private String description;

    private String category; // ✅ 새로 추가
    private boolean show;    // ✅ 새로 추가 (노출 여부)
}
