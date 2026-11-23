import java.util.ArrayList;
import java.util.List;

/**
 * Submenu do módulo de carteiras (tabela tb_wallet).
 *
 * Uma carteira pertence a um usuário e tem uma moeda base, que é a moeda em que
 * o patrimônio dela é medido. Como tb_asset e tb_transaction apontam para a
 * carteira, a exclusão oferece apagar junto tudo que está vinculado.
 */
public final class MenuCarteira {

    private static final WalletDAO walletDAO = new WalletDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final CurrencyDAO currencyDAO = new CurrencyDAO();
    private static final AssetDAO assetDAO = new AssetDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();

    private MenuCarteira() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE CARTEIRAS");
            System.out.println("  1 - Incluir carteira");
            System.out.println("  2 - Alterar carteira");
            System.out.println("  3 - Excluir carteira");
            System.out.println("  4 - Exibir todas as carteiras");
            System.out.println("  5 - Exibir uma única carteira");
            System.out.println("  6 - Exibir as carteiras de um usuário");
            System.out.println("  7 - Exibir extrato e patrimônio de uma carteira");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 7);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodas();
                case 5 -> exibirUma();
                case 6 -> exibirPorUsuario();
                case 7 -> exibirExtrato();
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    // -------------------------------------------------------------------------
    //  1 - Inclusão
    // -------------------------------------------------------------------------
    private static void incluir() {
        Formatador.subtitulo("Inclusão de carteira");

        // Validação de chave estrangeira: o dono e a moeda base precisam existir
        // no banco ANTES da carteira. Por isso os dois são escolhidos em listas
        // lidas do próprio banco, e não digitados.
        User dono = Seletor.escolher(userDAO.listarTodos(),
                "Escolha o dono da carteira:", Formatador::resumoUsuario);
        if (dono == null) {
            return;
        }

        Currency moedaBase = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda base da carteira:", Formatador::resumoMoeda);
        if (moedaBase == null) {
            return;
        }

        // Regra de negócio: não faz sentido o mesmo usuário ter duas carteiras
        // na mesma moeda base.
        for (Wallet existente : walletDAO.listarPorUsuario(dono.getId())) {
            if (existente.getBaseCurrency().getId().equals(moedaBase.getId())) {
                System.out.println(dono.getName() + " já tem uma carteira com base em "
                        + moedaBase.getSymbol() + ".");
                return;
            }
        }

        Wallet carteira = new Wallet(dono, moedaBase);

        if (walletDAO.inserir(carteira)) {
            System.out.println("Id gerado: " + carteira.getId());
        }
    }

    // -------------------------------------------------------------------------
    //  2 - Alteração
    // -------------------------------------------------------------------------
    private static void alterar() {
        Formatador.subtitulo("Alteração de carteira");

        Wallet carteira = selecionar("Escolha a carteira que deseja alterar:");
        if (carteira == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesCarteira(carteira));

        if (Entrada.confirmar("Deseja trocar o dono da carteira")) {
            User novoDono = Seletor.escolher(userDAO.listarTodos(),
                    "Escolha o novo dono:", Formatador::resumoUsuario);
            if (novoDono != null) {
                carteira.setOwner(novoDono);
            }
        }

        if (Entrada.confirmar("Deseja trocar a moeda base")) {
            Currency novaMoeda = Seletor.escolher(currencyDAO.listarTodos(),
                    "Escolha a nova moeda base:", Formatador::resumoMoeda);
            if (novaMoeda != null) {
                carteira.setBaseCurrency(novaMoeda);
            }
        }

        // Confere de novo a regra "um dono não repete moeda base", porque o dono
        // ou a moeda podem ter mudado agora.
        for (Wallet existente : walletDAO.listarPorUsuario(carteira.getOwner().getId())) {
            if (!existente.getId().equals(carteira.getId())
                    && existente.getBaseCurrency().getId().equals(carteira.getBaseCurrency().getId())) {
                System.out.println(carteira.getOwner().getName() + " já tem outra carteira com base em "
                        + carteira.getBaseCurrency().getSymbol() + ". Alteração cancelada.");
                return;
            }
        }

        walletDAO.atualizar(carteira);
    }

    // -------------------------------------------------------------------------
    //  3 - Exclusão
    // -------------------------------------------------------------------------
    private static void excluir() {
        Formatador.subtitulo("Exclusão de carteira");

        Wallet carteira = selecionar("Escolha a carteira que deseja excluir:");
        if (carteira == null) {
            return;
        }

        List<Asset> ativos = assetDAO.listarPorCarteira(carteira.getId());
        List<Transaction> transacoes = transactionDAO.listarPorCarteira(carteira);

        System.out.println();
        System.out.println(Formatador.detalhesCarteira(carteira));
        System.out.println("  Ativos vinculados     : " + ativos.size());
        System.out.println("  Transações vinculadas : " + transacoes.size());

        // tb_asset e tb_transaction têm FK para tb_wallet: enquanto existirem
        // filhos, o DELETE da carteira falha com ORA-02292. Aqui o usuário
        // decide se quer mesmo levar tudo junto.
        if (!ativos.isEmpty() || !transacoes.isEmpty()) {
            System.out.println();
            System.out.println("Esta carteira tem registros vinculados. Para excluí-la, eles");
            System.out.println("precisam ser apagados junto - e isso não pode ser desfeito.");

            if (!Entrada.confirmar("Excluir a carteira E todos os registros vinculados")) {
                System.out.println("Exclusão cancelada.");
                return;
            }

            int transacoesApagadas = transactionDAO.deletarPorCarteira(carteira.getId());
            int ativosApagados = assetDAO.deletarPorCarteira(carteira.getId());

            System.out.println("Removidos: " + transacoesApagadas + " transação(ões) e "
                    + ativosApagados + " ativo(s).");

        } else if (!Entrada.confirmar("Confirma a exclusão desta carteira")) {
            System.out.println("Exclusão cancelada.");
            return;
        }

        walletDAO.deletar(carteira.getId());
    }

    // -------------------------------------------------------------------------
    //  4 - Exibição de todas
    // -------------------------------------------------------------------------
    private static void exibirTodas() {
        Formatador.subtitulo("Carteiras cadastradas");

        List<Wallet> carteiras = walletDAO.listarTodos();

        if (carteiras.isEmpty()) {
            System.out.println("Nenhuma carteira cadastrada no banco.");
            return;
        }

        for (Wallet carteira : carteiras) {
            System.out.println("  " + Formatador.resumoCarteira(carteira));
        }

        System.out.println();
        System.out.println("Total: " + carteiras.size() + " carteira(s).");
    }

    // -------------------------------------------------------------------------
    //  5 - Exibição de uma única
    // -------------------------------------------------------------------------
    private static void exibirUma() {
        Formatador.subtitulo("Consulta de uma carteira");

        Wallet escolhida = selecionar("Escolha a carteira que deseja consultar:");
        if (escolhida == null) {
            return;
        }

        Wallet carteira = walletDAO.buscarPorId(escolhida.getId());

        if (carteira == null) {
            System.out.println("Carteira não encontrada: ela pode ter sido excluída por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesCarteira(carteira));
        System.out.println("  Ativos     : " + assetDAO.listarPorCarteira(carteira.getId()).size());
        System.out.println("  Transações : " + transactionDAO.listarPorCarteira(carteira).size());
    }

    // -------------------------------------------------------------------------
    //  6 - Opção extra: carteiras de um usuário
    // -------------------------------------------------------------------------
    private static void exibirPorUsuario() {
        Formatador.subtitulo("Carteiras de um usuário");

        User dono = Seletor.escolher(userDAO.listarTodos(),
                "Escolha o usuário:", Formatador::resumoUsuario);
        if (dono == null) {
            return;
        }

        List<Wallet> carteiras = walletDAO.listarPorUsuario(dono.getId());

        System.out.println();
        System.out.println("Usuário: " + Formatador.resumoUsuario(dono));

        if (carteiras.isEmpty()) {
            System.out.println("Este usuário ainda não tem carteiras.");
            return;
        }

        for (Wallet carteira : carteiras) {
            System.out.println("   - " + Formatador.resumoCarteira(carteira));
        }
    }

    // -------------------------------------------------------------------------
    //  7 - Opção extra: extrato e patrimônio
    // -------------------------------------------------------------------------
    private static void exibirExtrato() {
        Formatador.subtitulo("Extrato e patrimônio");

        Wallet carteira = selecionar("Escolha a carteira:");
        if (carteira == null) {
            return;
        }

        Currency base = carteira.getBaseCurrency();

        System.out.println();
        System.out.println(Formatador.detalhesCarteira(carteira));

        // Saldos
        List<Asset> ativos = assetDAO.listarPorCarteira(carteira.getId());
        System.out.println();
        System.out.println("Saldos:");
        if (ativos.isEmpty()) {
            System.out.println("   (nenhum ativo nesta carteira)");
        } else {
            for (Asset ativo : ativos) {
                System.out.println("   " + Formatador.resumoAtivo(ativo));
            }
        }

        // Movimentações
        List<Transaction> transacoes = transactionDAO.listarPorCarteira(carteira);
        System.out.println();
        System.out.println("Movimentações:");
        if (transacoes.isEmpty()) {
            System.out.println("   (nenhuma transação nesta carteira)");
        } else {
            for (Transaction transacao : transacoes) {
                System.out.println("   " + Formatador.resumoTransacao(transacao));
            }
        }

        // Patrimônio: usa o método Wallet.getTotalValue(), que converte cada
        // ativo para a moeda base usando a cotação mais recente do par.
        List<CurrencyQuote> cotacoes = new ArrayList<>();
        List<String> semCotacao = new ArrayList<>();

        for (Asset ativo : ativos) {
            carteira.appendAsset(ativo);

            if (ativo.getCurrency().getId().equals(base.getId())) {
                continue;  // o ativo já está na moeda base, não precisa converter
            }

            CurrencyQuote cotacao = quoteDAO.buscarMaisRecente(ativo.getCurrency().getId(), base.getId());
            if (cotacao != null) {
                cotacoes.add(cotacao);
            } else {
                semCotacao.add(ativo.getCurrency().getSymbol());
            }
        }

        System.out.println();
        System.out.println("Patrimônio estimado: " + carteira.getTotalValue(cotacoes) + " " + base.getSymbol());

        if (!semCotacao.isEmpty()) {
            System.out.println("Atenção: sem cotação em " + base.getSymbol() + " para "
                    + String.join(", ", semCotacao) + ".");
            System.out.println("Esses ativos ficaram de fora do total. Cadastre a cotação no módulo de cotações.");
        }
    }

    private static Wallet selecionar(String titulo) {
        return Seletor.escolher(walletDAO.listarTodos(), titulo, Formatador::resumoCarteira);
    }
}
