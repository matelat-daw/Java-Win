package com.asociaciondomitila.projects.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoIdentidadUtilTest {

    @Test
    void validarDniNie_returnsTrue_forValidDni() {
        assertTrue(DocumentoIdentidadUtil.validarDniNie("12345678Z"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("00000000T"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("1R"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("10X"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("00000001R"));
    }

    @Test
    void validarDniNie_returnsTrue_forValidNie() {
        assertTrue(DocumentoIdentidadUtil.validarDniNie("X1234567L"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("Y1234567X"));
        assertTrue(DocumentoIdentidadUtil.validarDniNie("Z1234567R"));
    }

    @Test
    void validarDniNie_returnsFalse_forInvalidValues() {
        assertFalse(DocumentoIdentidadUtil.validarDniNie(null));
        assertFalse(DocumentoIdentidadUtil.validarDniNie(""));
        assertFalse(DocumentoIdentidadUtil.validarDniNie("12345678A"));
        assertFalse(DocumentoIdentidadUtil.validarDniNie("1A"));
        assertFalse(DocumentoIdentidadUtil.validarDniNie("X1234567A"));
        assertFalse(DocumentoIdentidadUtil.validarDniNie("1234"));
        assertFalse(DocumentoIdentidadUtil.validarDniNie("ABCDEFGHJ"));
    }
}

