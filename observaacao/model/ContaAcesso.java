package observaacao.model;

public class ContaAcesso {

    private static final int TAMANHO_MINIMO_SENHA = 4;

    private final Usuario usuario;
    private final String login;
    private final String senha;

    public ContaAcesso(Usuario usuario, String login, String senha) {
        validarUsuario(usuario);
        this.usuario = usuario;
        this.login = normalizarLogin(login);
        this.senha = validarSenha(senha);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }
    }

    private String normalizarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("O login é obrigatório.");
        }
        return login.trim().toLowerCase();
    }

    private String validarSenha(String senha) {
        if (senha == null || senha.trim().length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                "A senha deve ter no mínimo 4 caracteres."
            );
        }
        return senha.trim();
    }

    public boolean autenticar(String senhaInformada) {
        if (senhaInformada == null) {
            return false;
        }
        return senha.equals(senhaInformada.trim());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getLogin() {
        return login;
    }
}
