import java.sql.Connection;
import java.sql.SQLException;

/**
 * VOLTZ - MISSÃO TIO PATINHAS
 * =============================================================================
 *  Programa principal: menu de texto que dá acesso a todos os módulos do
 *  sistema. Cada módulo tem o seu próprio submenu com Inclusão, Alteração,
 *  Exclusão, Exibição de todos e Exibição de um único registro, e todas as
 *  operações leem e gravam nas tabelas do Oracle através da camada DAO.
 *
 *  Organização das classes (uma por arquivo):
 *
 *   - Entidades      : User, Currency, Wallet, Asset, Transaction,
 *                      CurrencyQuote, TransactionType, BaseEntity
 *   - Acesso a dados : ConnectionFactory e os seis DAOs
 *   - Telas          : Main e as classes Menu*
 *   - Apoio          : Entrada, Validador, Seguranca, Seletor, Formatador
 *   - Fases          : DemonstracaoEmMemoria (Fase 5),
 *                      TesteIntegracaoBanco (Fase 6)
 *
 *  Antes de rodar, execute o script sql/create_tables.sql no seu usuário do
 *  Oracle e confira o usuário e a senha em ConnectionFactory.java.
 * =============================================================================
 */
public class Main {

    private static final CurrencyDAO currencyDAO = new CurrencyDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final AssetDAO assetDAO = new AssetDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();

    public static void main(String[] args) {
        apresentar();

        // Se o banco não responde, nenhuma opção do sistema vai funcionar
        // direito. Melhor avisar agora, de uma vez, do que deixar o usuário
        // levar um erro de SQL em cada tela.
        if (!conexaoDisponivel() && !Entrada.confirmar("Deseja abrir o sistema mesmo assim")) {
            System.out.println("Programa encerrado.");
            return;
        }

        int opcao;

        do {
            Formatador.titulo("VOLTZ - MENU PRINCIPAL");
            System.out.println("  1 - Usuários");
            System.out.println("  2 - Moedas");
            System.out.println("  3 - Carteiras");
            System.out.println("  4 - Ativos (saldos das carteiras)");
            System.out.println("  5 - Transações");
            System.out.println("  6 - Cotações");
            System.out.println("  7 - Utilitários e testes");
            System.out.println("  0 - Sair");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 7);

            switch (opcao) {
                case 1 -> MenuUsuario.exibir();
                case 2 -> MenuMoeda.exibir();
                case 3 -> MenuCarteira.exibir();
                case 4 -> MenuAtivo.exibir();
                case 5 -> MenuTransacao.exibir();
                case 6 -> MenuCotacao.exibir();
                case 7 -> menuUtilitarios();
                default -> System.out.println("Encerrando o VOLTZ. Até a próxima!");
            }

        } while (opcao != 0);
    }

    // -------------------------------------------------------------------------
    //  Menu de utilitários
    // -------------------------------------------------------------------------
    private static void menuUtilitarios() {
        int opcao;

        do {
            Formatador.titulo("UTILITÁRIOS E TESTES");
            System.out.println("  1 - Testar a conexão com o Oracle");
            System.out.println("  2 - Resumo dos registros do banco");
            System.out.println("  3 - Rodar os testes de integração com o banco (Fase 6)");
            System.out.println("  4 - Rodar a demonstração em memória (Fase 5)");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 4);

            switch (opcao) {
                case 1 -> conexaoDisponivel();
                case 2 -> resumoDoBanco();
                case 3 -> TesteIntegracaoBanco.executar();
                case 4 -> {
                    Formatador.subtitulo("Demonstração em memória (Fase 5)");
                    System.out.println("Esta opção não usa o banco: ela monta os objetos em Collections");
                    System.out.println("e grava usuarios.txt, moedas.txt e carteiras.txt na pasta do projeto.");
                    System.out.println();
                    DemonstracaoEmMemoria.executar();
                }
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    // -------------------------------------------------------------------------
    //  Apoio
    // -------------------------------------------------------------------------

    private static void apresentar() {
        Formatador.titulo("VOLTZ - MISSÃO TIO PATINHAS");
        System.out.println("  Sistema de carteiras de moedas e criptomoedas.");
        System.out.println("  Os dados são lidos e gravados no banco de dados Oracle.");
    }

    /** Abre e fecha uma conexão só para confirmar que o banco está acessível. */
    private static boolean conexaoDisponivel() {
        System.out.println();
        System.out.println("Conectando ao banco de dados...");

        try (Connection conexao = ConnectionFactory.getConnection()) {
            System.out.println("Conexão estabelecida com sucesso.");
            System.out.println("Usuário conectado: " + conexao.getMetaData().getUserName());
            return true;

        } catch (SQLException e) {
            System.out.println();
            System.out.println("NÃO FOI POSSÍVEL CONECTAR AO BANCO.");
            System.out.println("Motivo: " + e.getMessage());
            System.out.println();
            System.out.println("O que conferir:");
            System.out.println("  - usuário e senha do Oracle da FIAP em ConnectionFactory.java;");
            System.out.println("  - se o driver ojdbc está no classpath (dependência do pom.xml);");
            System.out.println("  - se o script sql/create_tables.sql já foi executado;");
            System.out.println("  - se você está na rede/VPN que alcança oracle.fiap.com.br.");
            return false;
        }
    }

    /** Conta quantos registros existem em cada tabela, para uma conferência rápida. */
    private static void resumoDoBanco() {
        Formatador.subtitulo("Resumo dos registros do banco");

        System.out.println("  tb_user           : " + userDAO.listarTodos().size() + " usuário(s)");
        System.out.println("  tb_currency       : " + currencyDAO.listarTodos().size() + " moeda(s)");
        System.out.println("  tb_wallet         : " + walletDAO.listarTodos().size() + " carteira(s)");
        System.out.println("  tb_asset          : " + assetDAO.listarTodos().size() + " ativo(s)");
        System.out.println("  tb_transaction    : " + transactionDAO.listarTodos().size() + " transação(ões)");
        System.out.println("  tb_currency_quote : " + quoteDAO.listarTodos().size() + " cotação(ões)");
    }
}
