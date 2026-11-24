import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public final class Validador {

    public static final int TAMANHO_MAXIMO_TEXTO = 255;   // VARCHAR2(255)
    public static final int TAMANHO_MAXIMO_SIMBOLO = 10;  // VARCHAR2(10)
    public static final int TAMANHO_MINIMO_SENHA = 6;
    public static final int CASAS_DECIMAIS = 8;           // NUMBER(18,8)
    public static final int DIGITOS_INTEIROS = 10;        // 18 - 8

    private static final Pattern PADRAO_EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PADRAO_SIMBOLO =
            Pattern.compile("^[A-Z0-9]{1,10}$");

    private Validador() {
    }

    public static boolean textoPreenchido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    public static boolean emailValido(String email) {
        return textoPreenchido(email)
                && email.length() <= TAMANHO_MAXIMO_TEXTO
                && PADRAO_EMAIL.matcher(email).matches();
    }

    public static boolean simboloValido(String simbolo) {
        return textoPreenchido(simbolo) && PADRAO_SIMBOLO.matcher(simbolo).matches();
    }

    public static boolean senhaValida(String senha) {
        return senha != null && senha.length() >= TAMANHO_MINIMO_SENHA;
    }

    public static boolean valorPositivo(BigDecimal valor) {
        return valor != null && valor.signum() > 0;
    }

    public static boolean valorNaoNegativo(BigDecimal valor) {
        return valor != null && valor.signum() >= 0;
    }

    public static boolean cabeNaColunaNumerica(BigDecimal valor) {
        if (valor == null) {
            return false;
        }

        BigDecimal limpo = valor.stripTrailingZeros();
        if (limpo.scale() < 0) {
            limpo = limpo.setScale(0);
        }
        if (limpo.scale() > CASAS_DECIMAIS) {
            return false;
        }

        return limpo.precision() - limpo.scale() <= DIGITOS_INTEIROS;
    }

    public static boolean dataNaoFutura(LocalDateTime data) {
        return data != null && !data.isAfter(LocalDateTime.now());
    }
}
