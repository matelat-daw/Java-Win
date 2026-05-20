package com.miapp.service;

import com.miapp.model.Producto;
import com.miapp.model.Categoria;
import com.miapp.repository.ProductoRepository;
import com.miapp.repository.CategoriaRepository;
import com.miapp.util.ImagesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    public List<Producto> obtenerCatalogo() {
        return productoRepository.findAll();
    }

    public Page<Producto> obtenerCatalogoPaginado(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }
    
    public Optional<Producto> obtenerProductoPorId(int id) {
        return productoRepository.findById(id);
    }
    
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }
    
    public void eliminarProducto(int id) {
        // Obtener el producto para eliminar su imagen
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isPresent() && producto.get().getImagen() != null) {
            // Eliminar la imagen del servidor
            ImagesUtil.eliminarImagen(producto.get().getImagen());
        }
        // Eliminar el producto de la base de datos
        productoRepository.deleteById(id);
    }
    
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    public Page<Producto> buscarPorCategoriaPaginado(String categoria, Pageable pageable) {
        return productoRepository.findByCategoria(categoria, pageable);
    }
    
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
    
    public List<String> obtenerCategorias() {
        return categoriaRepository.findAll().stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList());
    }
    
    /**
     * Guarda un producto con su imagen
     *
     * @param producto El producto a guardar
     * @param imagenFile El archivo de imagen (puede ser null para actualización sin cambio de imagen)
     * @return El producto guardado
     * @throws Exception Si ocurre error al guardar la imagen
     */
    public Producto guardarProductoConImagen(Producto producto, MultipartFile imagenFile) throws Exception {
        // Obtener imagen anterior si es actualización
        String imagenAnterior = null;
        if (producto.getId() > 0) {
            Optional<Producto> productoExistente = productoRepository.findById(producto.getId());
            if (productoExistente.isPresent()) {
                imagenAnterior = productoExistente.get().getImagen();
            }
        }
        
        // Si se proporciona un archivo de imagen nuevo
        if (imagenFile != null && !imagenFile.isEmpty()) {
            // Eliminar imagen anterior si existe
            if (imagenAnterior != null) {
                ImagesUtil.eliminarImagen(imagenAnterior);
            }
            
            // Guardar la nueva imagen y asignar su nombre al producto
            String nombreImagen = ImagesUtil.guardarImagen(imagenFile);
            producto.setImagen(nombreImagen);
        } else if (producto.getId() > 0 && imagenAnterior != null) {
            // Si es actualización y no se proporciona nueva imagen, mantener la anterior
            producto.setImagen(imagenAnterior);
        }
        
        // Guardar el producto en la base de datos
        return productoRepository.save(producto);
    }
}