package com.miapp.controller;

import com.miapp.service.ProductoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/store")
public class ResumenController {
    
    private final ProductoService productoService;
    
    public ResumenController(ProductoService productoService) {
        this.productoService = productoService;
    }
    
    @GetMapping("/summary")
    public String resumen(Model model) {
        var productos = productoService.obtenerCatalogo();
        var totalProductos = productos.size();
        var precioPromedio = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .average()
                .orElse(0.0);
        var precioMayor = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .max()
                .orElse(0.0);
        var precioMenor = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .min()
                .orElse(0.0);
        
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("precioPromedio", String.format("%.2f", precioPromedio));
        model.addAttribute("precioMayor", String.format("%.2f", precioMayor));
        model.addAttribute("precioMenor", String.format("%.2f", precioMenor));
        model.addAttribute("categorias", productoService.obtenerCategorias());
        
        return "resumen";
    }
}