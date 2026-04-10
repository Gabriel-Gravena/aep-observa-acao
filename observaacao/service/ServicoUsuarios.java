package observaacao.service;

import java.util.Optional;
import observaacao.model.ContaAcesso;
import observaacao.model.Usuario;
import observaacao.model.enums.TipoUsuario;
import observaacao.repository.RepositorioUsuarios;

public class ServicoUsuarios {

    private final RepositorioUsuarios repositorioUsuarios;

    public ServicoUsuarios(RepositorioUsuarios repositorioUsuarios) {
        if (repositorioUsuarios == null) {
            throw new IllegalArgumentException(
                "O repositório de usuários é obrigatório."
            );
        }
        this.repositorioUsuarios = repositorioUsuarios;
    }

    public Usuario cadastrarUsuario(
        String nome,
        String contato,
        String login,
        String senha,
        TipoUsuario tipoUsuario
    ) {
        validarTipoCadastro(tipoUsuario);
        validarLoginDisponivel(login);
        Usuario usuario = new Usuario(nome, contato, tipoUsuario, false);
        ContaAcesso contaAcesso = new ContaAcesso(usuario, login, senha);
        repositorioUsuarios.salvar(contaAcesso);
        return usuario;
    }

    public Usuario autenticar(String login, String senha) {
        ContaAcesso contaAcesso = obterContaObrigatoria(login);
        if (contaAcesso.autenticar(senha)) {
            return contaAcesso.getUsuario();
        }
        throw new IllegalArgumentException("Senha inválida.");
    }

    public boolean isServidor(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.getTipo() == TipoUsuario.SERVIDOR;
    }

    public Optional<Usuario> buscarUsuarioPorLogin(String login) {
        Optional<ContaAcesso> contaAcesso = repositorioUsuarios.buscarPorLogin(
            login
        );
        if (contaAcesso.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(contaAcesso.get().getUsuario());
    }

    private ContaAcesso obterContaObrigatoria(String login) {
        Optional<ContaAcesso> contaAcesso = repositorioUsuarios.buscarPorLogin(
            login
        );
        if (contaAcesso.isPresent()) {
            return contaAcesso.get();
        }
        throw new IllegalArgumentException("Login não encontrado.");
    }

    private void validarLoginDisponivel(String login) {
        if (repositorioUsuarios.existeLogin(login)) {
            throw new IllegalArgumentException("Já existe um usuário com esse login.");
        }
    }

    private void validarTipoCadastro(TipoUsuario tipoUsuario) {
        if (
            tipoUsuario != TipoUsuario.CIDADAO &&
            tipoUsuario != TipoUsuario.SERVIDOR
        ) {
            throw new IllegalArgumentException(
                "O cadastro permite apenas cidadão ou servidor."
            );
        }
    }
}
