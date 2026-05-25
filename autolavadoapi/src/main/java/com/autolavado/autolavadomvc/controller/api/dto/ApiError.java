package com.autolavado.autolavadomvc.controller.api.dto;

import java.util.Map;

public record ApiError(
        String message,
        Map<String, String> fieldErrors
) {
}
