import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MenuTransacao {

    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final CurrencyDAO currencyDAO = new CurrencyDAO();

    private MenuTransacao() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE TRANSAÇÕES");
            System.out.println("  1 - Incluir transação");
            System.out.println("  2 - Alterar transação");
            System.out.println("  3 - Excluir transação");
            System.out.println("  4 - Exibir todas as transações");
            System.out.println("  5 - Exibir uma única transação");
            System.out.println("  6 - Exibir as transações de uma carteira");
            System.out.println("  7 - Resumo por tipo de transação");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 7);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodas();
                case 5 -> exibirUma();
                case 6 -> exibirPorCarteira();
                case 7 -> resumoPorTipo();
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    private static void incluir() {
        Formatador.subtitulo("Inclusão de transação");

        Wallet carteira = selecionarCarteira("Escolha a carteira da transação:");
        if (carteira == null) {
            return;
        }

        Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda movimentada:", Formatador::resumoMoeda);
        if (moeda == null) {
            return;
        }

        TransactionType tipo = lerTipo();

        BigDecimal quantidade = Entrada.lerDecimal("Quantidade movimentada", false);

        System.out.println("Preço de 1 " + moeda.getSymbol() + " em "
                + carteira.getBaseCurrency().getSymbol()
                + " no momento da operação (use 1 se for a própria moeda base).");
        BigDecimal preco = Entrada.lerDecimal("Preço unitário", false);

        LocalDateTime executadaEm = Entrada.lerDataHora("Data e hora da operação", LocalDateTime.now());

        Transaction transacao = new Transaction(
                UUID.randomUUID(), carteira, moeda, quantidade, preco, tipo, executadaEm);

        System.out.println();
        System.out.println("Valor total da operação: " + Formatador.numero(transacao.getTotalValue())
                + " " + carteira.getBaseCurrency().getSymbol());

        if (!Entrada.confirmar("Confirma o registro desta transação")) {
            System.out.println("Inclusão cancelada.");
            return;
        }

        if (transactionDAO.inserir(transacao)) {
            System.out.println("Id gerado: " + transacao.getId());
            System.out.println("Lembrete: o saldo da carteira fica em tb_asset. Se esta operação");
            System.out.println("mudou o saldo, ajuste o ativo no módulo de ativos.");
        }
    }

    private static void alterar() {
        Formatador.subtitulo("Alteração de transação");

        Wallet carteira = selecionarCarteira("Escolha a carteira da transação:");
        if (carteira == null) {
            return;
        }

        Transaction transacao = selecionarTransacao(carteira, "Escolha a transação que deseja alterar:");
        if (transacao == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesTransacao(transacao));
        System.out.println();

        if (Entrada.confirmar("Deseja trocar a moeda movimentada")) {
            Currency novaMoeda = Seletor.escolher(currencyDAO.listarTodos(),
                    "Escolha a nova moeda:", Formatador::resumoMoeda);
            if (novaMoeda != null) {
                transacao.setCurrency(novaMoeda);
            }
        }

        if (Entrada.confirmar("Deseja trocar o tipo da transação")) {
            transacao.setType(lerTipo());
        }

        System.out.println("Pressione ENTER para manter o valor atual de cada campo.");
        transacao.setQuantity(Entrada.lerDecimal("Quantidade", false, transacao.getQuantity()));
        transacao.setPriceAtTime(Entrada.lerDecimal("Preço unitário", false, transacao.getPriceAtTime()));
        transacao.setExecutedAt(Entrada.lerDataHora("Data e hora da operação", transacao.getExecutedAt()));

        System.out.println();
        System.out.println("Novo valor total: " + Formatador.numero(transacao.getTotalValue()));

        transactionDAO.atualizar(transacao);
    }

    private static void excluir() {
        Formatador.subtitulo("Exclusão de transação");

        Wallet carteira = selecionarCarteira("Escolha a carteira da transação:");
        if (carteira == null) {
            return;
        }

        Transaction transacao = selecionarTransacao(carteira, "Escolha a transação que deseja excluir:");
        if (transacao == null) {
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesTransacao(transacao));
        System.out.println();
        System.out.println("Atenção: excluir uma transação apaga o histórico da operação.");

        if (Entrada.confirmar("Confirma a exclusão desta transação")) {
            transactionDAO.deletar(transacao.getId());
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static void exibirTodas() {
        Formatador.subtitulo("Transações cadastradas");

        List<Transaction> transacoes = transactionDAO.listarTodos();

        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação cadastrada no banco.");
            return;
        }

        for (Transaction transacao : transacoes) {
            System.out.println("  " + transacao.getWallet().getOwner().getName()
                    + " | " + Formatador.resumoTransacao(transacao));
        }

        System.out.println();
        System.out.println("Total: " + transacoes.size() + " transação(ões).");
    }

    private static void exibirUma() {
        Formatador.subtitulo("Consulta de uma transação");

        Wallet carteira = selecionarCarteira("Escolha a carteira da transação:");
        if (carteira == null) {
            return;
        }

        Transaction escolhida = selecionarTransacao(carteira, "Escolha a transação que deseja consultar:");
        if (escolhida == null) {
            return;
        }

        Transaction transacao = transactionDAO.buscarPorId(escolhida.getId());

        if (transacao == null) {
            System.out.println("Transação não encontrada: ela pode ter sido excluída por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesTransacao(transacao));
    }

    private static void exibirPorCarteira() {
        Formatador.subtitulo("Transações de uma carteira");

        Wallet carteira = selecionarCarteira("Escolha a carteira:");
        if (carteira == null) {
            return;
        }

        List<Transaction> transacoes = transactionDAO.listarPorCarteira(carteira);

        System.out.println();
        System.out.println("Carteira: " + Formatador.resumoCarteira(carteira));

        if (transacoes.isEmpty()) {
            System.out.println("Esta carteira ainda não tem transações.");
            return;
        }

        for (Transaction transacao : transacoes) {
            System.out.println("   " + Formatador.resumoTransacao(transacao));
        }

        System.out.println();
        System.out.println("Total: " + transacoes.size() + " transação(ões).");
    }

    private static void resumoPorTipo() {
        Formatador.subtitulo("Resumo por tipo de transação");

        List<Transaction> transacoes = transactionDAO.listarTodos();

        if (transacoes.isEmpty()) {
            System.out.println("Nenhuma transação cadastrada no banco.");
            return;
        }

        Map<TransactionType, Integer> quantidadePorTipo = new LinkedHashMap<>();
        Map<TransactionType, BigDecimal> totalPorTipo = new LinkedHashMap<>();

        for (TransactionType tipo : TransactionType.values()) {
            quantidadePorTipo.put(tipo, 0);
            totalPorTipo.put(tipo, BigDecimal.ZERO);
        }

        for (Transaction transacao : transacoes) {
            TransactionType tipo = transacao.getType();
            quantidadePorTipo.put(tipo, quantidadePorTipo.get(tipo) + 1);
            totalPorTipo.put(tipo, totalPorTipo.get(tipo).add(transacao.getTotalValue()));
        }

        for (TransactionType tipo : TransactionType.values()) {
            System.out.println("  " + tipo + " (" + Formatador.descricaoTipo(tipo) + "): "
                    + quantidadePorTipo.get(tipo) + " operação(ões), valor somado "
                    + Formatador.numero(totalPorTipo.get(tipo)));
        }

        System.out.println();
        System.out.println("Observação: os valores somados misturam moedas base diferentes,");
        System.out.println("então servem como volume de operações, não como total financeiro.");
    }

    private static TransactionType lerTipo() {
        System.out.println("Tipos disponíveis:");

        TransactionType[] tipos = TransactionType.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("  " + (i + 1) + " - " + tipos[i]
                    + " (" + Formatador.descricaoTipo(tipos[i]) + ")");
        }

        return tipos[Entrada.lerInteiro("Tipo", 1, tipos.length) - 1];
    }

    private static Wallet selecionarCarteira(String titulo) {
        return Seletor.escolher(walletDAO.listarTodos(), titulo, Formatador::resumoCarteira);
    }

    private static Transaction selecionarTransacao(Wallet carteira, String titulo) {
        List<Transaction> transacoes = transactionDAO.listarPorCarteira(carteira);

        if (transacoes.isEmpty()) {
            System.out.println("Esta carteira não tem nenhuma transação cadastrada.");
            return null;
        }

        return Seletor.escolher(transacoes, titulo, Formatador::resumoTransacao);
    }
}
