package com.asociacion.frontend.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

// 👇 ESTOS SON LOS IMPORTS QUE TE FALTAN 👇
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FrontendService {
    private final RestClient restClient;

    // Inyectamos la URL de la Gateway configurada en application.properties
    public FrontendService(@Value("${gateway.url}") String gatewayUrl) {
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
}