import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class Main {

    // Camada DAO: uma instância por tabela do banco (Fase 6)
    private static final CurrencyDAO currencyDAO = new CurrencyDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final AssetDAO assetDAO = new AssetDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();

    // Placar dos testes de integração
    private static int testesOk = 0;
    private static int testesFalha = 0;

    public static void main(String[] args) {
        demonstracaoEmMemoria();
        testarIntegracaoComBanco();
    }

    // =========================================================================
    //  PARTE 1 - DEMONSTRAÇÃO EM MEMÓRIA (Fase 5)
    //  Usa só objetos Java e Collections, sem tocar no banco.
    // =========================================================================
    private static void demonstracaoEmMemoria() {
        try {
            // Initialize users
            User user1 = new User("user1@gmail.com", "1234", "user1");
            User user2 = new User("user2@gmail.com", "1234", "user2");

            ArrayList<User> users = new ArrayList<>();
            users.add(user1);
            users.add(user2);

            System.out.println("Users: " + users);

            // Initialize currencies
            ArrayList<Currency> supportedCurrencies = new ArrayList<>();

            Currency BRL = new Currency("BRL", "Real Brasileiro", true);
            Currency USD = new Currency("USD", "Dólar dos Estados Unidos", true);

            ArrayList<Currency> fiatCurrencies = new ArrayList<>();
            fiatCurrencies.add(BRL);
            fiatCurrencies.add(USD);

            supportedCurrencies.addAll(fiatCurrencies);

            Currency BTC = new Currency("BTC", "Bitcoin", false);
            Currency ETH = new Currency("ETH", "Ethereum", false);

            ArrayList<Currency> cryptoCurrencies = new ArrayList<>();
            cryptoCurrencies.add(BTC);
            cryptoCurrencies.add(ETH);

            supportedCurrencies.addAll(cryptoCurrencies);

            HashMap<String, Currency> currenciesByCode = new HashMap<>();
            for (Currency c : supportedCurrencies) {
                currenciesByCode.put(c.getSymbol(), c);
            }

            // Initialize wallets
            HashMap<User, ArrayList<Wallet>> walletsByUser = new HashMap<>();

            ArrayList<Wallet> user1Wallets = new ArrayList<>();
            user1Wallets.add(new Wallet(user1, BRL));
            user1Wallets.add(new Wallet(user1, USD));
            walletsByUser.put(user1, user1Wallets);

            ArrayList<Wallet> user2Wallets = new ArrayList<>();
            user2Wallets.add(new Wallet(user2, BRL));
            user2Wallets.add(new Wallet(user2, USD));
            walletsByUser.put(user2, user2Wallets);

            // Start operations
            System.out.println("User 1:");
            System.out.println(user1);

            walletsByUser.get(user1).get(0).deposit(currenciesByCode.get("BTC"), new BigDecimal("0.0002"));
            walletsByUser.get(user1).get(1).deposit(currenciesByCode.get("BTC"), new BigDecimal("0.0001"));

            System.out.println("Wallet 1:");
            System.out.println(walletsByUser.get(user1).get(0));
            System.out.println("BRL: " + walletsByUser.get(user1).get(0).getBalance(BRL));
            System.out.println("BTC: " + walletsByUser.get(user1).get(0).getBalance("BTC"));

            System.out.println("Wallet 2:");
            System.out.println(walletsByUser.get(user1).get(1));
            System.out.println("BRL: " + walletsByUser.get(user1).get(1).getBalance(BRL));
            System.out.println("BTC: " + walletsByUser.get(user1).get(1).getBalance("BTC"));

            System.out.println("User 2:");
            System.out.println(user2);

            walletsByUser.get(user2).get(0).deposit(currenciesByCode.get("ETH"), new BigDecimal("0.0003"));
            walletsByUser.get(user2).get(1).deposit(currenciesByCode.get("ETH"), new BigDecimal("0.0005"));

            System.out.println("Wallet 1:");
            System.out.println(walletsByUser.get(user2).get(0));
            System.out.println("BRL: " + walletsByUser.get(user2).get(0).getBalance(BRL));
            System.out.println("ETH: " + walletsByUser.get(user2).get(0).getBalance("ETH"));

            System.out.println("Wallet 2:");
            System.out.println(walletsByUser.get(user2).get(1));
            System.out.println("BRL: " + walletsByUser.get(user2).get(1).getBalance(BRL));
            System.out.println("ETH: " + walletsByUser.get(user2).get(1).getBalance("ETH"));

            // Criar / atualizar arquivo de usuários
            FileWriter writerUsers = new FileWriter("usuarios.txt");

            for (User user : users) {
                writerUsers.write(user.toString() + "\n");
            }

            writerUsers.close();

            // Criar / atualizar arquivo de moedas
            FileWriter writerCurrencies = new FileWriter("moedas.txt");

            for (Currency currency : supportedCurrencies) {
                writerCurrencies.write(currency.toString() + "\n");
            }

            writerCurrencies.close();


            // Criar / atualizar arquivo de carteiras por usuário
            FileWriter writerWallets = new FileWriter("carteiras.txt");

            for (User user : walletsByUser.keySet()) {
                writerWallets.write("Usuário: " + user.toString() + "\n");

                ArrayList<Wallet> wallets = walletsByUser.get(user);

                for (Wallet wallet : wallets) {
                    writerWallets.write(wallet.toString() + "\n");
                }

                writerWallets.write("----------------------\n");
            }

            writerWallets.close();

            System.out.println("Arquivos criados/atualizados com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println("Erro de argumento: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    // =========================================================================
    //  PARTE 2 - TESTES DE INTEGRAÇÃO COM O BANCO ORACLE (Fase 6)
    // -------------------------------------------------------------------------
    //  Exercita INSERT, SELECT, UPDATE e DELETE nas 6 tabelas da modelagem,
    //  através da camada DAO.
    //
    //  Dois cuidados importantes:
    //
    //  1. O banco da FIAP é compartilhado e já tem os dados do script DML.
    //     Por isso os testes criam os PRÓPRIOS registros (ids novos e símbolo/
    //     e-mail com sufixo único) e, no final, apagam só o que criaram.
    //     Nenhuma linha pré-existente é alterada ou removida.
    //
    //  2. A ordem respeita as chaves estrangeiras: ao criar, vai das tabelas
    //     pai para as filhas (moeda e usuário primeiro); ao apagar, faz o
    //     caminho inverso. Fora dessa ordem o Oracle recusa com ORA-02291
    //     (parent key not found) ou ORA-02292 (child record found).
    // =========================================================================
    private static void testarIntegracaoComBanco() {
        cabecalho("TESTES DE INTEGRAÇÃO COM O BANCO DE DADOS ORACLE");

        if (!bancoDisponivel()) {
            return;
        }

        // Sufixo único por execução: permite rodar a Main várias vezes seguidas
        // sem esbarrar nas constraints UNIQUE de tb_currency.symbol e tb_user.email.
        String sufixo = String.valueOf(System.currentTimeMillis() % 100000);

        // Cada etapa devolve null se o registro não chegou a ser gravado. Como os
        // DAOs capturam SQLException em vez de propagá-la, esse null é o sinal de
        // que algo falhou (tabela inexistente, por exemplo) - e aí as etapas
        // seguintes são puladas, em vez de quebrar com NullPointerException.
        Currency[] moedas = testarCurrencyDAO(sufixo);
        Currency moedaBase = moedas != null ? moedas[0] : null;
        Currency cripto = moedas != null ? moedas[1] : null;

        User usuario = moedas != null ? testarUserDAO(sufixo) : null;
        Wallet carteira = usuario != null ? testarWalletDAO(usuario, moedaBase, cripto) : null;
        Asset ativo = carteira != null ? testarAssetDAO(carteira, cripto) : null;
        Transaction transacao = carteira != null ? testarTransactionDAO(carteira, cripto) : null;
        // Cotação depende só das moedas, não da carteira.
        CurrencyQuote cotacao = moedas != null ? testarCurrencyQuoteDAO(cripto, moedaBase) : null;

        if (carteira == null) {
            System.out.println();
            System.out.println("Os testes foram interrompidos: um registro não chegou a ser gravado.");
            System.out.println("Confira se o script sql/create_tables.sql já foi executado no seu usuário");
            System.out.println("do Oracle e veja as mensagens de erro acima.");
        }

        limparDadosDeTeste(cotacao, transacao, ativo, carteira, usuario, moedaBase, cripto);

        placarFinal();
    }

    // -------------------------------------------------------------------------
    //  1. tb_currency
    // -------------------------------------------------------------------------
    // Devolve {moeda base, cripto} relidas do banco, ou null se o INSERT falhou.
    private static Currency[] testarCurrencyDAO(String sufixo) {
        secao("1. tb_currency (CurrencyDAO)");

        // Símbolo tem no máximo 10 caracteres no DDL: "TB" + 5 dígitos cabe.
        Currency base = new Currency("TB" + sufixo, "Moeda Base de Teste", true);
        Currency cripto = new Currency("TC" + sufixo, "Cripto de Teste", false);

        // INSERT
        currencyDAO.inserir(base);
        currencyDAO.inserir(cripto);

        // SELECT por id
        Currency lida = currencyDAO.buscarPorId(base.getId());
        verificar("INSERT + SELECT por id devolvem a moeda base", lida != null);
        if (lida != null) {
            verificar("Símbolo gravado corretamente", base.getSymbol().equals(lida.getSymbol()));
            verificar("Flag isFiat gravada corretamente (true)", lida.isFiat());
        }

        // SELECT por símbolo (coluna UNIQUE)
        Currency porSimbolo = currencyDAO.buscarPorSimbolo(cripto.getSymbol());
        verificar("SELECT por símbolo encontra a cripto de teste", porSimbolo != null);
        if (porSimbolo != null) {
            verificar("Id bate com o da moeda inserida", cripto.getId().equals(porSimbolo.getId()));
            verificar("Flag isFiat gravada corretamente (false)", !porSimbolo.isFiat());
        }

        // SELECT de todos + uso de Collections (HashMap indexado pelo símbolo)
        List<Currency> todas = currencyDAO.listarTodos();
        Map<String, Currency> porCodigo = new HashMap<>();
        for (Currency c : todas) {
            porCodigo.put(c.getSymbol(), c);
        }
        System.out.println("   Moedas cadastradas no banco: " + todas.size());
        verificar("SELECT de todos inclui as duas moedas de teste",
                porCodigo.containsKey(base.getSymbol()) && porCodigo.containsKey(cripto.getSymbol()));

        // SELECT filtrado por tipo
        List<Currency> criptos = currencyDAO.listarPorTipo(false);
        verificar("SELECT por tipo traz a cripto e não traz a fiduciária",
                contemId(criptos, cripto.getId()) && !contemId(criptos, base.getId()));

        // UPDATE - Currency é imutável, então montamos outro objeto com o mesmo id
        Currency renomeada = new Currency(base.getId(), base.getSymbol(), "Moeda Base Renomeada", true);
        currencyDAO.atualizar(renomeada);

        Currency depoisDoUpdate = currencyDAO.buscarPorId(base.getId());
        verificar("UPDATE alterou o nome da moeda",
                depoisDoUpdate != null && "Moeda Base Renomeada".equals(depoisDoUpdate.getName()));

        // O DELETE desta tabela fica para o final: carteira, ativo, transação e
        // cotação ainda vão apontar para estas moedas.
        if (depoisDoUpdate == null || porSimbolo == null) {
            return null;
        }
        return new Currency[]{depoisDoUpdate, porSimbolo};
    }

    // -------------------------------------------------------------------------
    //  2. tb_user
    // -------------------------------------------------------------------------
    private static User testarUserDAO(String sufixo) {
        secao("2. tb_user (UserDAO)");

        User novo = new User("teste" + sufixo + "@fiap.com.br", "senha123hash", "Usuário de Teste");

        // INSERT
        userDAO.inserir(novo);

        // SELECT por id
        User lido = userDAO.buscarPorId(novo.getId());
        verificar("INSERT + SELECT por id devolvem o usuário", lido != null);
        if (lido == null) {
            return null;
        }
        verificar("E-mail gravado corretamente", novo.getEmail().equals(lido.getEmail()));
        verificar("Nome gravado corretamente", novo.getName().equals(lido.getName()));
        verificar("Hash da senha gravado corretamente",
                novo.getHashedPassword().equals(lido.getHashedPassword()));

        // SELECT de todos
        List<User> todos = userDAO.listarTodos();
        System.out.println("   Usuários cadastrados no banco: " + todos.size());
        verificar("SELECT de todos inclui o usuário de teste", contemId(todos, novo.getId()));

        // UPDATE
        novo.setName("Usuário Atualizado");
        novo.setEmail("atualizado" + sufixo + "@fiap.com.br");
        userDAO.atualizar(novo);

        User depoisDoUpdate = userDAO.buscarPorId(novo.getId());
        verificar("UPDATE alterou nome e e-mail",
                depoisDoUpdate != null
                        && "Usuário Atualizado".equals(depoisDoUpdate.getName())
                        && novo.getEmail().equals(depoisDoUpdate.getEmail()));

        return novo;
    }

    // -------------------------------------------------------------------------
    //  3. tb_wallet
    // -------------------------------------------------------------------------
    private static Wallet testarWalletDAO(User dono, Currency moedaBase, Currency outraMoeda) {
        secao("3. tb_wallet (WalletDAO)");

        Wallet carteira = new Wallet(dono, moedaBase);

        // INSERT
        walletDAO.inserir(carteira);

        // SELECT por id - a consulta traz o usuário e a moeda via JOIN
        Wallet lida = walletDAO.buscarPorId(carteira.getId());
        verificar("INSERT + SELECT por id devolvem a carteira", lida != null);
        if (lida == null) {
            return null;
        }
        verificar("JOIN trouxe o dono correto", dono.getId().equals(lida.getOwner().getId()));
        verificar("JOIN trouxe a moeda base correta",
                moedaBase.getId().equals(lida.getBaseCurrency().getId()));

        // SELECT filtrado por usuário
        List<Wallet> doUsuario = walletDAO.listarPorUsuario(dono.getId());
        verificar("SELECT por usuário traz a carteira criada", contemId(doUsuario, carteira.getId()));

        // SELECT de todos
        List<Wallet> todas = walletDAO.listarTodos();
        System.out.println("   Carteiras cadastradas no banco: " + todas.size());
        verificar("SELECT de todos inclui a carteira de teste", contemId(todas, carteira.getId()));

        // UPDATE - troca a moeda base da carteira
        carteira.setBaseCurrency(outraMoeda);
        walletDAO.atualizar(carteira);

        Wallet depoisDoUpdate = walletDAO.buscarPorId(carteira.getId());
        verificar("UPDATE trocou a moeda base da carteira",
                depoisDoUpdate != null
                        && outraMoeda.getId().equals(depoisDoUpdate.getBaseCurrency().getId()));

        // Volta a moeda base original para o resto dos testes
        carteira.setBaseCurrency(moedaBase);
        walletDAO.atualizar(carteira);

        return carteira;
    }

    // -------------------------------------------------------------------------
    //  4. tb_asset
    // -------------------------------------------------------------------------
    private static Asset testarAssetDAO(Wallet carteira, Currency moeda) {
        secao("4. tb_asset (AssetDAO)");

        Asset ativo = new Asset(moeda, new BigDecimal("1.50000000"));

        // INSERT - o ativo precisa saber a qual carteira pertence
        assetDAO.inserir(ativo, carteira.getId());

        // SELECT por id
        Asset lido = assetDAO.buscarPorId(ativo.getId());
        verificar("INSERT + SELECT por id devolvem o ativo", lido != null);
        if (lido != null) {
            // BigDecimal.equals também compara a escala: 1.5 e 1.50000000 seriam
            // considerados diferentes. Em valores numéricos use sempre compareTo.
            verificar("Quantidade gravada corretamente",
                    ativo.getQuantity().compareTo(lido.getQuantity()) == 0);
            verificar("JOIN trouxe a moeda correta", moeda.getId().equals(lido.getCurrency().getId()));
        }

        // SELECT filtrado por carteira
        List<Asset> daCarteira = assetDAO.listarPorCarteira(carteira.getId());
        verificar("SELECT por carteira traz o ativo criado", contemId(daCarteira, ativo.getId()));
        verificar("A carteira de teste tem exatamente 1 ativo", daCarteira.size() == 1);

        // UPDATE - simula um depósito somando à quantidade atual
        ativo.setQuantity(ativo.getQuantity().add(new BigDecimal("0.25")));
        assetDAO.atualizar(ativo);

        Asset depoisDoUpdate = assetDAO.buscarPorId(ativo.getId());
        verificar("UPDATE alterou a quantidade do ativo para 1.75",
                depoisDoUpdate != null
                        && depoisDoUpdate.getQuantity().compareTo(new BigDecimal("1.75")) == 0);

        return ativo;
    }

    // -------------------------------------------------------------------------
    //  5. tb_transaction
    // -------------------------------------------------------------------------
    private static Transaction testarTransactionDAO(Wallet carteira, Currency moeda) {
        secao("5. tb_transaction (TransactionDAO)");

        // withNano(0) evita falso negativo na comparação: a coluna TIMESTAMP do
        // Oracle guarda até microssegundos, enquanto LocalDateTime.now() pode vir
        // com nanossegundos, que seriam truncados na ida ao banco.
        LocalDateTime agora = LocalDateTime.now().withNano(0);

        Transaction transacao = new Transaction(
                UUID.randomUUID(), carteira, moeda,
                new BigDecimal("0.50000000"), new BigDecimal("1000.00"),
                TransactionType.BUY, agora);

        // INSERT
        transactionDAO.inserir(transacao);

        // SELECT por id
        Transaction lida = transactionDAO.buscarPorId(transacao.getId());
        verificar("INSERT + SELECT por id devolvem a transação", lida != null);
        if (lida != null) {
            verificar("Tipo da transação gravado corretamente", lida.type == TransactionType.BUY);
            verificar("Quantidade gravada corretamente",
                    transacao.quantity.compareTo(lida.quantity) == 0);
            verificar("Data/hora gravada corretamente", agora.equals(lida.executedAt));
            verificar("Valor total calculado = 500.00",
                    lida.getTotalValue().compareTo(new BigDecimal("500.00")) == 0);
            verificar("Carteira associada carregada pelo DAO",
                    lida.wallet != null && carteira.getId().equals(lida.wallet.getId()));
        }

        // SELECT filtrado por carteira
        List<Transaction> daCarteira = transactionDAO.listarPorCarteira(carteira);
        verificar("SELECT por carteira traz a transação criada", contemId(daCarteira, transacao.getId()));

        // UPDATE - corrige o tipo e o preço da operação
        transacao.type = TransactionType.SELL;
        transacao.priceAtTime = new BigDecimal("1200.00");
        transactionDAO.atualizar(transacao);

        Transaction depoisDoUpdate = transactionDAO.buscarPorId(transacao.getId());
        verificar("UPDATE alterou tipo e preço da transação",
                depoisDoUpdate != null
                        && depoisDoUpdate.type == TransactionType.SELL
                        && depoisDoUpdate.priceAtTime.compareTo(new BigDecimal("1200.00")) == 0);

        return transacao;
    }

    // -------------------------------------------------------------------------
    //  6. tb_currency_quote
    // -------------------------------------------------------------------------
    private static CurrencyQuote testarCurrencyQuoteDAO(Currency moeda, Currency moedaBase) {
        secao("6. tb_currency_quote (CurrencyQuoteDAO)");

        LocalDateTime ontem = LocalDateTime.now().withNano(0).minusDays(1);
        LocalDateTime agora = LocalDateTime.now().withNano(0);

        CurrencyQuote antiga = new CurrencyQuote(
                UUID.randomUUID(), moeda, moedaBase, new BigDecimal("1000.00"), ontem);
        CurrencyQuote recente = new CurrencyQuote(
                UUID.randomUUID(), moeda, moedaBase, new BigDecimal("1100.00"), agora);

        // INSERT
        quoteDAO.inserir(antiga);
        quoteDAO.inserir(recente);

        // SELECT por id
        CurrencyQuote lida = quoteDAO.buscarPorId(recente.getId());
        verificar("INSERT + SELECT por id devolvem a cotação", lida != null);
        if (lida != null) {
            verificar("Preço gravado corretamente",
                    recente.getPrice().compareTo(lida.getPrice()) == 0);
            verificar("JOIN trouxe a moeda cotada correta",
                    moeda.getId().equals(lida.getCurrency().getId()));
            verificar("JOIN trouxe a moeda base correta",
                    moedaBase.getId().equals(lida.getBaseCurrency().getId()));
        }

        // SELECT filtrado por moeda
        List<CurrencyQuote> daMoeda = quoteDAO.listarPorMoeda(moeda.getId());
        verificar("SELECT por moeda traz as duas cotações criadas", daMoeda.size() == 2);

        // SELECT com ORDER BY + ROWNUM: a cotação mais recente do par
        CurrencyQuote maisRecente = quoteDAO.buscarMaisRecente(moeda.getId(), moedaBase.getId());
        verificar("SELECT da cotação mais recente devolve a de hoje, não a de ontem",
                maisRecente != null && recente.getId().equals(maisRecente.getId()));

        // UPDATE - corrige o preço lançado errado
        recente.setPrice(new BigDecimal("1150.00"));
        quoteDAO.atualizar(recente);

        CurrencyQuote depoisDoUpdate = quoteDAO.buscarPorId(recente.getId());
        verificar("UPDATE alterou o preço da cotação",
                depoisDoUpdate != null
                        && depoisDoUpdate.getPrice().compareTo(new BigDecimal("1150.00")) == 0);

        // Apaga já a cotação antiga, para o teste de DELETE em massa do final
        // sobrar só com a recente.
        quoteDAO.deletar(antiga.getId());
        verificar("DELETE removeu a cotação antiga", quoteDAO.buscarPorId(antiga.getId()) == null);

        return recente;
    }

    // -------------------------------------------------------------------------
    //  LIMPEZA - testa o DELETE de todas as tabelas
    // -------------------------------------------------------------------------
    //  Ordem inversa das chaves estrangeiras: primeiro os registros filhos
    //  (cotação, transação, ativo), depois carteira, e por último usuário e
    //  moedas, que são as tabelas pai.
    // -------------------------------------------------------------------------
    private static void limparDadosDeTeste(CurrencyQuote cotacao, Transaction transacao, Asset ativo,
                                           Wallet carteira, User usuario, Currency moedaBase, Currency cripto) {
        secao("7. DELETE de todos os registros de teste");

        if (cotacao != null) {
            quoteDAO.deletar(cotacao.getId());
            verificar("Cotação removida de tb_currency_quote",
                    quoteDAO.buscarPorId(cotacao.getId()) == null);
        }

        if (carteira != null) {
            // DELETE em massa: apaga todas as transações e ativos da carteira de teste
            transactionDAO.deletarPorCarteira(carteira.getId());
            verificar("Transações da carteira removidas de tb_transaction",
                    transactionDAO.listarPorCarteira(carteira).isEmpty());

            assetDAO.deletarPorCarteira(carteira.getId());
            verificar("Ativos da carteira removidos de tb_asset",
                    assetDAO.listarPorCarteira(carteira.getId()).isEmpty());

            walletDAO.deletar(carteira.getId());
            verificar("Carteira removida de tb_wallet",
                    walletDAO.buscarPorId(carteira.getId()) == null);
        }

        if (usuario != null) {
            userDAO.deletar(usuario.getId());
            verificar("Usuário removido de tb_user", userDAO.buscarPorId(usuario.getId()) == null);
        }

        if (moedaBase != null) {
            currencyDAO.deletar(moedaBase.getId());
            verificar("Moeda base removida de tb_currency",
                    currencyDAO.buscarPorId(moedaBase.getId()) == null);
        }

        if (cripto != null) {
            currencyDAO.deletar(cripto.getId());
            verificar("Cripto de teste removida de tb_currency",
                    currencyDAO.buscarPorId(cripto.getId()) == null);
        }
    }

    // =========================================================================
    //  APOIO AOS TESTES
    // =========================================================================

    // Confere a conexão antes de começar. Os DAOs capturam SQLException e só
    // imprimem a mensagem, então sem esta checagem uma senha errada apareceria
    // como dezenas de "Erro ao ..." sem explicar a causa.
    private static boolean bancoDisponivel() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("Conexão com o Oracle estabelecida com sucesso.");
            System.out.println("Usuário conectado: " + conn.getMetaData().getUserName());
            return true;
        } catch (SQLException e) {
            System.out.println("NÃO FOI POSSÍVEL CONECTAR AO BANCO.");
            System.out.println("Motivo: " + e.getMessage());
            System.out.println();
            System.out.println("Confira em ConnectionFactory.java o usuário e a senha do Oracle da FIAP,");
            System.out.println("e se o driver ojdbc está no classpath (dependência do pom.xml).");
            System.out.println("Os testes de banco foram ignorados.");
            return false;
        }
    }

    // Procura um id dentro de uma lista de entidades (todas herdam de BaseEntity)
    private static boolean contemId(List<? extends BaseEntity> lista, UUID id) {
        for (BaseEntity entidade : lista) {
            if (entidade.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private static void verificar(String descricao, boolean condicao) {
        if (condicao) {
            testesOk++;
            System.out.println("   [OK]    " + descricao);
        } else {
            testesFalha++;
            System.out.println("   [FALHA] " + descricao);
        }
    }

    private static void cabecalho(String titulo) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  " + titulo);
        System.out.println("=".repeat(70));
    }

    private static void secao(String titulo) {
        System.out.println();
        System.out.println("-- " + titulo + " " + "-".repeat(Math.max(0, 66 - titulo.length())));
    }

    private static void placarFinal() {
        cabecalho("RESULTADO DOS TESTES");
        System.out.println("  Verificações com sucesso : " + testesOk);
        System.out.println("  Verificações com falha   : " + testesFalha);
        System.out.println();
        if (testesFalha == 0) {
            System.out.println("  Integração Java + Oracle validada nas 6 tabelas da modelagem.");
        } else {
            System.out.println("  Revise as mensagens marcadas com [FALHA] acima.");
        }
        System.out.println("=".repeat(70));
    }
}
