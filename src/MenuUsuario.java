import java.util.List;

public final class MenuUsuario {

    private static final UserDAO userDAO = new UserDAO();
    private static final WalletDAO walletDAO = new WalletDAO();

    private MenuUsuario() {
    }

    public static void exibir() {
        int opcao;

        do {
            Formatador.titulo("MÓDULO DE USUÁRIOS");
            System.out.println("  1 - Incluir usuário");
            System.out.println("  2 - Alterar usuário");
            System.out.println("  3 - Excluir usuário");
            System.out.println("  4 - Exibir todos os usuários");
            System.out.println("  5 - Exibir um único usuário");
            System.out.println("  6 - Exibir as carteiras de um usuário");
            System.out.println("  0 - Voltar ao menu principal");
            System.out.println();

            opcao = Entrada.lerInteiro("Escolha uma opção", 0, 6);

            switch (opcao) {
                case 1 -> incluir();
                case 2 -> alterar();
                case 3 -> excluir();
                case 4 -> exibirTodos();
                case 5 -> exibirUm();
                case 6 -> exibirCarteirasDoUsuario();
                default -> System.out.println("Voltando ao menu principal...");
            }

            if (opcao != 0) {
                Entrada.pausar();
            }

        } while (opcao != 0);
    }

    private static void incluir() {
        Formatador.subtitulo("Inclusão de usuário");

        String nome = Entrada.lerTextoObrigatorio("Nome completo", Validador.TAMANHO_MAXIMO_TEXTO);
        String email = Entrada.lerEmail("E-mail");

        if (userDAO.buscarPorEmail(email) != null) {
            System.out.println("Já existe um usuário cadastrado com o e-mail " + email + ".");
            return;
        }

        String senha = Entrada.lerSenha("Senha (mínimo " + Validador.TAMANHO_MINIMO_SENHA + " caracteres)");

        User usuario = new User(email, Seguranca.gerarHashSha256(senha), nome);

        if (userDAO.inserir(usuario)) {
            System.out.println("Id gerado: " + usuario.getId());
        }
    }

    private static void alterar() {
        Formatador.subtitulo("Alteração de usuário");

        User usuario = selecionar("Escolha o usuário que deseja alterar:");
        if (usuario == null) {
            return;
        }

        System.out.println();
        System.out.println("Dados atuais:");
        System.out.println(Formatador.detalhesUsuario(usuario));
        System.out.println();
        System.out.println("Pressione ENTER para manter o valor atual de cada campo.");

        String nome = Entrada.lerTextoOpcional("Nome", usuario.getName(), Validador.TAMANHO_MAXIMO_TEXTO);
        String email = Entrada.lerEmail("E-mail", usuario.getEmail());

        if (!email.equalsIgnoreCase(usuario.getEmail())) {
            User dono = userDAO.buscarPorEmail(email);
            if (dono != null && !dono.getId().equals(usuario.getId())) {
                System.out.println("Já existe outro usuário com o e-mail " + email + ". Alteração cancelada.");
                return;
            }
        }

        usuario.setName(nome);
        usuario.setEmail(email);

        if (Entrada.confirmar("Deseja trocar a senha")) {
            String senha = Entrada.lerSenha("Nova senha (mínimo "
                    + Validador.TAMANHO_MINIMO_SENHA + " caracteres)");
            usuario.setHashedPassword(Seguranca.gerarHashSha256(senha));
        }

        userDAO.atualizar(usuario);
    }

    private static void excluir() {
        Formatador.subtitulo("Exclusão de usuário");

        User usuario = selecionar("Escolha o usuário que deseja excluir:");
        if (usuario == null) {
            return;
        }

        List<Wallet> carteiras = walletDAO.listarPorUsuario(usuario.getId());
        if (!carteiras.isEmpty()) {
            System.out.println();
            System.out.println("Não é possível excluir: este usuário ainda tem "
                    + carteiras.size() + " carteira(s) cadastrada(s):");
            for (Wallet carteira : carteiras) {
                System.out.println("   - " + Formatador.resumoCarteira(carteira));
            }
            System.out.println("Exclua as carteiras primeiro, no módulo de carteiras.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesUsuario(usuario));
        System.out.println();

        if (Entrada.confirmar("Confirma a exclusão deste usuário")) {
            userDAO.deletar(usuario.getId());
        } else {
            System.out.println("Exclusão cancelada.");
        }
    }

    private static void exibirTodos() {
        Formatador.subtitulo("Usuários cadastrados");

        List<User> usuarios = userDAO.listarTodos();

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuário cadastrado no banco.");
            return;
        }

        for (User usuario : usuarios) {
            System.out.println("  " + Formatador.resumoUsuario(usuario));
        }

        System.out.println();
        System.out.println("Total: " + usuarios.size() + " usuário(s).");
    }

    private static void exibirUm() {
        Formatador.subtitulo("Consulta de um usuário");

        User escolhido = selecionar("Escolha o usuário que deseja consultar:");
        if (escolhido == null) {
            return;
        }

        User usuario = userDAO.buscarPorId(escolhido.getId());

        if (usuario == null) {
            System.out.println("Usuário não encontrado: ele pode ter sido excluído por outra pessoa.");
            return;
        }

        System.out.println();
        System.out.println(Formatador.detalhesUsuario(usuario));
    }

    private static void exibirCarteirasDoUsuario() {
        Formatador.subtitulo("Carteiras de um usuário");

        User usuario = selecionar("Escolha o usuário:");
        if (usuario == null) {
            return;
        }

        List<Wallet> carteiras = walletDAO.listarPorUsuario(usuario.getId());

        System.out.println();
        System.out.println("Usuário: " + Formatador.resumoUsuario(usuario));

        if (carteiras.isEmpty()) {
            System.out.println("Este usuário ainda não tem carteiras.");
            return;
        }

        for (Wallet carteira : carteiras) {
            System.out.println("   - " + Formatador.resumoCarteira(carteira));
        }

        System.out.println();
        System.out.println("Total: " + carteiras.size() + " carteira(s).");
    }

    private static User selecionar(String titulo) {
        return Seletor.escolher(userDAO.listarTodos(), titulo, Formatador::resumoUsuario);
    }
}
