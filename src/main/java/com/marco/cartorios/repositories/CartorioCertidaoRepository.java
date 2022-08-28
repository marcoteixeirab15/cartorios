package com.marco.cartorios.repositories;

import com.marco.cartorios.domain.Cartorio;
import com.marco.cartorios.domain.CartorioCertidao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartorioCertidaoRepository extends JpaRepository<CartorioCertidao, Integer> {

    @Query
    void deleteCartorioCertidaoByCartorio_Id(int cartorioId);

}
