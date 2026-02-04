package com.ocean.piuda.admin.site.entity;

import com.ocean.piuda.global.api.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "site_name_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class SiteNameOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_option_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public void update(String name, Boolean isActive) {
        if (name != null) this.name = name;
        if (isActive!= null) this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }
}