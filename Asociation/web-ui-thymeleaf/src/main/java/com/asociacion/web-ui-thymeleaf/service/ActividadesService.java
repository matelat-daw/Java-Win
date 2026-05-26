package com.asociacion.web_ui_thymeleaf.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ActividadesService {
	private final RestClient restClient;

	public ActividadesService(@Value("${gateway.url}") String gatewayUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(gatewayUrl)
				.build();
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> obtenerTodasLasActividades() {
		return restClient.get()
				.uri("/api/v1/actividades")
				.retrieve()
				.body(List.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> crearActividad(Map<String, Object> actividad) {
		return restClient.post()
				.uri("/api/v1/actividades")
				.body(actividad)
				.retrieve()
				.body(Map.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> obtenerActividadPorId(Integer id) {
		return restClient.get()
				.uri("/api/v1/actividades/{id}", id)
				.retrieve()
				.body(Map.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> inscribirSocioEnActividad(Integer actividadId, Map<String, Object> inscripcion) {
		return restClient.post()
				.uri("/api/v1/actividades/{id}/inscripciones", actividadId)
				.body(inscripcion)
				.retrieve()
				.body(Map.class);
	}
}