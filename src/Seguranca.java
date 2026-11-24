import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Seguranca {

    private Seguranca() {
    }

    public static String gerarHashSha256(String senha) {
        try {
            MessageDigest algoritmo = MessageDigest.getInstance("SHA-256");
            byte[] resumo = algoritmo.digest(senha.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexadecimal = new StringBuilder(resumo.length * 2);
            for (byte b : resumo) {
                hexadecimal.append(String.format("%02x", b));
            }

            return hexadecimal.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível nesta JVM.", e);
        }
    }
}
