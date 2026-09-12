import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDAO {

    // CREATE - insere um novo usuário no banco
    public void inserir(User user) {
        String sql = "INSERT INTO tb_user (id, email, hashedPassword, name) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId().toString());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getHashedPassword());
            stmt.setString(4, user.getName());

            stmt.executeUpdate();
            System.out.println("Usuário inserido com sucesso: " + user.getName());

        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário: " + e.getMessage());
        }
    }

    // READ - busca um usuário pelo id
    public User buscarPorId(UUID id) {
        String sql = "SELECT id, email, hashedPassword, name FROM tb_user WHERE id = ?";
        User user = null;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            UUID.fromString(rs.getString("id")),
                            rs.getString("email"),
                            rs.getString("hashedPassword"),
                            rs.getString("name")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar usuário: " + e.getMessage());
        }

        return user;
    }

    // READ - lista todos os usuários
    public List<User> listarTodos() {
        String sql = "SELECT id, email, hashedPassword, name FROM tb_user";
        List<User> usuarios = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getString("hashedPassword"),
                        rs.getString("name")
                );
                usuarios.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }

        return usuarios;
    }

    // UPDATE - atualiza os dados de um usuário existente
    public void atualizar(User user) {
        String sql = "UPDATE tb_user SET email = ?, hashedPassword = ?, name = ? WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getHashedPassword());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getId().toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Usuário atualizado com sucesso: " + user.getName());
            } else {
                System.out.println("Nenhum usuário encontrado com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    // DELETE - remove um usuário pelo id
    public void deletar(UUID id) {
        String sql = "DELETE FROM tb_user WHERE id = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id.toString());

            int linhas = stmt.executeUpdate();
            if (linhas > 0) {
                System.out.println("Usuário removido com sucesso.");
            } else {
                System.out.println("Nenhum usuário encontrado com esse id.");
            }

        } catch (SQLException e) {
            System.out.println("Erro ao deletar usuário: " + e.getMessage());
        }
    }
}