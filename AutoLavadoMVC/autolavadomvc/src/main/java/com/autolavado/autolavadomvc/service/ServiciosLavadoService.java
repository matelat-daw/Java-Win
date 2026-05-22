package com.autolavado.autolavadomvc.service;

import com.autolavado.autolavadomvc.model.ServiciosLavado;
import com.autolavado.autolavadomvc.model.TipoServicio;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServiciosLavadoService {

    private static final List<ServiciosLavado> SERVICIOS = List.of(
            new ServiciosLavado(
                    "Lavado básico",
                    "Exterior",
                    new BigDecimal("8.00"),
                    20,
                    "Económico",
                    "/imgs/basico.jpg",
                    TipoServicio.BÁSICO),
            new ServiciosLavado(
                    "Lavado completo",
                    "Exterior + interior",
                    new BigDecimal("15.00"),
                    35,
                    "Más vendido",
                    "/imgs/completo.jpg",
                    TipoServicio.COMPLETO),
            new ServiciosLavado(
                    "Lavado premium",
                    "Detalle completo",
                    new BigDecimal("25.00"),
                    60,
                    "Premium",
                    "/imgs/premium.jpg",
                    TipoServicio.PREMIUM),
            new ServiciosLavado(
                    "Limpieza interior",
                    "Interior",
                    new BigDecimal("12.00"),
                    30,
                    "Interior",
                    "/imgs/interior.jpg",
                    TipoServicio.INTERIOR),
            new ServiciosLavado(
                    "Pulido de faros",
                    "Mantenimiento",
                    new BigDecimal("18.00"),
                    45,
                    "Seguridad",
                    "/imgs/pulido.webp",
                    TipoServicio.PULIDO_FAROS),
            new ServiciosLavado(
                    "Aspirado profesional",
                    "Interior",
                    new BigDecimal("6.00"),
                    15,
                    "Rápido",
                    "/imgs/aspirado.jpg",
                    TipoServicio.ASPIRADO)
    );

    public List<ServiciosLavado> listarServicios() {
        return SERVICIOS;
    }
}