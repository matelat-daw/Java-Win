package com.asociaciondomitila.projects.validation;

import com.asociaciondomitila.projects.util.DocumentoIdentidadUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDniNieValidator implements ConstraintValidator<ValidDniNie, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return DocumentoIdentidadUtil.validarDniNie(value);
    }
}
