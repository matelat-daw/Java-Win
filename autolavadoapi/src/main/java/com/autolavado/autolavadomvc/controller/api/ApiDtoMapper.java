package com.autolavado.autolavadomvc.controller.api;

import com.autolavado.autolavadomvc.controller.api.dto.ReservaDto;
import com.autolavado.autolavadomvc.controller.api.dto.ServicioDto;
import com.autolavado.autolavadomvc.model.Facturacion;
import com.autolavado.autolavadomvc.model.ReservaServicio;
import com.autolavado.autolavadomvc.model.Servicio;

public final class ApiDtoMapper {

    private ApiDtoMapper() {
    }

    public static ServicioDto toDto(Servicio servicio) {
        if (servicio == null) {
            return null;
        }
        return new ServicioDto(
                servicio.getId(),
                servicio.getServicio(),
                servicio.getPrecio(),
                servicio.getDescripcion(),
                servicio.getImagen()
        );
    }

    public static ReservaDto toDto(ReservaServicio reserva) {
        if (reserva == null) {
            return null;
        }

        Facturacion principal = reserva.getFacturacionPrincipal();
        Servicio servicio = principal != null ? principal.getServicio() : null;

        return new ReservaDto(
                reserva.getId(),
                reserva.getNombreCliente(),
                reserva.getTelefono(),
                reserva.getMatricula(),
                reserva.getEstado() != null ? reserva.getEstado().getEstado() : null,
                reserva.getFecha(),
                reserva.getHora(),
                reserva.getObservaciones(),
                toDto(servicio),
                reserva.getTotal()
        );
    }
}
