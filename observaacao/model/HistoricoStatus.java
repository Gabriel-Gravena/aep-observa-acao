package observaacao.model;

import java.time.LocalDateTime;
import observaacao.model.enums.StatusSolicitacao;

public class HistoricoStatus {

    private static final String RESPONSAVEL_PADRAO = "SISTEMA";

    private final StatusSolicitacao statusAnterior;
    private final StatusSolicitacao statusNovo;
    private final LocalDateTime dataMovimentacao;
    private final String responsavel;
    private final String comentario;

    public HistoricoStatus(
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String responsavel,
        String comentario
    ) {
        this(
            statusAnterior,
            statusNovo,
            LocalDateTime.now(),
            responsavel,
            comentario
        );
    }

    public HistoricoStatus(
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        LocalDateTime dataMovimentacao,
        String responsavel,
        String comentario
    ) {
        validarComentario(comentario);
        validarStatusNovo(statusNovo);
        validarDataMovimentacao(dataMovimentacao);
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.dataMovimentacao = dataMovimentacao;
        this.responsavel = normalizarResponsavel(responsavel);
        this.comentario = comentario.trim();
    }

    private void validarComentario(String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) {
            throw new IllegalArgumentException("Comentário é obrigatório.");
        }
    }

    private void validarStatusNovo(StatusSolicitacao statusNovo) {
        if (statusNovo == null) {
            throw new IllegalArgumentException("O novo status é obrigatório.");
        }
    }

    private void validarDataMovimentacao(LocalDateTime dataMovimentacao) {
        if (dataMovimentacao == null) {
            throw new IllegalArgumentException(
                "A data da movimentação é obrigatória."
            );
        }
    }

    private String normalizarResponsavel(String responsavel) {
        if (responsavel == null || responsavel.trim().isEmpty()) {
            return RESPONSAVEL_PADRAO;
        }
        return responsavel.trim();
    }

    public StatusSolicitacao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSolicitacao getStatusNovo() {
        return statusNovo;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getComentario() {
        return comentario;
    }
}
