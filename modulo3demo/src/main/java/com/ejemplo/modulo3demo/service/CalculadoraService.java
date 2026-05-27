package com.ejemplo.modulo3demo.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadoraService {
    public double calcularTotalConIva(double precioSinIva) {
        return precioSinIva * 1.21;
    }
}