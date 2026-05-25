package com.asociacion.socios_service.service;

import java.util.List;
import java.util.Optional;
import com.asociacion.socios_service.model.Socios;
import com.asociacion.socios_service.repository.SocioRepository;
import org.springframework.stereotype.Service;

@Service
public class SocioService {
    private final SocioRepository repository;

    public SocioService(SocioRepository repository) {
        this.repository = repository;
    }

    public List<Socios> getAllSocios() {
        return repository.findAll();
    }

    public Optional<Socios> getSocioById(int id) {
        return repository.findById(id);
    }

    public Socios createSocio(Socios socio) {
        return repository.save(socio);
    }
}