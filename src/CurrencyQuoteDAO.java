import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CurrencyQuoteDAO {

    private static final String SELECT_BASE =
        "SELECT q.id AS quote_id, q.price, q.quote_timestamp, " +
        "       c.id AS currency_id, c.symbol AS currency_symbol, " +
        "       c.name AS currency_name, c.isFiat AS currency_isFiat, " +
        "       b.id AS base_id, b.symbol AS base_symbol, " +
        "       b.name AS base_name, b.isFiat AS base_isFiat " +
        "FROM tb_currency_quote q " +
        "JOIN tb_currency c ON q.currency_id = c.id " +
        "JOIN tb_currency b ON q.baseCurrency_id = b.id ";

    public void inserir(CurrencyQuote quote) {
        String sql = "INSERT INTO tb_currency_quote " +
                "(id, currency_id, baseCurrency_id, price, quote_timestamp) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, quote.getId().toString());
            stmt.setString(2, quote.getCurrency().getId().toString());
            stmt.setString(3, quote.getBaseCurrency().getId().toString());
            stmt.setBigDecimal(4, quote.getPrice());
            stmt.setTimestamp(5, Timestamp.valueOf(quote.timestamp));

            stmt.executeUpdate();
            System.out.println("Cotação inserida com sucesso: " + quote.getCurrency().getSymbol()
                    + "/" + quote.getBaseCurrency().getSymbol());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir cotação: " + e.getMessage());
        }
    }

    public CurrencyQuote buscarPorId(UUID id) {
        String sql = SELECT_BASE + "WHERE q.id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar cotação: " + e.getMessage());
        }

        return null;
    }

    public List<CurrencyQuote> listarTodos() {
        List<CurrencyQuote> cotacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY q.quote_timestamp");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cotacoes.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar cotações: " + e.getMessage());
        }

        return cotacoes;
    }

    public List<CurrencyQuote> listarPorMoeda(UUID currencyId) {
        String sql = SELECT_BASE + "WHERE q.currency_id = ? ORDER BY q.quote_timestamp";
        List<CurrencyQuote> cotacoes = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cotacoes.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar cotações da moeda: " + e.getMessage());
        }

        return cotacoes;
    }

    public CurrencyQuote buscarMaisRecente(UUID currencyId, UUID baseCurrencyId) {
        String sql = "SELECT * FROM (" + SELECT_BASE +
                "WHERE q.currency_id = ? AND q.baseCurrency_id = ? " +
                "ORDER BY q.quote_timestamp DESC) WHERE ROWNUM = 1";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId.toString());
            stmt.setString(2, baseCurrencyId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar cotação mais recente: " + e.getMessage());
        }

        return null;
    }

    public void atualizar(CurrencyQuote quote) {
        String sql = "UPDATE tb_currency_quote " +
                "SET currency_id = ?, baseCurrency_id = ?, price = ?, quote_timestamp = ? " +
                "WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, quote.getCurrency().getId().toString());
            stmt.setString(2, quote.getBaseCurrency().getId().toString());
            stmt.setBigDecimal(3, quote.getPrice());
            stmt.setTimestamp(4, Timestamp.valueOf(quote.timestamp));
            stmt.setString(5, quote.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Cotação atualizada com sucesso: " + quote.getId());
            } else {
                System.out.println("Nenhuma cotação encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar cotação: " + e.getMessage());
        }
    }

    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_currency_quote WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Cotação removida com sucesso.");
            } else {
                System.out.println("Nenhuma cotação encontrada com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cotação: " + e.getMessage());
        }
    }

    public void deletarPorMoeda(UUID currencyId) {
        String sql = "DELETE FROM tb_currency_quote WHERE currency_id = ? OR baseCurrency_id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currencyId.toString());
            stmt.setString(2, currencyId.toString());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao deletar cotações da moeda: " + e.getMessage());
        }
    }

    private CurrencyQuote mapear(ResultSet rs) throws SQLException {
        Currency currency = new Currency(
                UUID.fromString(rs.getString("currency_id")),
                rs.getString("currency_symbol"),
                rs.getString("currency_name"),
                rs.getInt("currency_isFiat") == 1
        );

        Currency baseCurrency = new Currency(
                UUID.fromString(rs.getString("base_id")),
                rs.getString("base_symbol"),
                rs.getString("base_name"),
                rs.getInt("base_isFiat") == 1
        );

        BigDecimal price = rs.getBigDecimal("price");

        return new CurrencyQuote(
                UUID.fromString(rs.getString("quote_id")),
                currency,
                baseCurrency,
                price,
                rs.getTimestamp("quote_timestamp").toLocalDateTime()
        );
    }
}