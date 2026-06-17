package com.miapp.controller;

import com.miapp.model.Producto;
import com.miapp.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/store")
public class CatalogoController {
    
    private final ProductoService productoService;
    
    public CatalogoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BigDecimal.class, new java.beans.PropertyEditorSupport() {
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
    
    @GetMapping("/catalog")
    public String catalogo(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        int pageSize = 8;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), pageSize, Sort.by(Sort.Direction.ASC, catalogo(pageSize, model).equalsIgnoreCase("nombre") ? "nombre" : "id"));
        Page<Producto> productosPage = productoService.obtenerCatalogoPaginado(pageable);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "catalogo";
    }
    
    @GetMapping("/catalog/category/{category}")
    public String catalogoPorCategoria(
            @PathVariable("category") String category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        int pageSize = 8;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), pageSize, Sort.by(Sort.Direction.ASC, category.equalsIgnoreCase("nombre") ? "nombre" : "id"));
        Page<Producto> productosPage = productoService.buscarPorCategoriaPaginado(category, pageable);

        model.addAttribute("productosPage", productosPage);
        model.addAttribute("categoriaSeleccionada", category);
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "catalogo";
    }
    
    @GetMapping("/detail/{id}")
    public String detalleProducto(@PathVariable int id, Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "detalle-producto";
        }
        return "redirect:/store/catalog";
    }
    
    @GetMapping("/new")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", productoService.obtenerCategorias());
        return "nuevo-producto";
    }
    
    @PostMapping("/save")
    public String guardarProducto(
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Revisa los campos marcados e inténtalo de nuevo.");
                model.addAttribute("categorias", productoService.obtenerCategorias());
                return "nuevo-producto";
            }

            // Validar que se proporcionó una imagen
            if (imagenFile == null || imagenFile.isEmpty()) {
                model.addAttribute("error", "Debes seleccionar una imagen para el producto");
                model.addAttribute("categorias", productoService.obtenerCategorias());
                return "nuevo-producto";
            }

            // Guardar con imagen
            Producto productoGuardado = productoService.guardarProductoConImagen(producto, imagenFile);
            
            // Usar flash attributes para pasar el mensaje de éxito y datos del producto para el modal
            redirectAttributes.addFlashAttribute("tipo", "exito");
            redirectAttributes.addFlashAttribute("mensaje", "¡Producto creado exitosamente!");
            redirectAttributes.addFlashAttribute("nombreProducto", productoGuardado.getNombre());
            redirectAttributes.addFlashAttribute("precioProducto", productoGuardado.getPrecio());
            
            return "redirect:/store/catalog";
        } catch (IllegalArgumentException e) {
            // Error de validación (extensión, tamaño, etc)
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "nuevo-producto";
        } catch (Exception e) {
            // Otros errores
            model.addAttribute("error", "Error inesperado al guardar: " + e.getMessage());
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "nuevo-producto";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String mostrarFormularioEditar(@PathVariable int id, Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "editar-producto";
        }
        return "redirect:/store/catalog";
    }
    
    @PostMapping("/update")
    public String actualizarProducto(
            @Valid @ModelAttribute("producto") Producto producto,
            BindingResult bindingResult,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            Model model) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Revisa los campos marcados e inténtalo de nuevo.");
                model.addAttribute("categorias", productoService.obtenerCategorias());
                return "editar-producto";
            }
            
            // Guardar con imagen (imagen puede ser null/vacío)
            productoService.guardarProductoConImagen(producto, imagenFile);
            
            return "redirect:/store/detail/" + producto.getId();
        } catch (IllegalArgumentException e) {
            // Error de validación (extensión, tamaño, etc)
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("categorias", productoService.obtenerCategorias());
            return "editar-producto";
        } catch (Exception e) {
            // Otros errores
            return "redirect:/store/edit/" + producto.getId();
        }
    }
    
    @GetMapping("/delete/{id}")
    public String eliminarProducto(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Producto> producto = productoService.obtenerProductoPorId(id);
            if (producto.isPresent()) {
                String nombreProducto = producto.get().getNombre();
                productoService.eliminarProducto(id);
                
                // Usar sistema de modales para la eliminación
                redirectAttributes.addFlashAttribute("tipo", "eliminado");
                redirectAttributes.addFlashAttribute("mensaje", "El producto ha sido eliminado permanentemente.");
                redirectAttributes.addFlashAttribute("nombreProducto", nombreProducto);
                
                return "redirect:/store/catalog";
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/store/catalog";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
            return "redirect:/store/catalog";
        }
    }
}