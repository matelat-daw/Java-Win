package com.miapp;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("consultaBean")
@SessionScoped

public class ConsultaBean implements Serializable {
    
    @Inject
    private TiendaBean tiendaBean;

    private String nombreCliente;
    private String email;
    private String producto;
    private Integer cantidad;
    private boolean urgente;

    public ConsultaBean() {
        // Constructor vacío
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Producto getProductoSeleccionado() {
        if (tiendaBean == null || producto == null || producto.isBlank()) {
            return null;
        }

        List<Producto> productos = tiendaBean.getProductos();
        for (Producto item : productos) {
            if (producto.equals(item.getNombre())) {
                return item;
            }
        }

        return null;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public boolean isUrgente() {
        return urgente;
    }

    public void setUrgente(boolean urgente) {
        this.urgente = urgente;
    }

    public int getStockDisponible() {
        Producto seleccionado = getProductoSeleccionado();
        if (seleccionado == null) {
            return 0;
        }

        return seleccionado.getStock();
    }

    public boolean isHayStockDisponible() {
        Producto seleccionado = getProductoSeleccionado();
        return seleccionado != null && seleccionado.isDisponible() && seleccionado.getStock() > 0;
    }

    public boolean isMostrarCantidadSelector() {
        Producto seleccionado = getProductoSeleccionado();
        return seleccionado != null && seleccionado.isDisponible() && seleccionado.getStock() > 0;
    }

    public boolean isMostrarMensajeNoDisponible() {
        Producto seleccionado = getProductoSeleccionado();
        return seleccionado != null && (!seleccionado.isDisponible() || seleccionado.getStock() <= 0);
    }

    public boolean isProductoSinDisponibilidad() {
        Producto seleccionado = getProductoSeleccionado();
        return seleccionado == null || !seleccionado.isDisponible() || seleccionado.getStock() <= 0;
    }

    public List<Integer> getCantidadesDisponibles() {
        int limite = Math.min(5, getStockDisponible());
        List<Integer> cantidades = new ArrayList<>();
        for (int i = 1; i <= limite; i++) {
            cantidades.add(i);
        }
        return cantidades;
    }

    public String enviar() {
        if (isProductoSinDisponibilidad()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Este producto no está disponible o no tiene stock. Consulta por otro producto.",
                            null));
            return null;
        }

        if (cantidad != null && cantidad > getStockDisponible()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "La cantidad solicitada supera el stock disponible.",
                            null));
            return null;
        }

        System.out.println("Consulta recibida de: " + nombreCliente);
        System.out.println("Email: " + email);
        System.out.println("Producto: " + producto);
        System.out.println("Cantidad: " + cantidad);
        System.out.println("¿Es urgente?: " + urgente);
        
        return "confirmacion-consulta?faces-redirect=false"; // Redirige a una página de confirmación
    }
}