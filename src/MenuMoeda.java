import java.util.ArrayList;
import java.util.List;

public final class MenuMoeda {

    private static final CurrencyDAO currencyDAO = new CurrencyDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final AssetDAO assetDAO = new AssetDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();

    private MenuMoeda() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE MOEDAS");
            System.out.println("  1 - Incluir moeda");
            System.out.println("  2 - Alterar moeda");
            System.out.println("  3 - Excluir moeda");
            System.out.println("  4 - Exibir todas as moedas");
            System.out.println("  5 - Exibir uma única moeda");
            System.out.println("  6 - Exibir por tipo (fiduciárias ou criptomoedas)");
            System.out.println("  7 - Buscar moeda pelo símbolo");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 7);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodas();
                case 5 -> exibirUma();
                case 6 -> exibirPorTipo();
                case 7 -> buscarPorSimbolo();
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    private static void incluir() {
        Formatador.subtitulo("Inclusão de moeda");

        String simbolo = Entrada.lerSimbolo("Símbolo (ex.: BRL, BTC)");

        if (currencyDAO.buscarPorSimbolo(simbolo) != null) {
            System.out.println("Já existe uma moeda cadastrada com o símbolo " + simbolo + ".");
            return;
        }

        String nome = Entrada.lerTextoObrigatorio("Nome da moeda", Validador.TAMANHO_MAXIMO_TEXTO);
        boolean fiduciaria = lerTipo();

        Currency moeda = new Currency(simbolo, nome, fiduciaria);

        if (currencyDAO.inserir(moeda)) {
            System.out.println("Id gerado: " + moeda.getId());
        }
    }

    private static void alterar() {
        Formatador.subtitulo("Alteração de moeda");

        Currency moeda = selecionar("Escolha a moeda que deseja alterar:");
        if (moeda == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesMoeda(moeda));
        System.out.println();
        System.out.println("Pressione ENTER para manter o valor atual de cada campo.");

        String simbolo = Entrada.lerSimbolo("Símbolo", moeda.getSymbol());
        String nome = Entrada.lerTextoOpcional("Nome", moeda.getName(), Validador.TAMANHO_MAXIMO_TEXTO);

        if (!simbolo.equals(moeda.getSymbol())) {
            Currency dona = currencyDAO.buscarPorSimbolo(simbolo);
            if (dona != null && !dona.getId().equals(moeda.getId())) {
                System.out.println("Já existe outra moeda com o símbolo " + simbolo + ". Alteração cancelada.");
                return;
            }
        }

        boolean fiduciaria = Entrada.confirmar("Trocar o tipo (hoje é "
                + (moeda.isFiat() ? "fiduciária" : "criptomoeda") + ")")
                ? !moeda.isFiat()
                : moeda.isFiat();

        Currency alterada = new Currency(moeda.getId(), simbolo, nome, fiduciaria);

        currencyDAO.atualizar(alterada);
    }

    private static void excluir() {
        Formatador.subtitulo("Exclusão de moeda");

        Currency moeda = selecionar("Escolha a moeda que deseja excluir:");
        if (moeda == null) {
            return;
        }

        List<String> vinculos = procurarVinculos(moeda);

        if (!vinculos.isEmpty()) {
            System.out.println();
            System.out.println("Não é possível excluir a moeda " + moeda.getSymbol()
                    + ": outros registros ainda dependem dela.");
            for (String vinculo : vinculos) {
                System.out.println("   - " + vinculo);
            }
            System.out.println("Remova esses registros antes de excluir a moeda.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesMoeda(moeda));
        System.out.println();

        if (Entrada.confirmar("Confirma a exclusão desta moeda")) {
            currencyDAO.deletar(moeda.getId());
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static void exibirTodas() {
        Formatador.subtitulo("Moedas cadastradas");

        List<Currency> moedas = currencyDAO.listarTodos();

        if (moedas.isEmpty()) {
            System.out.println("Nenhuma moeda cadastrada no banco.");
            return;
        }

        for (Currency moeda : moedas) {
            System.out.println("  " + Formatador.resumoMoeda(moeda));
        }

        System.out.println();
        System.out.println("Total: " + moedas.size() + " moeda(s).");
    }

    private static void exibirUma() {
        Formatador.subtitulo("Consulta de uma moeda");

        Currency escolhida = selecionar("Escolha a moeda que deseja consultar:");
        if (escolhida == null) {
            return;
        }

        Currency moeda = currencyDAO.buscarPorId(escolhida.getId());

        if (moeda == null) {
            System.out.println("Moeda não encontrada: ela pode ter sido excluída por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesMoeda(moeda));

        List<CurrencyQuote> cotacoes = quoteDAO.listarPorMoeda(moeda.getId());
        if (cotacoes.isEmpty()) {
            System.out.println("  Cotações   : nenhuma cadastrada");
        } else {
            CurrencyQuote ultima = cotacoes.get(cotacoes.size() - 1);
            System.out.println("  Cotações   : " + cotacoes.size()
                    + " registro(s) | última: " + Formatador.resumoCotacao(ultima));
        }
    }

    private static void exibirPorTipo() {
        Formatador.subtitulo("Moedas por tipo");

        boolean fiduciaria = lerTipo();
        List<Currency> moedas = currencyDAO.listarPorTipo(fiduciaria);

        if (moedas.isEmpty()) {
            System.out.println("Nenhuma moeda desse tipo está cadastrada.");
            return;
        }

        for (Currency moeda : moedas) {
            System.out.println("  " + Formatador.resumoMoeda(moeda));
        }

        System.out.println();
        System.out.println("Total: " + moedas.size() + " moeda(s).");
    }

    private static void buscarPorSimbolo() {
        Formatador.subtitulo("Busca pelo símbolo");

        String simbolo = Entrada.lerSimbolo("Símbolo procurado");
        Currency moeda = currencyDAO.buscarPorSimbolo(simbolo);

        if (moeda == null) {
            System.out.println("Nenhuma moeda cadastrada com o símbolo " + simbolo + ".");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesMoeda(moeda));
    }

    private static List<String> procurarVinculos(Currency moeda) {
        List<String> vinculos = new ArrayList<>();

        int carteiras = 0;
        for (Wallet carteira : walletDAO.listarTodos()) {
            if (carteira.getBaseCurrency().getId().equals(moeda.getId())) {
                carteiras++;
            }
        }
        if (carteiras > 0) {
            vinculos.add(carteiras + " carteira(s) usam esta moeda como moeda base");
        }

        int ativos = 0;
        for (Asset ativo : assetDAO.listarTodos()) {
            if (ativo.getCurrency().getId().equals(moeda.getId())) {
                ativos++;
            }
        }
        if (ativos > 0) {
            vinculos.add(ativos + " ativo(s) com saldo nesta moeda");
        }

        int transacoes = 0;
        for (Transaction transacao : transactionDAO.listarTodos()) {
            if (transacao.getCurrency().getId().equals(moeda.getId())) {
                transacoes++;
            }
        }
        if (transacoes > 0) {
            vinculos.add(transacoes + " transação(ões) registradas nesta moeda");
        }

        int cotacoes = 0;
        for (CurrencyQuote cotacao : quoteDAO.listarTodos()) {
            if (cotacao.getCurrency().getId().equals(moeda.getId())
                    || cotacao.getBaseCurrency().getId().equals(moeda.getId())) {
                cotacoes++;
            }
        }
        if (cotacoes > 0) {
            vinculos.add(cotacoes + " cotação(ões) envolvendo esta moeda");
        }

        return vinculos;
    }

    private static boolean lerTipo() {
        System.out.println("  1 - Moeda fiduciária (real, dólar, euro...)");
        System.out.println("  2 - Criptomoeda (bitcoin, ethereum...)");
        return Entrada.lerInteiro("Tipo", 1, 2) == 1;
    }

    private static Currency selecionar(String titulo) {
        return Seletor.escolher(currencyDAO.listarTodos(), titulo, Formatador::resumoMoeda);
    }
}
