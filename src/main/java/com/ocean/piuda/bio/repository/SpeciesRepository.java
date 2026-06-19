package com.ocean.piuda.bio.repository;


import com.ocean.piuda.bio.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<Species, Long> {
    Optional<Species> findByName(String name);
    List<Species> findByNameContainingIgnoreCase(String name);
}