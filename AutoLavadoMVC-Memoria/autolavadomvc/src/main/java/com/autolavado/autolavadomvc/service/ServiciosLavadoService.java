package com.autolavado.autolavadomvc.service;

import com.autolavado.autolavadomvc.model.ServiciosLavado;
import com.autolavado.autolavadomvc.model.TipoLavado;
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
                    TipoLavado.BASICO),
            new ServiciosLavado(
                    "Lavado completo",
                    "Exterior + interior",
                    new BigDecimal("15.00"),
                    35,
                    "Más vendido",
                    "/imgs/completo.jpg",
                    TipoLavado.COMPLETO),
            new ServiciosLavado(
                    "Lavado premium",
                    "Detalle completo",
                    new BigDecimal("25.00"),
                    60,
                    "Premium",
                    "/imgs/premium.jpg",
                    TipoLavado.PREMIUM),
            new ServiciosLavado(
                    "Limpieza interior",
                    "Interior",
                    new BigDecimal("12.00"),
                    30,
                    "Interior",
                    "/imgs/interior.jpg",
                    TipoLavado.INTERIOR),
            new ServiciosLavado(
                    "Pulido de faros",
                    "Mantenimiento",
                    new BigDecimal("18.00"),
                    45,
                    "Seguridad",
                    "/imgs/pulido.webp",
                    TipoLavado.PULIDO_FAROS),
            new ServiciosLavado(
                    "Aspirado profesional",
                    "Interior",
                    new BigDecimal("6.00"),
                    15,
                    "Rápido",
                    "/imgs/aspirado.jpg",
                    TipoLavado.ASPIRADO)
    );

    public List<ServiciosLavado> listarServicios() {
        return SERVICIOS;
    }
}