import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * DEMONSTRAÇÃO EM MEMÓRIA (Fase 5)
 * -----------------------------------------------------------------------------
 * Usa só objetos Java e Collections (ArrayList e HashMap), sem tocar no banco,
 * e no final grava três arquivos de texto na pasta do projeto: usuarios.txt,
 * moedas.txt e carteiras.txt.
 *
 * Este código ficava dentro da Main. Quando a Main virou o programa com menus,
 * ele foi movido para cá e continua acessível pelo menu Utilitários - assim a
 * entrega da Fase 5 não se perde.
 */
public final class DemonstracaoEmMemoria {

    private DemonstracaoEmMemoria() {
    }

    public static void executar() {
        try {
            // Usuários
            User user1 = new User("user1@gmail.com", "1234", "user1");
            User user2 = new User("user2@gmail.com", "1234", "user2");

            ArrayList<User> users = new ArrayList<>();
            users.add(user1);
            users.add(user2);

            System.out.println("Users: " + users);

            // Moedas
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

            // Carteiras
            HashMap<User, ArrayList<Wallet>> walletsByUser = new HashMap<>();

            ArrayList<Wallet> user1Wallets = new ArrayList<>();
            user1Wallets.add(new Wallet(user1, BRL));
            user1Wallets.add(new Wallet(user1, USD));
            walletsByUser.put(user1, user1Wallets);

            ArrayList<Wallet> user2Wallets = new ArrayList<>();
            user2Wallets.add(new Wallet(user2, BRL));
            user2Wallets.add(new Wallet(user2, USD));
            walletsByUser.put(user2, user2Wallets);

            // Operações
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
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }
}
