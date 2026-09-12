import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.FileWriter;


public class Main{
    // DAO usado para testar a integração com o banco de dados (Fase 5)
    private static final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) {
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

        // FASE 5 - TESTES DE INTEGRAÇÃO COM O BANCO DE DADOS (tabela tb_user)

        System.out.println("\n===== TESTE DE INTEGRAÇÃO COM BANCO DE DADOS - TB_USER =====\n");

        UUID idTeste = testarInsercao();
        testarListagem();
        testarBuscaPorId(idTeste);
        testarAtualizacao(idTeste);
        testarRemocao(idTeste);
        testarListagem();

        System.out.println("\n===== FIM DOS TESTES DE BANCO =====");
    }

    // Testa o INSERT (CREATE) e retorna o id gerado para os próximos testes
    private static UUID testarInsercao() {
        System.out.println(">> Testando INSERÇÃO...");
        User novoUser = new User("teste@fiap.com.br", "senha123hash", "Usuário de Teste");
        userDAO.inserir(novoUser);
        System.out.println();
        return novoUser.getId();
    }

    // Testa o SELECT de todos (READ)
    private static void testarListagem() {
        System.out.println(">> Testando LISTAGEM de todos os usuários...");
        List<User> usuarios = userDAO.listarTodos();
        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado.");
        } else {
            for (User u : usuarios) {
                System.out.println(u);
            }
        }
        System.out.println();
    }

    // Testa o SELECT por id (READ)
    private static void testarBuscaPorId(UUID id) {
        System.out.println(">> Testando BUSCA por id...");
        User user = userDAO.buscarPorId(id);
        if (user != null) {
            System.out.println("Encontrado: " + user);
        } else {
            System.out.println("Usuário não encontrado.");
        }
        System.out.println();
    }

    // Testa o UPDATE
    private static void testarAtualizacao(UUID id) {
        System.out.println(">> Testando ATUALIZAÇÃO...");
        User user = userDAO.buscarPorId(id);
        if (user != null) {
            user.setName("Usuário Atualizado");
            user.setEmail("atualizado@fiap.com.br");
            userDAO.atualizar(user);
        } else {
            System.out.println("Usuário não encontrado para atualizar.");
        }
        System.out.println();
    }

    // Testa o DELETE
    private static void testarRemocao(UUID id) {
        System.out.println(">> Testando REMOÇÃO...");
        userDAO.deletar(id);
        System.out.println();
    }
}