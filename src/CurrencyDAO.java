import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CurrencyDAO {

    private static final String SELECT_BASE =
        "SELECT id, symbol, name, isFiat FROM tb_currency ";

    // CREATE - insere uma nova moeda no banco
    public void inserir(Currency currency) {
        String sql = "INSERT INTO tb_currency (id, symbol, name, isFiat) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currency.getId().toString());
            stmt.setString(2, currency.getSymbol());
            stmt.setString(3, currency.getName());
            // O Oracle não tem BOOLEAN em coluna: o DDL usa NUMBER(1) com CHECK (isFiat IN (0, 1)).
            stmt.setInt(4, currency.isFiat() ? 1 : 0);

            stmt.executeUpdate();
            System.out.println("Moeda inserida com sucesso: " + currency.getSymbol());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir moeda: " + e.getMessage());
        }
    }

    // READ - busca uma moeda pelo id
    public Currency buscarPorId(UUID id) {
        String sql = SELECT_BASE + "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar moeda: " + e.getMessage());
        }

        return null;
    }

    // READ - busca uma moeda pelo símbolo (BRL, BTC, ...), que é UNIQUE na tabela
    public Currency buscarPorSimbolo(String symbol) {
        String sql = SELECT_BASE + "WHERE symbol = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, symbol);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar moeda pelo símbolo: " + e.getMessage());
        }

        return null;
    }

    // READ - lista todas as moedas
    public List<Currency> listarTodos() {
        List<Currency> moedas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY symbol");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                moedas.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar moedas: " + e.getMessage());
        }

        return moedas;
    }

    // READ - lista só as fiduciárias (isFiat = 1) ou só as criptomoedas (isFiat = 0)
    public List<Currency> listarPorTipo(boolean isFiat) {
        String sql = SELECT_BASE + "WHERE isFiat = ? ORDER BY symbol";
        List<Currency> moedas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, isFiat ? 1 : 0);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    moedas.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar moedas por tipo: " + e.getMessage());
        }

        return moedas;
    }

    // UPDATE - atualiza os dados de uma moeda existente.
    // Os atributos de Currency são final (imutáveis), então o chamador monta um
    // novo objeto com o MESMO id e os valores já alterados.
    public void atualizar(Currency currency) {
        String sql = "UPDATE tb_currency SET symbol = ?, name = ?, isFiat = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currency.getSymbol());
            stmt.setString(2, currency.getName());
            stmt.setInt(3, currency.isFiat() ? 1 : 0);
            stmt.setString(4, currency.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Moeda atualizada com sucesso: " + currency.getSymbol());
            } else {
                System.out.println("Nenhuma moeda encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar moeda: " + e.getMessage());
        }
    }

    // DELETE - remove uma moeda pelo id.
    // Só funciona se nenhuma carteira, ativo, transação ou cotação ainda apontar
    // para ela; caso contrário o Oracle devolve ORA-02292 (child record found).
    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_currency WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Moeda removida com sucesso.");
            } else {
                System.out.println("Nenhuma moeda encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar moeda: " + e.getMessage());
        }
    }

    private Currency mapear(ResultSet rs) throws SQLException {
        return new Currency(
                UUID.fromString(rs.getString("id")),
                rs.getString("symbol"),
                rs.getString("name"),
                rs.getInt("isFiat") == 1
        );
    }
}
