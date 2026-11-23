import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Regras de validação do programa, reunidas em um lugar só.
 *
 * São métodos "puros": recebem um valor e devolvem true/false, sem imprimir
 * nada e sem ler o teclado. Quem avisa o usuário é a classe Entrada ou o menu
 * que chamou. Assim a mesma regra serve para a digitação e para conferências
 * feitas depois, antes de gravar no banco.
 *
 * Os limites abaixo são os mesmos do script sql/create_tables.sql: validar aqui
 * evita que o Oracle recuse a gravação com ORA-12899 (valor grande demais para
 * a coluna) ou ORA-01438 (número maior que a precisão permitida).
 */
public final class Validador {

    public static final int TAMANHO_MAXIMO_TEXTO = 255;   // VARCHAR2(255)
    public static final int TAMANHO_MAXIMO_SIMBOLO = 10;  // VARCHAR2(10)
    public static final int TAMANHO_MINIMO_SENHA = 6;
    public static final int CASAS_DECIMAIS = 8;           // NUMBER(18,8)
    public static final int DIGITOS_INTEIROS = 10;        // 18 - 8

    // Formato simplificado de e-mail: algo@algo.algo, sem espaços.
    private static final Pattern PADRAO_EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Símbolo de moeda: letras e números, sem espaços nem acentos.
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

    /**
     * Confere se o número cabe em NUMBER(18,8): até 8 casas decimais e até 10
     * dígitos na parte inteira. precision() - scale() é justamente a quantidade
     * de dígitos antes da vírgula.
     */
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
