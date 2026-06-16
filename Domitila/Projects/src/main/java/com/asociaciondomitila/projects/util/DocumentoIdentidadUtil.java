package com.asociaciondomitila.projects.util;

public class DocumentoIdentidadUtil {

    private static final String LETRAS_VALIDAS = "TRWAGMYFPDXBNJZSQVHLCKE";

    public static boolean validarDniNie(String documento) {
        if (documento == null) {
            return false;
        }

        String doc = documento.trim().toUpperCase();

        if (!doc.matches("^[XYZ\\d]\\d{7}[A-Z]$")) {
            return false;
        }

        try {
            String numeroFormateado = doc;
            char primeraLetra = doc.charAt(0);

            if (primeraLetra == 'X') {
                numeroFormateado = "0" + doc.substring(1);
            } else if (primeraLetra == 'Y') {
                numeroFormateado = "1" + doc.substring(1);
            } else if (primeraLetra == 'Z') {
                numeroFormateado = "2" + doc.substring(1);
            }

            int numero = Integer.parseInt(numeroFormateado.substring(0, 8));
            char letraControl = doc.charAt(8);

            char letraCalculada = LETRAS_VALIDAS.charAt(numero % 23);

            return letraControl == letraCalculada;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
