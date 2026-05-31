package com.miapp.service;

import com.miapp.dto.CategoriaOptionDto;
import com.miapp.dto.ProductoRequestDto;
import com.miapp.dto.ProductoResponseDto;
import com.miapp.dto.ProductoUpdateDto;
import com.miapp.model.Categoria;
import com.miapp.model.Producto;
import com.miapp.repository.CategoriaRepository;
import com.miapp.repository.ProductoRepository;
import com.miapp.util.ImagesUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDto> obtenerCatalogo() {
        List<Producto> productos = productoRepository.findAll();
        Map<Long, String> categoriasPorId = construirMapaCategorias();
        return productos.stream()
                .map(producto -> toResponseDto(producto, categoriasPorId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponseDto> obtenerCatalogo(Pageable pageable) {
        Map<Long, String> categoriasPorId = construirMapaCategorias();
        return productoRepository.findAll(pageable).map(producto -> toResponseDto(producto, categoriasPorId));
    }

    @Transactional(readOnly = true)
    public Page<ProductoResponseDto> buscarProductos(String nombre, Long categoria, Pageable pageable) {
        String nombreNormalizado = nombre == null ? null : nombre.trim();
        boolean tieneNombre = nombreNormalizado != null && !nombreNormalizado.isBlank();
        boolean tieneCategoria = categoria != null;

        Page<Producto> page;
        if (tieneNombre && tieneCategoria) {
            page = productoRepository.findByCategoriaAndNombreContainingIgnoreCase(categoria, nombreNormalizado, pageable);
        } else if (tieneNombre) {
            page = productoRepository.findByNombreContainingIgnoreCase(nombreNormalizado, pageable);
        } else if (tieneCategoria) {
            page = productoRepository.findByCategoria(categoria, pageable);
        } else {
            page = productoRepository.findAll(pageable);
        }

        Map<Long, String> categoriasPorId = construirMapaCategorias();
        return page.map(producto -> toResponseDto(producto, categoriasPorId));
    }

    @Transactional(readOnly = true)
    public Optional<ProductoResponseDto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).map(this::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProductoEntidadPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public ProductoResponseDto crearProducto(ProductoRequestDto productoDto) {
        Producto producto = new Producto();
        producto.setNombre(productoDto.getNombre());
        producto.setPrecio(productoDto.getPrecio());
        producto.setStock(productoDto.getStock() != null ? productoDto.getStock() : 0);
        producto.setCategoria(productoDto.getCategoria());
        producto.setDescripcion(productoDto.getDescripcion());
        producto.setImagen(productoDto.getImagen());
        producto.setActivo(productoDto.getActivo() != null ? productoDto.getActivo() : Boolean.TRUE);

        Producto saved = productoRepository.save(producto);
        return toResponseDto(saved);
    }

    @Transactional
    public Optional<ProductoResponseDto> actualizarProducto(Long id, ProductoUpdateDto productoDto) {
        return productoRepository.findById(id).map(target -> {
            if (productoDto.getNombre() != null) {
                String nombre = productoDto.getNombre().trim();
                if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre no puede estar vacío");
                target.setNombre(nombre);
            }
            if (productoDto.getStock() != null) target.setStock(productoDto.getStock());
            if (productoDto.getPrecio() != null) target.setPrecio(productoDto.getPrecio());
            if (productoDto.getCategoria() != null) target.setCategoria(productoDto.getCategoria());
            if (productoDto.getDescripcion() != null) target.setDescripcion(productoDto.getDescripcion());
            if (productoDto.getActivo() != null) target.setActivo(productoDto.getActivo());
            if (productoDto.getImagen() != null) {
                String imagen = productoDto.getImagen().trim();
                if (!imagen.isEmpty()) {
                    target.setImagen(imagen);
                }
            }

            Producto saved = productoRepository.save(target);
            return toResponseDto(saved);
        });
    }

    @Transactional
    public boolean eliminarProducto(Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) return false;

        String imagen = producto.get().getImagen();
        if (imagen != null && !imagen.isBlank()) {
            ImagesUtil.eliminarImagen(imagen);
        }
        productoRepository.deleteById(id);
        return true;
    }

    public List<Producto> buscarPorCategoria(Long categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public ProductoResponseDto actualizarImagen(Long id, MultipartFile imagenFile) throws IOException {
        if (imagenFile == null || imagenFile.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen no puede estar vacío");
        }

        Producto producto = productoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        String imagenAnterior = producto.getImagen();

        String nombreImagen = ImagesUtil.guardarImagen(imagenFile);
        producto.setImagen(nombreImagen);

        Producto saved = productoRepository.save(producto);

        if (imagenAnterior != null && !imagenAnterior.isBlank()) {
            ImagesUtil.eliminarImagen(imagenAnterior);
        }

        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoriaOptionDto> obtenerCategorias() {
        return categoriaRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(categoria -> new CategoriaOptionDto(categoria.getId(), categoria.getNombre()))
                .toList();
    }

    private ProductoResponseDto toResponseDto(Producto producto) {
        String nombreCategoria = "";
        if (producto.getCategoria() != null) {
            nombreCategoria = categoriaRepository.findById(producto.getCategoria())
                    .map(Categoria::getNombre)
                    .orElse(String.valueOf(producto.getCategoria()));
        }
        return new ProductoResponseDto(
                producto.getId(),
                producto.getNombre(),
                producto.getStock(),
                producto.getPrecio(),
                nombreCategoria,
                producto.getDescripcion(),
                producto.getImagen(),
                producto.getActivo() != null ? producto.getActivo() : Boolean.TRUE
        );
    }

    private ProductoResponseDto toResponseDto(Producto producto, Map<Long, String> categoriasPorId) {
        String nombreCategoria = "";
        Long categoriaId = producto.getCategoria();
        if (categoriaId != null) {
            nombreCategoria = categoriasPorId.getOrDefault(categoriaId, String.valueOf(categoriaId));
        }
        return new ProductoResponseDto(
                producto.getId(),
                producto.getNombre(),
                producto.getStock(),
                producto.getPrecio(),
                nombreCategoria,
                producto.getDescripcion(),
                producto.getImagen(),
                producto.getActivo() != null ? producto.getActivo() : Boolean.TRUE
        );
    }

    private Map<Long, String> construirMapaCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        if (categorias.isEmpty()) {
            return Collections.emptyMap();
        }
        return categorias.stream().collect(java.util.stream.Collectors.toMap(Categoria::getId, Categoria::getNombre, (a, b) -> a));
    }
}
