package com.asociacion.actividades_service.client;

import com.asociacion.actividades_service.dto.SocioDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "socios-service")
public interface SocioCliente {
    
    @GetMapping("/api/v1/socios/{id}")
    SocioDto buscarSocio(@PathVariable("id") int id);
}