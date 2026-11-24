import java.util.List;
import java.util.function.Function;

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
