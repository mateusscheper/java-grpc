package scheper.mateus.utils;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class StringUtils {

    private StringUtils() {
    }

    public static String formatarCpf(String cpf) {
        if (isBlank(cpf)) {
            return cpf;
        }

        String cpfSomenteNumeros = limparCpf(cpf);
        if (cpfSomenteNumeros.length() != 11) {
            return cpf;
        }

        return cpfSomenteNumeros.substring(0, 3)
                + "."
                + cpfSomenteNumeros.substring(3, 6)
                + "."
                + cpfSomenteNumeros.substring(6, 9)
                + "-"
                + cpfSomenteNumeros.substring(9);
    }

    public static String limparCpf(String cpf) {
        if (isBlank(cpf)) {
            return cpf;
        }

        return cpf.replaceAll("\\D", "").trim();
    }
}
