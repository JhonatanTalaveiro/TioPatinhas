import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Transforma a senha digitada em um hash SHA-256.
 *
 * A coluna do banco se chama hashedPassword justamente porque ela nunca deve
 * guardar a senha pura: se o banco vazar, o atacante leva só os hashes. É o
 * mesmo formato (64 caracteres hexadecimais) já usado nos INSERTs do script
 * sql/dml_tables.sql.
 *
 * Observação honesta: SHA-256 puro, sem "salt", é suficiente para o trabalho,
 * mas em um sistema real o certo seria um algoritmo feito para senhas, como
 * bcrypt ou Argon2.
 */
public final class Seguranca {

    private Seguranca() {
    }

    public static String gerarHashSha256(String senha) {
        try {
            MessageDigest algoritmo = MessageDigest.getInstance("SHA-256");
            byte[] resumo = algoritmo.digest(senha.getBytes(StandardCharsets.UTF_8));

            // Cada byte vira dois caracteres hexadecimais (ex.: 255 vira "ff").
            StringBuilder hexadecimal = new StringBuilder(resumo.length * 2);
            for (byte b : resumo) {
                hexadecimal.append(String.format("%02x", b));
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 existe em qualquer JVM, então isto não deve acontecer.
            throw new IllegalStateException("Algoritmo SHA-256 indisponível nesta JVM.", e);
        }
    }
}
