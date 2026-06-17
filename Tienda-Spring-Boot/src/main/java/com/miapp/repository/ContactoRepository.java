package com.miapp.repository;

import com.miapp.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactoRepository extends JpaRepository<Contacto, Integer> {
}