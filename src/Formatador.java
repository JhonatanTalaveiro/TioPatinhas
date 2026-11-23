import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Monta os textos que aparecem na tela.
 *
 * Separar a formatação dos menus evita que cada tela invente o seu próprio
 * jeito de mostrar uma carteira ou uma transação. Há dois formatos para cada
 * entidade:
 *
 *   - resumo...()   : uma linha só, usada nas listagens e nas listas de escolha;
 *   - detalhes...() : o registro inteiro, usado em "Exibir um único registro".
 */
public final class Formatador {

    private Formatador() {
    }

    // -------------------------------------------------------------------------
    //  Apoio
    // -------------------------------------------------------------------------

    /** Mostra só o começo do UUID: o id inteiro tem 36 caracteres e polui a tela. */
    public static String idCurto(UUID id) {
        return id == null ? "-" : id.toString().substring(0, 8) + "...";
    }

    public static String dataHora(LocalDateTime data) {
        return data == null ? "-" : data.format(Entrada.FORMATO_DATA_HORA);
    }

    /** toPlainString evita que valores pequenos virem notação científica (1E-8). */
    public static String numero(BigDecimal valor) {
        return valor == null ? "-" : valor.toPlainString();
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  " + texto);
        System.out.println("=".repeat(70));
    }

    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("-- " + texto + " " + "-".repeat(Math.max(0, 66 - texto.length())));
    }

    // -------------------------------------------------------------------------
    //  Usuário
    // -------------------------------------------------------------------------

    public static String resumoUsuario(User usuario) {
        return usuario.getName() + " | " + usuario.getEmail() + " | id " + idCurto(usuario.getId());
    }

    public static String detalhesUsuario(User usuario) {
        return "  Id     : " + usuario.getId()
                + "\n  Nome   : " + usuario.getName()
                + "\n  E-mail : " + usuario.getEmail()
                + "\n  Senha  : " + usuario.getHashedPassword() + "  (hash SHA-256)";
    }

    // -------------------------------------------------------------------------
    //  Moeda
    // -------------------------------------------------------------------------

    public static String resumoMoeda(Currency moeda) {
        return moeda.getSymbol() + " - " + moeda.getName()
                + " (" + (moeda.isFiat() ? "fiduciária" : "criptomoeda") + ")";
    }

    public static String detalhesMoeda(Currency moeda) {
        return "  Id     : " + moeda.getId()
                + "\n  Símbolo: " + moeda.getSymbol()
                + "\n  Nome   : " + moeda.getName()
                + "\n  Tipo   : " + (moeda.isFiat() ? "Moeda fiduciária" : "Criptomoeda");
    }

    // -------------------------------------------------------------------------
    //  Carteira
    // -------------------------------------------------------------------------

    public static String resumoCarteira(Wallet carteira) {
        return carteira.getOwner().getName()
                + " | moeda base " + carteira.getBaseCurrency().getSymbol()
                + " | id " + idCurto(carteira.getId());
    }

    public static String detalhesCarteira(Wallet carteira) {
        return "  Id         : " + carteira.getId()
                + "\n  Dono       : " + carteira.getOwner().getName()
                + " (" + carteira.getOwner().getEmail() + ")"
                + "\n  Moeda base : " + resumoMoeda(carteira.getBaseCurrency());
    }

    // -------------------------------------------------------------------------
    //  Ativo (saldo de uma moeda dentro de uma carteira)
    // -------------------------------------------------------------------------

    public static String resumoAtivo(Asset ativo) {
        return ativo.getCurrency().getSymbol() + ": " + numero(ativo.getQuantity())
                + " | id " + idCurto(ativo.getId());
    }

    public static String detalhesAtivo(Asset ativo) {
        return "  Id        : " + ativo.getId()
                + "\n  Moeda     : " + resumoMoeda(ativo.getCurrency())
                + "\n  Quantidade: " + numero(ativo.getQuantity());
    }

    // -------------------------------------------------------------------------
    //  Transação
    // -------------------------------------------------------------------------

    public static String resumoTransacao(Transaction transacao) {
        return dataHora(transacao.getExecutedAt())
                + " | " + transacao.getType()
                + " | " + numero(transacao.getQuantity()) + " " + transacao.getCurrency().getSymbol()
                + " a " + numero(transacao.getPriceAtTime())
                + " | total " + numero(transacao.getTotalValue());
    }

    public static String detalhesTransacao(Transaction transacao) {
        return "  Id            : " + transacao.getId()
                + "\n  Carteira      : " + resumoCarteira(transacao.getWallet())
                + "\n  Moeda         : " + resumoMoeda(transacao.getCurrency())
                + "\n  Tipo          : " + transacao.getType() + " - " + descricaoTipo(transacao.getType())
                + "\n  Quantidade    : " + numero(transacao.getQuantity())
                + "\n  Preço unitário: " + numero(transacao.getPriceAtTime())
                + "\n  Valor total   : " + numero(transacao.getTotalValue())
                + "\n  Executada em  : " + dataHora(transacao.getExecutedAt());
    }

    public static String descricaoTipo(TransactionType tipo) {
        return switch (tipo) {
            case BUY -> "compra";
            case SELL -> "venda";
            case DEPOSIT -> "depósito";
            case WITHDRAW -> "saque/retirada";
        };
    }

    // -------------------------------------------------------------------------
    //  Cotação
    // -------------------------------------------------------------------------

    public static String resumoCotacao(CurrencyQuote cotacao) {
        return dataHora(cotacao.getTimestamp())
                + " | 1 " + cotacao.getCurrency().getSymbol()
                + " = " + numero(cotacao.getPrice()) + " " + cotacao.getBaseCurrency().getSymbol();
    }

    public static String detalhesCotacao(CurrencyQuote cotacao) {
        return "  Id         : " + cotacao.getId()
                + "\n  Moeda      : " + resumoMoeda(cotacao.getCurrency())
                + "\n  Cotada em  : " + resumoMoeda(cotacao.getBaseCurrency())
                + "\n  Preço      : 1 " + cotacao.getCurrency().getSymbol()
                + " = " + numero(cotacao.getPrice()) + " " + cotacao.getBaseCurrency().getSymbol()
                + "\n  Data/hora  : " + dataHora(cotacao.getTimestamp());
    }
}
