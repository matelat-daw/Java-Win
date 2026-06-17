package com.asociaciondomitila.projects.util;

public class DocumentoIdentidadUtil {

    private static final String LETRAS_VALIDAS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final String DNI_REGEX = "^\\d{1,8}[A-Z]$";
    private static final String NIE_REGEX = "^[XYZ]\\d{7}[A-Z]$";

    public static boolean validarDniNie(String documento) {
        if (documento == null) {
            return false;
        }

        String doc = documento.trim().toUpperCase().replaceAll("[-\\s]", "");

        if (doc.isEmpty()) {
            return false;
        }

        try {
            String numeroDocumento;
            char letraControl = doc.charAt(doc.length() - 1);

            if (doc.matches(DNI_REGEX)) {
                numeroDocumento = doc.substring(0, doc.length() - 1);
            } else if (doc.matches(NIE_REGEX)) {
                char primeraLetra = doc.charAt(0);
                char prefijoNumerico = switch (primeraLetra) {
                    case 'X' -> '0';
                    case 'Y' -> '1';
                    case 'Z' -> '2';
                    default -> ' ';
                };
                numeroDocumento = prefijoNumerico + doc.substring(1, doc.length() - 1);
            } else {
                return false;
            }

            int numero = Integer.parseInt(numeroDocumento);

            char letraCalculada = LETRAS_VALIDAS.charAt(numero % 23);

            return letraControl == letraCalculada;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
