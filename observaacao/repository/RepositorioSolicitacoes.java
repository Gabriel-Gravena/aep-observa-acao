package observaacao.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import observaacao.model.Categoria;
import observaacao.model.Solicitacao;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;

public class RepositorioSolicitacoes {

    private final List<Solicitacao> solicitacoes;
    private final Map<String, Solicitacao> solicitacoesPorProtocolo;

    public RepositorioSolicitacoes() {
        this.solicitacoes = new ArrayList<>();
        this.solicitacoesPorProtocolo = new LinkedHashMap<>();
    }

    public void salvar(Solicitacao solicitacao) {
        validarSolicitacao(solicitacao);
        removerSeExistir(solicitacao.getProtocolo());
        solicitacoes.add(solicitacao);
        solicitacoesPorProtocolo.put(solicitacao.getProtocolo(), solicitacao);
    }

    public Optional<Solicitacao> buscarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(
            solicitacoesPorProtocolo.get(protocolo.trim())
        );
    }

    public List<Solicitacao> listarTodas() {
        return new ArrayList<>(solicitacoes);
    }

    public List<Solicitacao> listarPorStatus(StatusSolicitacao status) {
        List<Solicitacao> resultado = new ArrayList<Solicitacao>();
        if (status == null) {
            return resultado;
        }

        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.getStatus() == status) {
                resultado.add(solicitacao);
            }
        }

        return resultado;
    }

    public List<Solicitacao> listarPorCategoria(Categoria categoria) {
        List<Solicitacao> resultado = new ArrayList<Solicitacao>();
        if (categoria == null) {
            return resultado;
        }

        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.getCategoria().equals(categoria)) {
                resultado.add(solicitacao);
            }
        }

        return resultado;
    }

    public List<Solicitacao> listarPorBairro(String bairro) {
        List<Solicitacao> resultado = new ArrayList<Solicitacao>();
        if (bairro == null || bairro.trim().isEmpty()) {
            return listarTodas();
        }

        String bairroNormalizado = bairro.trim().toLowerCase();
        for (Solicitacao solicitacao : solicitacoes) {
            if (contemBairro(solicitacao, bairroNormalizado)) {
                resultado.add(solicitacao);
            }
        }

        return resultado;
    }

    public List<Solicitacao> listarAtrasadas() {
        List<Solicitacao> resultado = new ArrayList<Solicitacao>();

        for (Solicitacao solicitacao : solicitacoes) {
            if (
                !solicitacao.estaNoPrazo() &&
                solicitacao.getStatus() != StatusSolicitacao.ENCERRADO
            ) {
                resultado.add(solicitacao);
            }
        }

        return resultado;
    }

    public List<Solicitacao> listarPorPrioridadeEBairro(
        String bairro,
        Prioridade prioridade
    ) {
        List<Solicitacao> resultado = new ArrayList<Solicitacao>();
        if (prioridade == null) {
            return resultado;
        }

        List<Solicitacao> solicitacoesFiltradas = listarPorBairro(bairro);
        for (Solicitacao solicitacao : solicitacoesFiltradas) {
            if (solicitacao.getPrioridade() == prioridade) {
                resultado.add(solicitacao);
            }
        }

        return resultado;
    }

    private void validarSolicitacao(Solicitacao solicitacao) {
        if (solicitacao == null) {
            throw new IllegalArgumentException(
                "A solicitação não pode ser nula."
            );
        }
    }

    private void removerSeExistir(String protocolo) {
        Solicitacao existente = solicitacoesPorProtocolo.remove(protocolo);
        if (existente == null) {
            return;
        }
        solicitacoes.remove(existente);
    }

    private boolean contemBairro(
        Solicitacao solicitacao,
        String bairroNormalizado
    ) {
        return solicitacao
            .getLocalizacao()
            .toLowerCase()
            .contains(bairroNormalizado);
    }
}
