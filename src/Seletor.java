import java.util.List;
import java.util.function.Function;

/**
 * Mostra uma lista numerada e devolve o item escolhido.
 *
 * Os ids do sistema são UUIDs de 36 caracteres. Obrigar o usuário a digitar
 * "30000000-0000-0000-0000-000000000001" para alterar uma carteira seria um
 * convite ao erro, então toda alteração, exclusão ou consulta de um registro
 * lista o que existe no banco e pede só o número da linha.
 *
 * O parâmetro "rotulo" é a função que sabe transformar cada objeto no texto que
 * aparece na tela (ver a classe Formatador).
 *
 * Devolve null quando a lista está vazia ou quando o usuário cancela.
 */
public final class Seletor {

    private Seletor() {
    }

    public static <T> T escolher(List<T> itens, String titulo, Function<T, String> rotulo) {
        if (itens == null || itens.isEmpty()) {
            System.out.println("Nenhum registro encontrado para essa operação.");
            return null;
        }

        System.out.println();
        System.out.println(titulo);

        for (int i = 0; i < itens.size(); i++) {
            System.out.println("  " + (i + 1) + " - " + rotulo.apply(itens.get(i)));
        }
        System.out.println("  0 - Cancelar");

        int escolha = Entrada.lerInteiro("Digite o número desejado", 0, itens.size());

        return escolha == 0 ? null : itens.get(escolha - 1);
    }
}
