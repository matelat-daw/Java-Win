package com.autolavado.autolavadomvc.controller;

import com.autolavado.autolavadomvc.controller.api.ApiDtoMapper;
import com.autolavado.autolavadomvc.controller.api.dto.ServicioDto;
import com.autolavado.autolavadomvc.service.CatalogoServiciosService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/servicios")
public class CatalogoServiciosController {

    private final CatalogoServiciosService catalogoServiciosService;

    public CatalogoServiciosController(CatalogoServiciosService catalogoServiciosService) {
        this.catalogoServiciosService = catalogoServiciosService;
    }

    @GetMapping
    public List<ServicioDto> listar() {
        return catalogoServiciosService.listarServicios().stream()
                .map(ApiDtoMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ServicioDto detalle(@PathVariable Integer id) {
        var servicio = catalogoServiciosService.buscarPorId(id);
        if (servicio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El servicio solicitado no existe.");
        }
        return ApiDtoMapper.toDto(servicio);
    }
}
