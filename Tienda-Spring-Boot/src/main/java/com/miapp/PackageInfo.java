package com.miapp;

/**
 * ESTRUCTURA DE LA APLICACIÓN TIENDA TECH
 * =======================================
 * 
 * com.miapp/
 * ├── TiendaApplication.java          ← Clase principal de Spring Boot
 * │
 * ├── config/                         ← Configuraciones de la aplicación
 * │   └── DatabaseConfig.java
 * │
 * ├── controller/                     ← Controladores REST/MVC
 * │   ├── CatalogoController.java
 * │   ├── ContactoController.java
 * │   ├── ResumenController.java
 * │   └── TiendaController.java
 * │
 * ├── model/                          ← Entidades JPA
 * │   ├── Contacto.java
 * │   └── Producto.java
 * │
 * ├── repository/                     ← Spring Data JPA Repositories
 * │   ├── ContactoRepository.java
 * │   └── ProductoRepository.java
 * │
 * ├── service/                        ← Lógica de negocio
 * │   ├── ContactoService.java
 * │   └── ProductoService.java
 * │
 * ├── dto/                            ← Data Transfer Objects
 * │   ├── ResumenDTO.java
 * │   └── TiendaInfoDTO.java
 * │
 * ├── util/                           ← Clases utilitarias
 * │   └── ImagesUtil.java
 * │
 * └── (DEPRECATED - Antiguas)
 *     ├── CatalogoServlet.java        [Use CatalogoController]
 *     ├── ContactoBean.java           [Use TiendaInfoDTO]
 *     ├── ContactoServlet.java        [Use ContactoController]
 *     ├── DatabaseConnection.java     [Use DatabaseConfig]
 *     ├── DatosTienda.java            [Use ProductoService]
 *     ├── ImagesUtil.java             [Use util/ImagesUtil]
 *     ├── ProductoDAO.java            [Use ProductoRepository]
 *     ├── ResumenBean.java            [Use ResumenDTO]
 *     ├── TiendaServlet.java          [Use TiendaController]
 *     └── *Servlet.java               [Use Controllers]
 * 
 * GUÍA DE MIGRATION
 * =================
 * 
 * Si necesitas importar clases, usa:
 * 
 *   ✅ Correcto (Nueva estructura):
 *      import com.miapp.config.DatabaseConfig;
 *      import com.miapp.util.ImagesUtil;
 *      import com.miapp.dto.TiendaInfoDTO;
 *      import com.miapp.dto.ResumenDTO;
 *      import com.miapp.model.Producto;
 *      import com.miapp.repository.ProductoRepository;
 *      import com.miapp.service.ProductoService;
 * 
 *   ❌ Incorrecto (Estructura antigua):
 *      import com.miapp.DatabaseConnection;  (No usar)
 *      import com.miapp.ProductoDAO;         (No usar)
 *      import com.miapp.DatosTienda;         (No usar)
 *      import com.miapp.ImagesUtil;          (No usar - está en util/)
 * 
 * VER TAMBIÉN:
 *   - REFACTORING.md para detalles completos de cambios
 *   - application.properties para configuración de base de datos
 */
public final class PackageInfo {
    private PackageInfo() {
        // No instantiate
    }
}
