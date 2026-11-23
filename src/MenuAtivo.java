import java.math.BigDecimal;
import java.util.List;

/**
 * Submenu do módulo de ativos (tabela tb_asset).
 *
 * Um ativo é o SALDO de uma moeda dentro de uma carteira: "a carteira 1 tem
 * 0,006 BTC". Como o ativo só existe dentro de uma carteira, quase toda opção
 * aqui começa escolhendo a carteira.
 */
public final class MenuAtivo {

    private static final AssetDAO assetDAO = new AssetDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final CurrencyDAO currencyDAO = new CurrencyDAO();

    private MenuAtivo() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE ATIVOS (SALDOS DAS CARTEIRAS)");
            System.out.println("  1 - Incluir ativo em uma carteira");
            System.out.println("  2 - Alterar ativo");
            System.out.println("  3 - Excluir ativo");
            System.out.println("  4 - Exibir todos os ativos");
            System.out.println("  5 - Exibir um único ativo");
            System.out.println("  6 - Exibir os ativos de uma carteira");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 6);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodos();
                case 5 -> exibirUm();
                case 6 -> exibirPorCarteira();
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
        Formatador.subtitulo("Inclusão de ativo");

        Wallet carteira = selecionarCarteira("Escolha a carteira que vai receber o ativo:");
        if (carteira == null) {
            return;
        }

        Currency moeda = Seletor.escolher(currencyDAO.listarTodos(),
                "Escolha a moeda do ativo:", Formatador::resumoMoeda);
        if (moeda == null) {
            return;
        }

        // Regra de negócio: o saldo de uma moeda na carteira é uma linha só.
        // Se a moeda já está lá, o certo é ALTERAR a quantidade, não incluir
        // um segundo registro - senão o saldo apareceria dividido em dois.
        for (Asset existente : assetDAO.listarPorCarteira(carteira.getId())) {
            if (existente.getCurrency().getId().equals(moeda.getId())) {
                System.out.println("Esta carteira já tem um ativo em " + moeda.getSymbol()
                        + " (saldo atual: " + Formatador.numero(existente.getQuantity()) + ").");
                System.out.println("Use a opção 2 (Alterar ativo) para mudar a quantidade.");
                return;
            }
        }

        // Zero é permitido: é normal abrir a posição de uma moeda com saldo 0.
        BigDecimal quantidade = Entrada.lerDecimal("Quantidade", true);

        Asset ativo = new Asset(moeda, quantidade);

        if (assetDAO.inserir(ativo, carteira.getId())) {
            System.out.println("Id gerado: " + ativo.getId());
        }
    }

    // -------------------------------------------------------------------------
    //  2 - Alteração
    // -------------------------------------------------------------------------
    private static void alterar() {
        Formatador.subtitulo("Alteração de ativo");

        Wallet carteira = selecionarCarteira("Escolha a carteira do ativo:");
        if (carteira == null) {
            return;
        }

        Asset ativo = selecionarAtivo(carteira, "Escolha o ativo que deseja alterar:");
        if (ativo == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesAtivo(ativo));
        System.out.println();

        if (Entrada.confirmar("Deseja trocar a moeda do ativo")) {
            Currency novaMoeda = Seletor.escolher(currencyDAO.listarTodos(),
                    "Escolha a nova moeda:", Formatador::resumoMoeda);

            if (novaMoeda != null && !novaMoeda.getId().equals(ativo.getCurrency().getId())) {
                for (Asset existente : assetDAO.listarPorCarteira(carteira.getId())) {
                    if (existente.getCurrency().getId().equals(novaMoeda.getId())) {
                        System.out.println("Esta carteira já tem um ativo em " + novaMoeda.getSymbol()
                                + ". Alteração cancelada.");
                        return;
                    }
                }
                ativo.setCurrency(novaMoeda);
            }
        }

        System.out.println("Pressione ENTER para manter a quantidade atual.");
        BigDecimal quantidade = Entrada.lerDecimal("Quantidade", true, ativo.getQuantity());
        ativo.setQuantity(quantidade);

        assetDAO.atualizar(ativo);
    }

    // -------------------------------------------------------------------------
    //  3 - Exclusão
    // -------------------------------------------------------------------------
    private static void excluir() {
        Formatador.subtitulo("Exclusão de ativo");

        Wallet carteira = selecionarCarteira("Escolha a carteira do ativo:");
        if (carteira == null) {
            return;
        }

        Asset ativo = selecionarAtivo(carteira, "Escolha o ativo que deseja excluir:");
        if (ativo == null) {
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesAtivo(ativo));

        // Aviso honesto: apagar o ativo apaga o saldo, mas o histórico de
        // transações daquela moeda continua na carteira.
        if (ativo.getQuantity().signum() > 0) {
            System.out.println();
            System.out.println("Atenção: este ativo ainda tem saldo "
                    + Formatador.numero(ativo.getQuantity()) + " " + ativo.getCurrency().getSymbol() + ".");
        }

        System.out.println();

        if (Entrada.confirmar("Confirma a exclusão deste ativo")) {
            assetDAO.deletar(ativo.getId());
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    // -------------------------------------------------------------------------
    //  4 - Exibição de todos
    // -------------------------------------------------------------------------
    private static void exibirTodos() {
        Formatador.subtitulo("Ativos cadastrados");

        List<Wallet> carteiras = walletDAO.listarTodos();

        if (carteiras.isEmpty()) {
            System.out.println("Nenhuma carteira cadastrada, então não há ativos.");
            return;
        }

        int total = 0;

        // Os ativos são listados carteira por carteira: um saldo sem a carteira
        // a que pertence não diz muita coisa.
        for (Wallet carteira : carteiras) {
            List<Asset> ativos = assetDAO.listarPorCarteira(carteira.getId());

            System.out.println();
            System.out.println("Carteira: " + Formatador.resumoCarteira(carteira));

            if (ativos.isEmpty()) {
                System.out.println("   (sem ativos)");
                continue;
            }

            for (Asset ativo : ativos) {
                System.out.println("   " + Formatador.resumoAtivo(ativo));
            }

            total += ativos.size();
        }

        System.out.println();
        System.out.println("Total: " + total + " ativo(s) em " + carteiras.size() + " carteira(s).");
    }

    // -------------------------------------------------------------------------
    //  5 - Exibição de um único
    // -------------------------------------------------------------------------
    private static void exibirUm() {
        Formatador.subtitulo("Consulta de um ativo");

        Wallet carteira = selecionarCarteira("Escolha a carteira do ativo:");
        if (carteira == null) {
            return;
        }

        Asset escolhido = selecionarAtivo(carteira, "Escolha o ativo que deseja consultar:");
        if (escolhido == null) {
            return;
        }

        Asset ativo = assetDAO.buscarPorId(escolhido.getId());

        if (ativo == null) {
            System.out.println("Ativo não encontrado: ele pode ter sido excluído por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println("  Carteira  : " + Formatador.resumoCarteira(carteira));
        System.out.println(Formatador.detalhesAtivo(ativo));
    }

    // -------------------------------------------------------------------------
    //  6 - Opção extra: ativos de uma carteira
    // -------------------------------------------------------------------------
    private static void exibirPorCarteira() {
        Formatador.subtitulo("Ativos de uma carteira");

        Wallet carteira = selecionarCarteira("Escolha a carteira:");
        if (carteira == null) {
            return;
        }

        List<Asset> ativos = assetDAO.listarPorCarteira(carteira.getId());

        System.out.println();
        System.out.println("Carteira: " + Formatador.resumoCarteira(carteira));

        if (ativos.isEmpty()) {
            System.out.println("Esta carteira ainda não tem ativos.");
            return;
        }

        for (Asset ativo : ativos) {
            System.out.println("   " + Formatador.resumoAtivo(ativo));
        }

        System.out.println();
        System.out.println("Total: " + ativos.size() + " ativo(s).");
    }

    // -------------------------------------------------------------------------
    //  Apoio
    // -------------------------------------------------------------------------

    private static Wallet selecionarCarteira(String titulo) {
        return Seletor.escolher(walletDAO.listarTodos(), titulo, Formatador::resumoCarteira);
    }

    private static Asset selecionarAtivo(Wallet carteira, String titulo) {
        List<Asset> ativos = assetDAO.listarPorCarteira(carteira.getId());

        if (ativos.isEmpty()) {
            System.out.println("Esta carteira não tem nenhum ativo cadastrado.");
            return null;
        }

        return Seletor.escolher(ativos, titulo, Formatador::resumoAtivo);
    }
}
