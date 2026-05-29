package com.miapp.controller;

import com.miapp.dto.TiendaInfoDTO;
import com.miapp.model.Contacto;
import com.miapp.model.Producto;
import com.miapp.service.ContactoService;
import com.miapp.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api", ""})
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ContactoService contactoService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null) {
                    setValue(null);
                    return;
                }

                String value = text.trim().replace(" ", "");
                if (value.isEmpty()) {
                    setValue(null);
                    return;
                }

                if (value.contains(",") && value.contains(".")) {
                    value = value.replace(".", "").replace(",", ".");
                } else if (value.contains(",")) {
                    value = value.replace(",", ".");
                }

                setValue(new BigDecimal(value));
            }
        });
    }

    @GetMapping("/store/info")
    public TiendaInfoDTO obtenerInformacion() {
        return new TiendaInfoDTO(
                "Tienda Tech",
                "Calle Principal 123, Madrid, España",
                "+34 123 456 789",
                "info@tiendatech.com",
                "Lunes a Viernes: 9:00 - 18:00 | Sábados: 10:00 - 14:00"
        );
    }

    @GetMapping("/categories")
    public List<String> categorias() {
        return productoService.obtenerCategorias();
    }

    @GetMapping("/products")
    public List<Producto> productos(@RequestParam(value = "category", required = false) String category) {
        if (category != null && !category.isBlank()) {
            return productoService.buscarPorCategoria(category.trim());
        }

        return productoService.obtenerCatalogo();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Producto> productoPorId(@PathVariable int id) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public Map<String, Object> resumen() {
        List<Producto> productos = productoService.obtenerCatalogo();
        List<String> categorias = productoService.obtenerCategorias();

        double precioPromedio = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .average()
                .orElse(0.0);
        double precioMayor = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .max()
                .orElse(0.0);
        double precioMenor = productos.stream()
                .mapToDouble(p -> p.getPrecio().doubleValue())
                .min()
                .orElse(0.0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalProductos", productos.size());
        data.put("totalCategorias", categorias.size());
        data.put("precioPromedio", precioPromedio);
        data.put("precioMayor", precioMayor);
        data.put("precioMenor", precioMenor);
        return data;
    }

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearProducto(
            @Valid @ModelAttribute Producto producto,
            BindingResult bindingResult,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(errores(bindingResult));
        }

        if (imagenFile == null || imagenFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Debes seleccionar una imagen para el producto."));
        }

        try {
            Producto guardado = productoService.guardarProductoConImagen(producto, imagenFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo guardar el producto."));
        }
    }

    @PostMapping(value = "/products/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarProducto(
            @PathVariable int id,
            @Valid @ModelAttribute Producto producto,
            BindingResult bindingResult,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(errores(bindingResult));
        }

        producto.setId(id);

        try {
            Optional<Producto> existente = productoService.obtenerProductoPorId(id);
            if (existente.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Producto guardado = productoService.guardarProductoConImagen(producto, imagenFile);
            return ResponseEntity.ok(guardado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "No se pudo actualizar el producto."));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable int id) {
        Optional<Producto> existente = productoService.obtenerProductoPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productoService.eliminarProducto(id);
        return ResponseEntity.ok(Map.of(
                "message", "Producto eliminado correctamente.",
                "id", id
        ));
    }

    @PostMapping("/contact")
    public ResponseEntity<?> enviarContacto(@Valid @RequestBody Contacto contacto) {
        Contacto guardado = contactoService.guardarContacto(contacto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Mensaje enviado con éxito.",
                "id", guardado.getId()
        ));
    }

    private Map<String, String> errores(BindingResult bindingResult) {
        Map<String, String> errores = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(error -> errores.put(error.getField(), error.getDefaultMessage()));
        return errores;
    }
}
