package com.autolavado.autolavadomvc.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.autolavado.autolavadomvc.model.Servicio;
import com.autolavado.autolavadomvc.repository.ServicioRepository;

@Service
public class CatalogoServiciosService {

    private final ServicioRepository servicioRepository;

    public CatalogoServiciosService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public List<Servicio> listarServicios() {
        return servicioRepository.findAll(Sort.by(Sort.Direction.ASC, "servicio"));
    }

    public Servicio buscarPorId(Integer id) {
        return id == null ? null : servicioRepository.findById(id).orElse(null);
    }
}