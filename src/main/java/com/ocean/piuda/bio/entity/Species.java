package com.ocean.piuda.bio.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


/**
 * 모든 해양 활동에서 공통 참조 위한 종 엔티티
 */
@Entity
@Table(name = "species")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Species extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "species_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // 국명 (예: 감태)

    public void update(String name) {
        if (name != null && !name.isBlank())  this.name = name;
    }
}