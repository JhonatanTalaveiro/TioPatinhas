import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletDAO {

    private static final String SELECT_BASE =
        "SELECT w.id AS wallet_id, " +
        "       u.id AS user_id, u.email, u.hashedPassword, u.name AS user_name, " +
        "       c.id AS currency_id, c.symbol, c.name AS currency_name, c.isFiat " +
        "FROM tb_wallet w " +
        "JOIN tb_user u ON w.owner_id = u.id " +
        "JOIN tb_currency c ON w.baseCurrency_id = c.id ";

    public void inserir(Wallet wallet) {
        String sql = "INSERT INTO tb_wallet (id, owner_id, baseCurrency_id) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, wallet.getId().toString());
            stmt.setString(2, wallet.getOwner().getId().toString());
            stmt.setString(3, wallet.getBaseCurrency().getId().toString());

            stmt.executeUpdate();
            System.out.println("Carteira inserida com sucesso: " + wallet.getId());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir carteira: " + e.getMessage());
        }
    }

    public Wallet buscarPorId(UUID id) {
        String sql = SELECT_BASE + "WHERE w.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar carteira: " + e.getMessage());
        }

        return null;
    }

    public List<Wallet> listarTodos() {
        List<Wallet> carteiras = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                carteiras.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar carteiras: " + e.getMessage());
        }

        return carteiras;
    }

    public List<Wallet> listarPorUsuario(UUID ownerId) {
        String sql = SELECT_BASE + "WHERE w.owner_id = ?";
        List<Wallet> carteiras = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ownerId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    carteiras.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar carteiras do usuário: " + e.getMessage());
        }

        return carteiras;
    }

    public void atualizar(Wallet wallet) {
        String sql = "UPDATE tb_wallet SET owner_id = ?, baseCurrency_id = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, wallet.getOwner().getId().toString());
            stmt.setString(2, wallet.getBaseCurrency().getId().toString());
            stmt.setString(3, wallet.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Carteira atualizada com sucesso: " + wallet.getId());
            } else {
                System.out.println("Nenhuma carteira encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar carteira: " + e.getMessage());
        }
    }

    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_wallet WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Carteira removida com sucesso.");
            } else {
                System.out.println("Nenhuma carteira encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar carteira: " + e.getMessage());
        }
    }

    private Wallet mapear(ResultSet rs) throws SQLException {
        User owner = new User(
                UUID.fromString(rs.getString("user_id")),
                rs.getString("email"),
                rs.getString("hashedPassword"),
                rs.getString("user_name")
        );

        Currency baseCurrency = new Currency(
                UUID.fromString(rs.getString("currency_id")),
                rs.getString("symbol"),
                rs.getString("currency_name"),
                rs.getInt("isFiat") == 1
        );

        return new Wallet(
                UUID.fromString(rs.getString("wallet_id")),
                owner,
                baseCurrency,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }
}