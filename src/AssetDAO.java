import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssetDAO {

    private static final String SELECT_BASE =
        "SELECT a.id AS asset_id, a.quantity, " +
        "       c.id AS currency_id, c.symbol, c.name AS currency_name, c.isFiat " +
        "FROM tb_asset a " +
        "JOIN tb_currency c ON a.currency_id = c.id ";

    public void inserir(Asset asset, UUID walletId) {
        String sql = "INSERT INTO tb_asset (id, wallet_id, currency_id, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getId().toString());
            stmt.setString(2, walletId.toString());
            stmt.setString(3, asset.getCurrency().getId().toString());
            stmt.setBigDecimal(4, asset.getQuantity());

            stmt.executeUpdate();
            System.out.println("Asset inserido com sucesso: " + asset.getCurrency().getSymbol());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir asset: " + e.getMessage());
        }
    }

    public Asset buscarPorId(UUID id) {
        String sql = SELECT_BASE + "WHERE a.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar asset: " + e.getMessage());
        }

        return null;
    }

    public List<Asset> listarTodos() {
        List<Asset> assets = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                assets.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar assets: " + e.getMessage());
        }

        return assets;
    }

    public List<Asset> listarPorCarteira(UUID walletId) {
        String sql = SELECT_BASE + "WHERE a.wallet_id = ?";
        List<Asset> assets = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, walletId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    assets.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar assets da carteira: " + e.getMessage());
        }

        return assets;
    }

    public void atualizar(Asset asset) {
        String sql = "UPDATE tb_asset SET currency_id = ?, quantity = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getCurrency().getId().toString());
            stmt.setBigDecimal(2, asset.getQuantity());
            stmt.setString(3, asset.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Asset atualizado com sucesso: " + asset.getId());
            } else {
                System.out.println("Nenhum asset encontrado com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar asset: " + e.getMessage());
        }
    }

    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_asset WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Asset removido com sucesso.");
            } else {
                System.out.println("Nenhum asset encontrado com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar asset: " + e.getMessage());
        }
    }

    public void deletarPorCarteira(UUID walletId) {
        String sql = "DELETE FROM tb_asset WHERE wallet_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, walletId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar assets da carteira: " + e.getMessage());
        }
    }

    private Asset mapear(ResultSet rs) throws SQLException {
        Currency currency = new Currency(
                UUID.fromString(rs.getString("currency_id")),
                rs.getString("symbol"),
                rs.getString("currency_name"),
                rs.getInt("isFiat") == 1
        );

        BigDecimal quantity = rs.getBigDecimal("quantity");

        return new Asset(UUID.fromString(rs.getString("asset_id")), currency, quantity);
    }
}