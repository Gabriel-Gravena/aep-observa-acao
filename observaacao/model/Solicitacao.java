package observaacao.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;

public class Solicitacao {

    private static final int TAMANHO_MINIMO_DESCRICAO = 20;
    private static final int TAMANHO_MINIMO_LOCALIZACAO = 5;

    private final String protocolo;
    private final Usuario solicitante;
    private final Categoria categoria;
    private final String descricao;
    private final String localizacao;
    private final Prioridade prioridade;
    private StatusSolicitacao status;
    private final LocalDateTime dataAbertura;
    private final LocalDate prazoAlvo;
    private final List<HistoricoStatus> historico;
    private final boolean anonimo;

    public Solicitacao(
        String protocolo,
        Usuario solicitante,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        boolean anonimo
    ) {
        this(
            protocolo,
            solicitante,
            categoria,
            descricao,
            localizacao,
            prioridade,
            anonimo,
            LocalDateTime.now()
        );
    }

    public Solicitacao(
        String protocolo,
        Usuario solicitante,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        boolean anonimo,
        LocalDateTime dataAbertura
    ) {
        validarProtocolo(protocolo);
        validarSolicitante(solicitante);
        validarCategoria(categoria);
        validarDescricao(descricao);
        validarLocalizacao(localizacao);
        validarPrioridade(prioridade);
        validarDataAbertura(dataAbertura);

        this.protocolo = protocolo.trim();
        this.solicitante = solicitante;
        this.categoria = categoria;
        this.descricao = descricao.trim();
        this.localizacao = localizacao.trim();
        this.prioridade = prioridade;
        this.status = StatusSolicitacao.ABERTO;
        this.dataAbertura = dataAbertura;
        this.prazoAlvo = prioridade.calcularPrazo(dataAbertura.toLocalDate());
        this.historico = new ArrayList<>();
        this.anonimo = deveForcarAnonimato(categoria) || anonimo;
    }

    public void adicionarHistorico(HistoricoStatus historicoStatus) {
        if (historicoStatus == null) {
            throw new IllegalArgumentException(
                "O histórico não pode ser nulo."
            );
        }
        historico.add(historicoStatus);
        this.status = historicoStatus.getStatusNovo();
    }

    public boolean estaNoPrazo() {
        return !LocalDate.now().isAfter(prazoAlvo);
    }

    public String resumo() {
        return String.format(
            "Protocolo: %s | Status: %s | Prazo: %s | Categoria: %s",
            protocolo,
            status,
            formatarPrazo(),
            categoria.getNome()
        );
    }

    public String getProtocolo() {
        return protocolo;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public LocalDate getPrazoAlvo() {
        return prazoAlvo;
    }

    public List<HistoricoStatus> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    public void atualizarStatus(StatusSolicitacao novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException(
                "O novo status não pode ser nulo."
            );
        }
        this.status = novoStatus;
    }

    private String formatarPrazo() {
        return String.format(
            "%02d/%02d/%04d",
            prazoAlvo.getDayOfMonth(),
            prazoAlvo.getMonthValue(),
            prazoAlvo.getYear()
        );
    }

    private void validarProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            throw new IllegalArgumentException("O protocolo é obrigatório.");
        }
    }

    private void validarSolicitante(Usuario solicitante) {
        if (solicitante == null) {
            throw new IllegalArgumentException("O solicitante é obrigatório.");
        }
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }
    }

    private void validarDescricao(String descricao) {
        if (
            descricao == null ||
            descricao.trim().length() < TAMANHO_MINIMO_DESCRICAO
        ) {
            throw new IllegalArgumentException(
                "A descrição deve ter no mínimo 20 caracteres."
            );
        }
    }

    private void validarLocalizacao(String localizacao) {
        if (
            localizacao == null ||
            localizacao.trim().length() < TAMANHO_MINIMO_LOCALIZACAO
        ) {
            throw new IllegalArgumentException(
                "A localização deve ter no mínimo 5 caracteres."
            );
        }
    }

    private void validarPrioridade(Prioridade prioridade) {
        if (prioridade == null) {
            throw new IllegalArgumentException("A prioridade é obrigatória.");
        }
    }

    private void validarDataAbertura(LocalDateTime dataAbertura) {
        if (dataAbertura == null) {
            throw new IllegalArgumentException(
                "A data de abertura é obrigatória."
            );
        }
    }

    private boolean deveForcarAnonimato(Categoria categoria) {
        return (
            categoria == Categoria.ASSEDIO ||
            categoria == Categoria.DENUNCIA_IRREGULARIDADE
        );
    }
}
