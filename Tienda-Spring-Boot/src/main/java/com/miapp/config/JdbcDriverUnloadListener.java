package com.miapp.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener para desregistrar correctamente los drivers JDBC al detener la aplicación.
 * Previene advertencias de memory leak cuando Tomcat detiene la aplicación.
 */
@WebListener
public class JdbcDriverUnloadListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDriverUnloadListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Desregistrando JDBC drivers...");
        
        // Obtener todos los drivers registrados
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                logger.info("Driver desregistrado exitosamente: {}", driver.getClass().getName());
            } catch (Exception e) {
                logger.error("Error al desregistrar driver: {}", driver.getClass().getName(), e);
            }
        }
        
        logger.info("Todos los JDBC drivers han sido desregistrados.");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Aplicación iniciada - JDBC Driver Unload Listener listo");
    }
}
