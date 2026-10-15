import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TransactionDAO {

    private final WalletDAO walletDAO = new WalletDAO();

    private static final String SELECT_BASE =
        "SELECT t.id AS tx_id, t.wallet_id, t.quantity, t.priceAtTime, " +
        "       t.TYPE AS tx_type, t.executedAt, " +
        "       c.id AS currency_id, c.symbol, c.name AS currency_name, c.isFiat " +
        "FROM tb_transaction t " +
        "JOIN tb_currency c ON t.currency_id = c.id ";

    public void inserir(Transaction t) {
        String sql = "INSERT INTO tb_transaction " +
                "(id, wallet_id, currency_id, quantity, priceAtTime, TYPE, executedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.getId().toString());
            stmt.setString(2, t.wallet.getId().toString());
            stmt.setString(3, t.currency.getId().toString());
            stmt.setBigDecimal(4, t.quantity);
            stmt.setBigDecimal(5, t.priceAtTime);
            stmt.setString(6, t.type.name());
            stmt.setTimestamp(7, Timestamp.valueOf(t.executedAt));

            stmt.executeUpdate();
            System.out.println("Transação inserida com sucesso: " + t.type + " " + t.currency.getSymbol());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir transação: " + e.getMessage());
        }
    }

    public Transaction buscarPorId(UUID id) {
        String sql = SELECT_BASE + "WHERE t.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID walletId = UUID.fromString(rs.getString("wallet_id"));
                    return mapear(rs, walletDAO.buscarPorId(walletId));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar transação: " + e.getMessage());
        }

        return null;
    }

    public List<Transaction> listarTodos() {
        List<Transaction> transacoes = new ArrayList<>();
        Map<UUID, Wallet> cacheCarteiras = new HashMap<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY t.executedAt");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID walletId = UUID.fromString(rs.getString("wallet_id"));

                Wallet wallet = cacheCarteiras.get(walletId);
                if (wallet == null) {
                    wallet = walletDAO.buscarPorId(walletId);
                    cacheCarteiras.put(walletId, wallet);
                }

                transacoes.add(mapear(rs, wallet));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar transações: " + e.getMessage());
        }

        return transacoes;
    }

    public List<Transaction> listarPorCarteira(Wallet wallet) {
        String sql = SELECT_BASE + "WHERE t.wallet_id = ? ORDER BY t.executedAt";
        List<Transaction> transacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, wallet.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transacoes.add(mapear(rs, wallet));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar transações da carteira: " + e.getMessage());
        }

        return transacoes;
    }

    public void atualizar(Transaction t) {
        String sql = "UPDATE tb_transaction " +
                "SET currency_id = ?, quantity = ?, priceAtTime = ?, TYPE = ?, executedAt = ? " +
                "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, t.currency.getId().toString());
            stmt.setBigDecimal(2, t.quantity);
            stmt.setBigDecimal(3, t.priceAtTime);
            stmt.setString(4, t.type.name());
            stmt.setTimestamp(5, Timestamp.valueOf(t.executedAt));
            stmt.setString(6, t.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Transação atualizada com sucesso: " + t.getId());
            } else {
                System.out.println("Nenhuma transação encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar transação: " + e.getMessage());
        }
    }

    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_transaction WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Transação removida com sucesso.");
            } else {
                System.out.println("Nenhuma transação encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar transação: " + e.getMessage());
        }
    }

    public void deletarPorCarteira(UUID walletId) {
        String sql = "DELETE FROM tb_transaction WHERE wallet_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, walletId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar transações da carteira: " + e.getMessage());
        }
    }

    private Transaction mapear(ResultSet rs, Wallet wallet) throws SQLException {
        Currency currency = new Currency(
                UUID.fromString(rs.getString("currency_id")),
                rs.getString("symbol"),
                rs.getString("currency_name"),
                rs.getInt("isFiat") == 1
        );

        return new Transaction(
                UUID.fromString(rs.getString("tx_id")),
                wallet,
                currency,
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("priceAtTime"),
                TransactionType.valueOf(rs.getString("tx_type")),
                rs.getTimestamp("executedAt").toLocalDateTime()
        );
    }
}