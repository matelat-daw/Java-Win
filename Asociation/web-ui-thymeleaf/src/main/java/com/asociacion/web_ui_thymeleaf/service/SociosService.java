package com.asociacion.web_ui_thymeleaf.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SociosService {
    private final RestClient restClient;

    // Inyectamos la URL de la Gateway configurada en application.properties
    public SociosService(@Value("${gateway.url}") String gatewayUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }

    // Método para obtener la lista de socios desde el Gateway
    @SuppressWarnings("unchecked") // Evita la advertencia de tipos genéricos al castear el List
    public List<Map<String, Object>> obtenerTodosLosSocios() {
        return restClient.get()
                .uri("/api/v1/socios")
                .retrieve()
                .body(List.class); 
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearSocio(Map<String, Object> socio) {
        return restClient.post()
                .uri("/api/v1/socios")
                .body(socio)
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerSocioPorId(Integer id) {
        return restClient.get()
                .uri("/api/v1/socios/{id}", id)
                .retrieve()
                .body(Map.class);
    }
}