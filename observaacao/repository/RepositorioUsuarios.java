package observaacao.repository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import observaacao.model.ContaAcesso;

public class RepositorioUsuarios {

    private final Map<String, ContaAcesso> contasPorLogin;

    public RepositorioUsuarios() {
        this.contasPorLogin = new LinkedHashMap<String, ContaAcesso>();
    }

    public void salvar(ContaAcesso contaAcesso) {
        validarConta(contaAcesso);
        contasPorLogin.put(contaAcesso.getLogin(), contaAcesso);
    }

    public Optional<ContaAcesso> buscarPorLogin(String login) {
        String loginNormalizado = normalizarLogin(login);
        if (loginNormalizado == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(contasPorLogin.get(loginNormalizado));
    }

    public boolean existeLogin(String login) {
        return buscarPorLogin(login).isPresent();
    }

    private void validarConta(ContaAcesso contaAcesso) {
        if (contaAcesso == null) {
            throw new IllegalArgumentException("A conta de acesso é obrigatória.");
        }
    }

    private String normalizarLogin(String login) {
        if (login == null || login.isBlank()) {
            return null;
        }
        return login.trim().toLowerCase();
    }
}
