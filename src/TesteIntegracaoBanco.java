import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TesteIntegracaoBanco {

    private static final CurrencyDAO currencyDAO = new CurrencyDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final WalletDAO walletDAO = new WalletDAO();
    private static final AssetDAO assetDAO = new AssetDAO();
    private static final TransactionDAO transactionDAO = new TransactionDAO();
    private static final CurrencyQuoteDAO quoteDAO = new CurrencyQuoteDAO();

    private static int testesOk = 0;
    private static int testesFalha = 0;

    private TesteIntegracaoBanco() {
    }

    public static void executar() {
        testesOk = 0;
        testesFalha = 0;

        cabecalho("TESTES DE INTEGRAÇÃO COM O BANCO DE DADOS ORACLE");

        if (!bancoDisponivel()) {
            return;
        }

        String sufixo = String.valueOf(System.currentTimeMillis() % 100000);

        Currency[] moedas = testarCurrencyDAO(sufixo);
        Currency moedaBase = moedas != null ? moedas[0] : null;
        Currency cripto = moedas != null ? moedas[1] : null;

        User usuario = moedas != null ? testarUserDAO(sufixo) : null;
        Wallet carteira = usuario != null ? testarWalletDAO(usuario, moedaBase, cripto) : null;
        Asset ativo = carteira != null ? testarAssetDAO(carteira, cripto) : null;
        Transaction transacao = carteira != null ? testarTransactionDAO(carteira, cripto) : null;
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

    private static Currency[] testarCurrencyDAO(String sufixo) {
        secao("1. tb_currency (CurrencyDAO)");

        Currency base = new Currency("TB" + sufixo, "Moeda Base de Teste", true);
        Currency cripto = new Currency("TC" + sufixo, "Cripto de Teste", false);

        currencyDAO.inserir(base);
        currencyDAO.inserir(cripto);

        Currency lida = currencyDAO.buscarPorId(base.getId());
        verificar("INSERT + SELECT por id devolvem a moeda base", lida != null);
        if (lida != null) {
            verificar("Símbolo gravado corretamente", base.getSymbol().equals(lida.getSymbol()));
            verificar("Flag isFiat gravada corretamente (true)", lida.isFiat());
        }

        Currency porSimbolo = currencyDAO.buscarPorSimbolo(cripto.getSymbol());
        verificar("SELECT por símbolo encontra a cripto de teste", porSimbolo != null);
        if (porSimbolo != null) {
            verificar("Id bate com o da moeda inserida", cripto.getId().equals(porSimbolo.getId()));
            verificar("Flag isFiat gravada corretamente (false)", !porSimbolo.isFiat());
        }

        List<Currency> todas = currencyDAO.listarTodos();
        Map<String, Currency> porCodigo = new HashMap<>();
        for (Currency c : todas) {
            porCodigo.put(c.getSymbol(), c);
        }
        System.out.println("   Moedas cadastradas no banco: " + todas.size());
        verificar("SELECT de todos inclui as duas moedas de teste",
                porCodigo.containsKey(base.getSymbol()) && porCodigo.containsKey(cripto.getSymbol()));

        List<Currency> criptos = currencyDAO.listarPorTipo(false);
        verificar("SELECT por tipo traz a cripto e não traz a fiduciária",
                contemId(criptos, cripto.getId()) && !contemId(criptos, base.getId()));

        Currency renomeada = new Currency(base.getId(), base.getSymbol(), "Moeda Base Renomeada", true);
        currencyDAO.atualizar(renomeada);

        Currency depoisDoUpdate = currencyDAO.buscarPorId(base.getId());
        verificar("UPDATE alterou o nome da moeda",
                depoisDoUpdate != null && "Moeda Base Renomeada".equals(depoisDoUpdate.getName()));

        if (depoisDoUpdate == null || porSimbolo == null) {
            return null;
        }
        return new Currency[]{depoisDoUpdate, porSimbolo};
    }

    private static User testarUserDAO(String sufixo) {
        secao("2. tb_user (UserDAO)");

        User novo = new User("teste" + sufixo + "@fiap.com.br", "senha123hash", "Usuário de Teste");

        userDAO.inserir(novo);

        User lido = userDAO.buscarPorId(novo.getId());
        verificar("INSERT + SELECT por id devolvem o usuário", lido != null);
        if (lido == null) {
            return null;
        }
        verificar("E-mail gravado corretamente", novo.getEmail().equals(lido.getEmail()));
        verificar("Nome gravado corretamente", novo.getName().equals(lido.getName()));
        verificar("Hash da senha gravado corretamente",
                novo.getHashedPassword().equals(lido.getHashedPassword()));

        User porEmail = userDAO.buscarPorEmail(novo.getEmail());
        verificar("SELECT por e-mail encontra o usuário de teste",
                porEmail != null && novo.getId().equals(porEmail.getId()));

        List<User> todos = userDAO.listarTodos();
        System.out.println("   Usuários cadastrados no banco: " + todos.size());
        verificar("SELECT de todos inclui o usuário de teste", contemId(todos, novo.getId()));

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

    private static Wallet testarWalletDAO(User dono, Currency moedaBase, Currency outraMoeda) {
        secao("3. tb_wallet (WalletDAO)");

        Wallet carteira = new Wallet(dono, moedaBase);

        walletDAO.inserir(carteira);

        Wallet lida = walletDAO.buscarPorId(carteira.getId());
        verificar("INSERT + SELECT por id devolvem a carteira", lida != null);
        if (lida == null) {
            return null;
        }
        verificar("JOIN trouxe o dono correto", dono.getId().equals(lida.getOwner().getId()));
        verificar("JOIN trouxe a moeda base correta",
                moedaBase.getId().equals(lida.getBaseCurrency().getId()));

        List<Wallet> doUsuario = walletDAO.listarPorUsuario(dono.getId());
        verificar("SELECT por usuário traz a carteira criada", contemId(doUsuario, carteira.getId()));

        List<Wallet> todas = walletDAO.listarTodos();
        System.out.println("   Carteiras cadastradas no banco: " + todas.size());
        verificar("SELECT de todos inclui a carteira de teste", contemId(todas, carteira.getId()));

        carteira.setBaseCurrency(outraMoeda);
        walletDAO.atualizar(carteira);

        Wallet depoisDoUpdate = walletDAO.buscarPorId(carteira.getId());
        verificar("UPDATE trocou a moeda base da carteira",
                depoisDoUpdate != null
                        && outraMoeda.getId().equals(depoisDoUpdate.getBaseCurrency().getId()));

        carteira.setBaseCurrency(moedaBase);
        walletDAO.atualizar(carteira);

        return carteira;
    }

    private static Asset testarAssetDAO(Wallet carteira, Currency moeda) {
        secao("4. tb_asset (AssetDAO)");

        Asset ativo = new Asset(moeda, new BigDecimal("1.50000000"));

        assetDAO.inserir(ativo, carteira.getId());

        Asset lido = assetDAO.buscarPorId(ativo.getId());
        verificar("INSERT + SELECT por id devolvem o ativo", lido != null);
        if (lido != null) {
            verificar("Quantidade gravada corretamente",
                    ativo.getQuantity().compareTo(lido.getQuantity()) == 0);
            verificar("JOIN trouxe a moeda correta", moeda.getId().equals(lido.getCurrency().getId()));
        }

        List<Asset> daCarteira = assetDAO.listarPorCarteira(carteira.getId());
        verificar("SELECT por carteira traz o ativo criado", contemId(daCarteira, ativo.getId()));
        verificar("A carteira de teste tem exatamente 1 ativo", daCarteira.size() == 1);

        ativo.setQuantity(ativo.getQuantity().add(new BigDecimal("0.25")));
        assetDAO.atualizar(ativo);

        Asset depoisDoUpdate = assetDAO.buscarPorId(ativo.getId());
        verificar("UPDATE alterou a quantidade do ativo para 1.75",
                depoisDoUpdate != null
                        && depoisDoUpdate.getQuantity().compareTo(new BigDecimal("1.75")) == 0);

        return ativo;
    }

    private static Transaction testarTransactionDAO(Wallet carteira, Currency moeda) {
        secao("5. tb_transaction (TransactionDAO)");

        LocalDateTime agora = LocalDateTime.now().withNano(0);

        Transaction transacao = new Transaction(
                UUID.randomUUID(), carteira, moeda,
                new BigDecimal("0.50000000"), new BigDecimal("1000.00"),
                TransactionType.BUY, agora);

        transactionDAO.inserir(transacao);

        Transaction lida = transactionDAO.buscarPorId(transacao.getId());
        verificar("INSERT + SELECT por id devolvem a transação", lida != null);
        if (lida != null) {
            verificar("Tipo da transação gravado corretamente", lida.getType() == TransactionType.BUY);
            verificar("Quantidade gravada corretamente",
                    transacao.getQuantity().compareTo(lida.getQuantity()) == 0);
            verificar("Data/hora gravada corretamente", agora.equals(lida.getExecutedAt()));
            verificar("Valor total calculado = 500.00",
                    lida.getTotalValue().compareTo(new BigDecimal("500.00")) == 0);
            verificar("Carteira associada carregada pelo DAO",
                    lida.getWallet() != null && carteira.getId().equals(lida.getWallet().getId()));
        }

        List<Transaction> daCarteira = transactionDAO.listarPorCarteira(carteira);
        verificar("SELECT por carteira traz a transação criada", contemId(daCarteira, transacao.getId()));

        transacao.setType(TransactionType.SELL);
        transacao.setPriceAtTime(new BigDecimal("1200.00"));
        transactionDAO.atualizar(transacao);

        Transaction depoisDoUpdate = transactionDAO.buscarPorId(transacao.getId());
        verificar("UPDATE alterou tipo e preço da transação",
                depoisDoUpdate != null
                        && depoisDoUpdate.getType() == TransactionType.SELL
                        && depoisDoUpdate.getPriceAtTime().compareTo(new BigDecimal("1200.00")) == 0);

        return transacao;
    }

    private static CurrencyQuote testarCurrencyQuoteDAO(Currency moeda, Currency moedaBase) {
        secao("6. tb_currency_quote (CurrencyQuoteDAO)");

        LocalDateTime ontem = LocalDateTime.now().withNano(0).minusDays(1);
        LocalDateTime agora = LocalDateTime.now().withNano(0);

        CurrencyQuote antiga = new CurrencyQuote(
                UUID.randomUUID(), moeda, moedaBase, new BigDecimal("1000.00"), ontem);
        CurrencyQuote recente = new CurrencyQuote(
                UUID.randomUUID(), moeda, moedaBase, new BigDecimal("1100.00"), agora);

        quoteDAO.inserir(antiga);
        quoteDAO.inserir(recente);

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

        List<CurrencyQuote> daMoeda = quoteDAO.listarPorMoeda(moeda.getId());
        verificar("SELECT por moeda traz as duas cotações criadas", daMoeda.size() == 2);

        CurrencyQuote maisRecente = quoteDAO.buscarMaisRecente(moeda.getId(), moedaBase.getId());
        verificar("SELECT da cotação mais recente devolve a de hoje, não a de ontem",
                maisRecente != null && recente.getId().equals(maisRecente.getId()));

        recente.setPrice(new BigDecimal("1150.00"));
        quoteDAO.atualizar(recente);

        CurrencyQuote depoisDoUpdate = quoteDAO.buscarPorId(recente.getId());
        verificar("UPDATE alterou o preço da cotação",
                depoisDoUpdate != null
                        && depoisDoUpdate.getPrice().compareTo(new BigDecimal("1150.00")) == 0);

        quoteDAO.deletar(antiga.getId());
        verificar("DELETE removeu a cotação antiga", quoteDAO.buscarPorId(antiga.getId()) == null);

        return recente;
    }

    private static void limparDadosDeTeste(CurrencyQuote cotacao, Transaction transacao, Asset ativo,
                                           Wallet carteira, User usuario, Currency moedaBase, Currency cripto) {
        secao("7. DELETE de todos os registros de teste");

        if (cotacao != null) {
            quoteDAO.deletar(cotacao.getId());
            verificar("Cotação removida de tb_currency_quote",
                    quoteDAO.buscarPorId(cotacao.getId()) == null);
        }

        if (carteira != null) {
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
