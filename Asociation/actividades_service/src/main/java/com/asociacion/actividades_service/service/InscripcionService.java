package com.asociacion.actividades_service.service;

import org.springframework.stereotype.Service;
import com.asociacion.actividades_service.repository.InscripcionRepository;
import com.asociacion.actividades_service.model.Inscripcion;
import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {
    private InscripcionRepository repository;

    public InscripcionService(InscripcionRepository repository) {
        this.repository = repository;
    }

    public List<Inscripcion> getAllInscripciones() {
        return repository.findAll();
    }

    public Optional<Inscripcion> getInscripcionById(Integer id) {
        return repository.findById(id);
    }

    public Inscripcion inscribirSocio(Inscripcion inscripcion) {
        return repository.save(inscripcion);
    }

    public Inscripcion crearInscripcion(Inscripcion inscripcion) {
        return repository.save(inscripcion);
    }
}