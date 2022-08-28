package com.marco.cartorios.repositories;

import com.marco.cartorios.domain.Cartorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartorioRepository extends JpaRepository<Cartorio, Integer> {
}
