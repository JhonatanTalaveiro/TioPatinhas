import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class Entrada {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private Entrada() {
    }

    public static String lerLinha(String prompt) {
        System.out.print(prompt);

        try {
            String linha = SCANNER.nextLine();
            return linha == null ? "" : linha.trim();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Entrada encerrada. Finalizando o programa.");
            System.exit(0);
            return "";
        }
    }

    public static int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            String texto = lerLinha(rotulo + ": ");

            try {
                int valor = Integer.parseInt(texto);

                if (valor < minimo || valor > maximo) {
                    System.out.println("Opção inválida: digite um número entre "
                            + minimo + " e " + maximo + ".");
                    continue;
                }

                return valor;

            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: digite apenas números.");
            }
        }
    }

    public static String lerTextoObrigatorio(String rotulo, int tamanhoMaximo) {
        while (true) {
            String texto = lerLinha(rotulo + ": ");

            if (!Validador.textoPreenchido(texto)) {
                System.out.println("Campo obrigatório: digite algum valor.");
            } else if (texto.length() > tamanhoMaximo) {
                System.out.println("Texto muito longo: no máximo " + tamanhoMaximo + " caracteres.");
            } else {
                return texto;
            }
        }
    }

    public static String lerTextoOpcional(String rotulo, String valorAtual, int tamanhoMaximo) {
        while (true) {
            String texto = lerLinha(rotulo + " [" + valorAtual + "]: ");

            if (texto.isEmpty()) {
                return valorAtual;
            }
            if (texto.length() > tamanhoMaximo) {
                System.out.println("Texto muito longo: no máximo " + tamanhoMaximo + " caracteres.");
                continue;
            }

            return texto;
        }
    }

    public static String lerEmail(String rotulo) {
        while (true) {
            String email = lerLinha(rotulo + ": ");

            if (Validador.emailValido(email)) {
                return email.toLowerCase();
            }

            System.out.println("E-mail inválido. Use o formato nome@dominio.com.");
        }
    }

    public static String lerEmail(String rotulo, String valorAtual) {
        while (true) {
            String email = lerLinha(rotulo + " [" + valorAtual + "]: ");

            if (email.isEmpty()) {
                return valorAtual;
            }
            if (Validador.emailValido(email)) {
                return email.toLowerCase();
            }

            System.out.println("E-mail inválido. Use o formato nome@dominio.com.");
        }
    }

    public static String lerSimbolo(String rotulo) {
        while (true) {
            String simbolo = lerLinha(rotulo + ": ").toUpperCase();

            if (Validador.simboloValido(simbolo)) {
                return simbolo;
            }

            System.out.println("Símbolo inválido: use de 1 a " + Validador.TAMANHO_MAXIMO_SIMBOLO
                    + " letras ou números, sem espaços (ex.: BRL, BTC).");
        }
    }

    public static String lerSimbolo(String rotulo, String valorAtual) {
        while (true) {
            String simbolo = lerLinha(rotulo + " [" + valorAtual + "]: ").toUpperCase();

            if (simbolo.isEmpty()) {
                return valorAtual;
            }
            if (Validador.simboloValido(simbolo)) {
                return simbolo;
            }

            System.out.println("Símbolo inválido: use de 1 a " + Validador.TAMANHO_MAXIMO_SIMBOLO
                    + " letras ou números, sem espaços (ex.: BRL, BTC).");
        }
    }

    public static String lerSenha(String rotulo) {
        while (true) {
            String senha = lerLinha(rotulo + ": ");

            if (Validador.senhaValida(senha)) {
                return senha;
            }

            System.out.println("Senha inválida: use pelo menos "
                    + Validador.TAMANHO_MINIMO_SENHA + " caracteres.");
        }
    }

    public static BigDecimal lerDecimal(String rotulo, boolean permiteZero) {
        while (true) {
            String texto = lerLinha(rotulo + ": ").replace(",", ".");

            BigDecimal valor = converter(texto, permiteZero);
            if (valor != null) {
                return valor;
            }
        }
    }

    public static BigDecimal lerDecimal(String rotulo, boolean permiteZero, BigDecimal valorAtual) {
        while (true) {
            String texto = lerLinha(rotulo + " [" + valorAtual.toPlainString() + "]: ").replace(",", ".");

            if (texto.isEmpty()) {
                return valorAtual;
            }

            BigDecimal valor = converter(texto, permiteZero);
            if (valor != null) {
                return valor;
            }
        }
    }

    public static LocalDateTime lerDataHora(String rotulo, LocalDateTime valorPadrao) {
        LocalDateTime padrao = valorPadrao.withSecond(0).withNano(0);

        while (true) {
            String texto = lerLinha(rotulo + " (dd/mm/aaaa hh:mm) ["
                    + padrao.format(FORMATO_DATA_HORA) + "]: ");

            if (texto.isEmpty()) {
                return padrao;
            }

            try {
                LocalDateTime valor = LocalDateTime.parse(texto, FORMATO_DATA_HORA);

                if (!Validador.dataNaoFutura(valor)) {
                    System.out.println("Data inválida: não é possível registrar uma data no futuro.");
                    continue;
                }

                return valor;

            } catch (DateTimeParseException e) {
                System.out.println("Data/hora inválida. Exemplo correto: 09/11/2026 14:30");
            }
        }
    }

    public static boolean confirmar(String pergunta) {
        while (true) {
            String resposta = lerLinha(pergunta + " (s/n): ").toLowerCase();

            if (resposta.equals("s") || resposta.equals("sim")) {
                return true;
            }
            if (resposta.equals("n") || resposta.equals("nao") || resposta.equals("não")) {
                return false;
            }

            System.out.println("Responda com s (sim) ou n (não).");
        }
    }

    public static void pausar() {
        lerLinha("\nPressione ENTER para continuar...");
    }

    private static BigDecimal converter(String texto, boolean permiteZero) {
        try {
            BigDecimal valor = new BigDecimal(texto).stripTrailingZeros();
            if (valor.scale() < 0) {
                valor = valor.setScale(0);
            }

            if (valor.signum() < 0 || (!permiteZero && valor.signum() == 0)) {
                System.out.println("Valor inválido: digite um número "
                        + (permiteZero ? "maior ou igual a zero." : "maior que zero."));
                return null;
            }
            if (valor.scale() > Validador.CASAS_DECIMAIS) {
                System.out.println("Valor inválido: no máximo "
                        + Validador.CASAS_DECIMAIS + " casas decimais.");
                return null;
            }
            if (!Validador.cabeNaColunaNumerica(valor)) {
                System.out.println("Valor alto demais: no máximo "
                        + Validador.DIGITOS_INTEIROS + " dígitos antes da vírgula.");
                return null;
            }

            return valor;

        } catch (NumberFormatException e) {
            System.out.println("Número inválido: digite algo como 1234.56");
            return null;
        }
    }
}
