package com.asociacion.actividades_service.service;

import org.springframework.stereotype.Service;
import com.asociacion.actividades_service.repository.ActividadesRepository;
import com.asociacion.actividades_service.model.Actividad;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ActividadService {
    private ActividadesRepository repository;

    public ActividadService(ActividadesRepository repository) {
        this.repository = repository;
    }

    public List<Actividad> getAllActividades() {
        return repository.findAll();
    }

    public Optional<Actividad> getActividadById(Integer id) {
        return repository.findById(id);
    }

    public Optional<Actividad> getActividadesById(Integer id) {
        return getActividadById(id);
    }

    public Actividad crearActividades(Actividad actividad) {
        return repository.save(actividad);
    }
}