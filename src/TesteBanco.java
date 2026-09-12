import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TesteBanco {

    public static void main(String[] args) {

        try {
            Connection conexao = ConnectionFactory.getConnection();

            System.out.println("Conectado ao Oracle!");

            Statement stm = conexao.createStatement();

            ResultSet resultado = stm.executeQuery(
                    "SELECT table_name FROM user_tables"
            );

            System.out.println("Tabelas do usuário:");

            while (resultado.next()) {
                System.out.println(resultado.getString("table_name"));
            }

            resultado.close();
            stm.close();
            conexao.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}