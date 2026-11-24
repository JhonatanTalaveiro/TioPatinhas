import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class MenuCotacao {

    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();
    private static final CurrencyDAO currencyDAO = new CurrencyDAO();

    private MenuCotacao() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE COTAÇÕES");
            System.out.println("  1 - Incluir cotação");
            System.out.println("  2 - Alterar cotação");
            System.out.println("  3 - Excluir cotação");
            System.out.println("  4 - Exibir todas as cotações");
            System.out.println("  5 - Exibir uma única cotação");
            System.out.println("  6 - Exibir a cotação mais recente de um par");
            System.out.println("  7 - Exibir o histórico de uma moeda");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 7);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodas();
                case 5 -> exibirUma();
                case 6 -> exibirMaisRecente();
                case 7 -> exibirHistoricoDaMoeda();
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    private static void incluir() {
        Formatador.subtitulo("Inclusão de cotação");

        Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda que está sendo cotada:", Formatador::resumoMoeda);
        if (moeda == null) {
            return;
        }

        Currency base = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda em que o preço é expresso:", Formatador::resumoMoeda);
        if (base == null) {
            return;
        }

        if (moeda.getId().equals(base.getId())) {
            System.out.println("A moeda cotada e a moeda base precisam ser diferentes.");
            return;
        }

        System.out.println("Quanto vale 1 " + moeda.getSymbol() + " em " + base.getSymbol() + "?");
        BigDecimal preco = Entrada.lerDecimal("Preço", false);

        LocalDateTime momento = Entrada.lerDataHora("Data e hora da cotação", LocalDateTime.now());

        CurrencyQuote cotacao = new CurrencyQuote(UUID.randomUUID(), moeda, base, preco, momento);

        if (quoteDAO.inserir(cotacao)) {
            System.out.println("Id gerado: " + cotacao.getId());
        }
    }

    private static void alterar() {
        Formatador.subtitulo("Alteração de cotação");

        CurrencyQuote cotacao = selecionar("Escolha a cotação que deseja alterar:");
        if (cotacao == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesCotacao(cotacao));
        System.out.println();

        if (Entrada.confirmar("Deseja trocar as moedas do par")) {
            Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                    "Escolha a moeda cotada:", Formatador::resumoMoeda);
            Currency base = Seletor.escolher(currencyDAO.listarTodos(),
                    "Escolha a moeda base:", Formatador::resumoMoeda);

            if (moeda != null && base != null) {
                if (moeda.getId().equals(base.getId())) {
                    System.out.println("A moeda cotada e a moeda base precisam ser diferentes. Alteração cancelada.");
                    return;
                }
                cotacao.setCurrency(moeda);
                cotacao.setBaseCurrency(base);
            }
        }

        System.out.println("Pressione ENTER para manter o valor atual de cada campo.");
        cotacao.setPrice(Entrada.lerDecimal("Preço", false, cotacao.getPrice()));
        cotacao.setTimestamp(Entrada.lerDataHora("Data e hora da cotação", cotacao.getTimestamp()));

        quoteDAO.atualizar(cotacao);
    }

    private static void excluir() {
        Formatador.subtitulo("Exclusão de cotação");

        CurrencyQuote cotacao = selecionar("Escolha a cotação que deseja excluir:");
        if (cotacao == null) {
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesCotacao(cotacao));
        System.out.println();
        System.out.println("Atenção: sem cotação, o patrimônio das carteiras deixa de considerar");
        System.out.println("os ativos em " + cotacao.getCurrency().getSymbol() + ".");

        if (Entrada.confirmar("Confirma a exclusão desta cotação")) {
            quoteDAO.deletar(cotacao.getId());
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static void exibirTodas() {
        Formatador.subtitulo("Cotações cadastradas");

        List<CurrencyQuote> cotacoes = quoteDAO.listarTodos();

        if (cotacoes.isEmpty()) {
            System.out.println("Nenhuma cotação cadastrada no banco.");
            return;
        }

        for (CurrencyQuote cotacao : cotacoes) {
            System.out.println("  " + Formatador.resumoCotacao(cotacao));
        }

        System.out.println();
        System.out.println("Total: " + cotacoes.size() + " cotação(ões).");
    }

    private static void exibirUma() {
        Formatador.subtitulo("Consulta de uma cotação");

        CurrencyQuote escolhida = selecionar("Escolha a cotação que deseja consultar:");
        if (escolhida == null) {
            return;
        }

        CurrencyQuote cotacao = quoteDAO.buscarPorId(escolhida.getId());

        if (cotacao == null) {
            System.out.println("Cotação não encontrada: ela pode ter sido excluída por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesCotacao(cotacao));
    }

    private static void exibirMaisRecente() {
        Formatador.subtitulo("Cotação mais recente de um par");

        Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda cotada:", Formatador::resumoMoeda);
        if (moeda == null) {
            return;
        }

        Currency base = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda base:", Formatador::resumoMoeda);
        if (base == null) {
            return;
        }

        CurrencyQuote cotacao = quoteDAO.buscarMaisRecente(moeda.getId(), base.getId());

        if (cotacao == null) {
            System.out.println("Nenhuma cotação cadastrada para o par "
                    + moeda.getSymbol() + "/" + base.getSymbol() + ".");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesCotacao(cotacao));
    }

    private static void exibirHistoricoDaMoeda() {
        Formatador.subtitulo("Histórico de cotações de uma moeda");

        Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda:", Formatador::resumoMoeda);
        if (moeda == null) {
            return;
        }

        List<CurrencyQuote> cotacoes = quoteDAO.listarPorMoeda(moeda.getId());

        System.out.println();
        System.out.println("Moeda: " + Formatador.resumoMoeda(moeda));

        if (cotacoes.isEmpty()) {
            System.out.println("Esta moeda ainda não tem cotações cadastradas.");
            return;
        }

        for (CurrencyQuote cotacao : cotacoes) {
            System.out.println("   " + Formatador.resumoCotacao(cotacao));
        }

        System.out.println();
        System.out.println("Total: " + cotacoes.size() + " cotação(ões), da mais antiga para a mais nova.");
    }

    private static CurrencyQuote selecionar(String titulo) {
        return Seletor.escolher(quoteDAO.listarTodos(), titulo, Formatador::resumoCotacao);
    }
}
