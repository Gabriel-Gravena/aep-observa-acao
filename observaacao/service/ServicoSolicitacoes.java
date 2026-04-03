package observaacao.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import observaacao.model.Categoria;
import observaacao.model.HistoricoStatus;
import observaacao.model.Solicitacao;
import observaacao.model.Usuario;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;
import observaacao.model.enums.TipoUsuario;
import observaacao.repository.RepositorioSolicitacoes;

public class ServicoSolicitacoes {

    private static final String MENSAGEM_COMENTARIO_OBRIGATORIO =
        "Comentário é obrigatório.";
    private static final String MENSAGEM_PROTOCOLO_OBRIGATORIO =
        "O protocolo é obrigatório.";
    private static final String MENSAGEM_RESPONSAVEL_PADRAO = "SISTEMA";
    private static final String MENSAGEM_DESCRICAO_INVALIDA =
        "A descrição deve ter no mínimo 20 caracteres.";
    private static final String MENSAGEM_LOCALIZACAO_INVALIDA =
        "A localização deve ter no mínimo 5 caracteres.";
    private static final int TAMANHO_MINIMO_DESCRICAO = 20;
    private static final int TAMANHO_MINIMO_LOCALIZACAO = 5;
    private static final DateTimeFormatter FORMATADOR_DATA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATADOR_DATA_HORA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RepositorioSolicitacoes repositorioSolicitacoes;
    private final FilaAtendimento filaAtendimento;
    private final GeradorProtocolo geradorProtocolo;
    private final ServicoAnonimato servicoAnonimato;

    public ServicoSolicitacoes(
        RepositorioSolicitacoes repositorioSolicitacoes,
        FilaAtendimento filaAtendimento,
        GeradorProtocolo geradorProtocolo,
        ServicoAnonimato servicoAnonimato
    ) {
        validarDependencias(
            repositorioSolicitacoes,
            filaAtendimento,
            geradorProtocolo,
            servicoAnonimato
        );
        this.repositorioSolicitacoes = repositorioSolicitacoes;
        this.filaAtendimento = filaAtendimento;
        this.geradorProtocolo = geradorProtocolo;
        this.servicoAnonimato = servicoAnonimato;
    }

    public Solicitacao registrarSolicitacao(
        String nomeUsuario,
        String contato,
        boolean anonimo,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade
    ) {
        return registrarSolicitacaoInterna(
            nomeUsuario,
            contato,
            anonimo,
            categoria,
            descricao,
            localizacao,
            prioridade,
            LocalDateTime.now()
        );
    }

    public Solicitacao registrarSolicitacaoComData(
        String nomeUsuario,
        String contato,
        boolean anonimo,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        LocalDateTime dataAbertura
    ) {
        return registrarSolicitacaoInterna(
            nomeUsuario,
            contato,
            anonimo,
            categoria,
            descricao,
            localizacao,
            prioridade,
            validarDataAbertura(dataAbertura)
        );
    }

    public void atualizarStatus(
        String protocolo,
        StatusSolicitacao novoStatus,
        String nomeResponsavel,
        String comentario
    ) {
        validarComentario(comentario);
        Solicitacao solicitacao = obterSolicitacaoObrigatoria(protocolo);
        validarNovoStatus(novoStatus);
        validarTransicao(solicitacao, novoStatus);
        HistoricoStatus historico = criarHistorico(
            solicitacao,
            novoStatus,
            nomeResponsavel,
            comentario
        );
        solicitacao.adicionarHistorico(historico);
        repositorioSolicitacoes.salvar(solicitacao);
    }

    public Optional<Solicitacao> buscarPorProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            return Optional.empty();
        }
        return repositorioSolicitacoes.buscarPorProtocolo(protocolo.trim());
    }

    public List<Solicitacao> listarFilaServidor(String bairro) {
        List<Solicitacao> fila = filaAtendimento.obterFilaPorPrioridade();
        if (bairro == null || bairro.trim().isEmpty()) {
            return fila;
        }
        String bairroNormalizado = bairro.trim().toLowerCase();
        return filtrarPorBairro(fila, bairroNormalizado);
    }

    public List<Solicitacao> listarDemandasAtrasadas() {
        return filaAtendimento.obterAtrasadas();
    }

    public String gerarRelatorioSolicitacao(String protocolo) {
        Solicitacao solicitacao = obterSolicitacaoObrigatoria(protocolo);
        StringBuilder relatorio = new StringBuilder();
        adicionarCabecalhoRelatorio(relatorio, solicitacao);
        adicionarDadosSolicitante(relatorio, solicitacao);
        adicionarHistoricoRelatorio(relatorio, solicitacao);
        return relatorio.toString();
    }

    private Solicitacao registrarSolicitacaoInterna(
        String nomeUsuario,
        String contato,
        boolean anonimo,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        LocalDateTime dataAbertura
    ) {
        validarCategoria(categoria);
        boolean anonimatoObrigatorio = servicoAnonimato.deveSerAnonimo(
            categoria
        );
        validarCamposSolicitacao(descricao, localizacao, prioridade);
        validarDataAbertura(dataAbertura);
        Usuario usuario = criarSolicitante(
            nomeUsuario,
            contato,
            anonimo || anonimatoObrigatorio
        );
        Solicitacao solicitacao = criarSolicitacao(
            usuario,
            categoria,
            descricao,
            localizacao,
            prioridade,
            anonimo || anonimatoObrigatorio,
            dataAbertura
        );
        filaAtendimento.adicionar(solicitacao);
        return solicitacao;
    }

    private void validarDependencias(
        RepositorioSolicitacoes repositorioSolicitacoes,
        FilaAtendimento filaAtendimento,
        GeradorProtocolo geradorProtocolo,
        ServicoAnonimato servicoAnonimato
    ) {
        if (repositorioSolicitacoes == null) {
            throw new IllegalArgumentException(
                "O repositório de solicitações é obrigatório."
            );
        }
        if (filaAtendimento == null) {
            throw new IllegalArgumentException(
                "A fila de atendimento é obrigatória."
            );
        }
        if (geradorProtocolo == null) {
            throw new IllegalArgumentException(
                "O gerador de protocolo é obrigatório."
            );
        }
        if (servicoAnonimato == null) {
            throw new IllegalArgumentException(
                "O serviço de anonimato é obrigatório."
            );
        }
    }

    private LocalDateTime validarDataAbertura(LocalDateTime dataAbertura) {
        if (dataAbertura == null) {
            throw new IllegalArgumentException(
                "A data de abertura é obrigatória."
            );
        }
        return dataAbertura;
    }

    private Usuario criarSolicitante(
        String nomeUsuario,
        String contato,
        boolean anonimo
    ) {
        if (anonimo) {
            return servicoAnonimato.criarUsuarioAnonimo();
        }
        return new Usuario(nomeUsuario, contato, TipoUsuario.CIDADAO, false);
    }

    private void validarCamposSolicitacao(
        String descricao,
        String localizacao,
        Prioridade prioridade
    ) {
        validarDescricao(descricao);
        validarLocalizacao(localizacao);
        validarPrioridade(prioridade);
    }

    private void validarDescricao(String descricao) {
        if (
            descricao == null ||
            descricao.trim().length() < TAMANHO_MINIMO_DESCRICAO
        ) {
            throw new IllegalArgumentException(MENSAGEM_DESCRICAO_INVALIDA);
        }
    }

    private void validarLocalizacao(String localizacao) {
        if (
            localizacao == null ||
            localizacao.trim().length() < TAMANHO_MINIMO_LOCALIZACAO
        ) {
            throw new IllegalArgumentException(MENSAGEM_LOCALIZACAO_INVALIDA);
        }
    }

    private void validarPrioridade(Prioridade prioridade) {
        if (prioridade == null) {
            throw new IllegalArgumentException("A prioridade é obrigatória.");
        }
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }
    }

    private Solicitacao criarSolicitacao(
        Usuario usuario,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        boolean anonimo,
        LocalDateTime dataAbertura
    ) {
        return new Solicitacao(
            geradorProtocolo.gerar(),
            usuario,
            categoria,
            descricao,
            localizacao,
            prioridade,
            anonimo,
            dataAbertura
        );
    }

    private Solicitacao obterSolicitacaoObrigatoria(String protocolo) {
        Optional<Solicitacao> solicitacaoEncontrada;
        validarProtocolo(protocolo);
        solicitacaoEncontrada = repositorioSolicitacoes.buscarPorProtocolo(
            protocolo.trim()
        );
        if (solicitacaoEncontrada.isPresent()) {
            return solicitacaoEncontrada.get();
        }
        throw new IllegalArgumentException(
            "Solicitação não encontrada para o protocolo informado."
        );
    }

    private void validarComentario(String comentario) {
        if (comentario == null || comentario.trim().isEmpty()) {
            throw new IllegalArgumentException(MENSAGEM_COMENTARIO_OBRIGATORIO);
        }
    }

    private void validarProtocolo(String protocolo) {
        if (protocolo == null || protocolo.trim().isEmpty()) {
            throw new IllegalArgumentException(MENSAGEM_PROTOCOLO_OBRIGATORIO);
        }
    }

    private void validarNovoStatus(StatusSolicitacao novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("O novo status é obrigatório.");
        }
    }

    private void validarTransicao(
        Solicitacao solicitacao,
        StatusSolicitacao novoStatus
    ) {
        StatusSolicitacao statusAtual = solicitacao.getStatus();
        if (statusAtual.isTransicaoValida(novoStatus)) {
            return;
        }
        String mensagem = String.format(
            "Transição inválida de %s para %s no protocolo %s.",
            statusAtual,
            novoStatus,
            solicitacao.getProtocolo()
        );
        System.out.println(mensagem);
        throw new IllegalStateException(mensagem);
    }

    private HistoricoStatus criarHistorico(
        Solicitacao solicitacao,
        StatusSolicitacao novoStatus,
        String nomeResponsavel,
        String comentario
    ) {
        return new HistoricoStatus(
            solicitacao.getStatus(),
            novoStatus,
            normalizarResponsavel(nomeResponsavel),
            comentario
        );
    }

    private String normalizarResponsavel(String nomeResponsavel) {
        if (nomeResponsavel == null || nomeResponsavel.trim().isEmpty()) {
            return MENSAGEM_RESPONSAVEL_PADRAO;
        }
        return nomeResponsavel.trim();
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

    private List<Solicitacao> filtrarPorBairro(
        List<Solicitacao> fila,
        String bairroNormalizado
    ) {
        java.util.ArrayList<Solicitacao> filtradas = new java.util.ArrayList<
            Solicitacao
        >();
        for (int i = 0; i < fila.size(); i++) {
            Solicitacao solicitacao = fila.get(i);
            if (contemBairro(solicitacao, bairroNormalizado)) {
                filtradas.add(solicitacao);
            }
        }
        return filtradas;
    }

    private List<HistoricoStatus> copiarHistorico(
        List<HistoricoStatus> historico
    ) {
        java.util.ArrayList<HistoricoStatus> copia = new java.util.ArrayList<
            HistoricoStatus
        >();
        for (int i = 0; i < historico.size(); i++) {
            copia.add(historico.get(i));
        }
        return copia;
    }

    private void ordenarHistoricoPorData(List<HistoricoStatus> historico) {
        for (int i = 1; i < historico.size(); i++) {
            HistoricoStatus atual = historico.get(i);
            int j = i - 1;
            while (j >= 0 && veioDepois(historico.get(j), atual)) {
                historico.set(j + 1, historico.get(j));
                j--;
            }
            historico.set(j + 1, atual);
        }
    }

    private boolean veioDepois(
        HistoricoStatus historicoAtual,
        HistoricoStatus historicoComparado
    ) {
        return historicoAtual
            .getDataMovimentacao()
            .isAfter(historicoComparado.getDataMovimentacao());
    }

    private void adicionarCabecalhoRelatorio(
        StringBuilder relatorio,
        Solicitacao solicitacao
    ) {
        relatorio.append("=========================================\n");
        relatorio
            .append("Protocolo: ")
            .append(solicitacao.getProtocolo())
            .append('\n');
        relatorio
            .append("Categoria: ")
            .append(solicitacao.getCategoria().getNome())
            .append('\n');
        relatorio
            .append("Status atual: ")
            .append(solicitacao.getStatus())
            .append('\n');
        relatorio
            .append("Prioridade: ")
            .append(solicitacao.getPrioridade())
            .append('\n');
        relatorio
            .append("Localização: ")
            .append(solicitacao.getLocalizacao())
            .append('\n');
        relatorio
            .append("Prazo alvo: ")
            .append(formatarData(solicitacao.getPrazoAlvo()))
            .append('\n');
        relatorio
            .append("No prazo: ")
            .append(solicitacao.estaNoPrazo() ? "SIM" : "NÃO")
            .append('\n');
        relatorio
            .append("Descrição: ")
            .append(solicitacao.getDescricao())
            .append('\n');
    }

    private void adicionarDadosSolicitante(
        StringBuilder relatorio,
        Solicitacao solicitacao
    ) {
        relatorio
            .append(
                servicoAnonimato.protegerDados(solicitacao.getSolicitante())
            )
            .append('\n');
    }

    private void adicionarHistoricoRelatorio(
        StringBuilder relatorio,
        Solicitacao solicitacao
    ) {
        relatorio.append("-----------------------------------------\n");
        relatorio.append("Histórico completo\n");
        relatorio.append("-----------------------------------------\n");
        if (solicitacao.getHistorico().isEmpty()) {
            relatorio.append("Nenhuma movimentação registrada.\n");
            return;
        }
        List<HistoricoStatus> historicoOrdenado = copiarHistorico(
            solicitacao.getHistorico()
        );
        ordenarHistoricoPorData(historicoOrdenado);
        for (int i = 0; i < historicoOrdenado.size(); i++) {
            relatorio.append(formatarLinhaHistorico(historicoOrdenado.get(i)));
        }
    }

    private String formatarLinhaHistorico(HistoricoStatus historicoStatus) {
        StringBuilder linha = new StringBuilder();
        linha.append(
            historicoStatus.getDataMovimentacao().format(FORMATADOR_DATA_HORA)
        );
        linha.append(" | ");
        linha.append(historicoStatus.getResponsavel());
        linha.append(" | ");
        linha.append(descreverTransicao(historicoStatus));
        linha.append(" | ");
        linha.append(historicoStatus.getComentario());
        linha.append('\n');
        return linha.toString();
    }

    private String descreverTransicao(HistoricoStatus historicoStatus) {
        if (historicoStatus.getStatusAnterior() == null) {
            return "Início -> " + historicoStatus.getStatusNovo();
        }
        return (
            historicoStatus.getStatusAnterior() +
            " -> " +
            historicoStatus.getStatusNovo()
        );
    }

    private String formatarData(LocalDate data) {
        return data.format(FORMATADOR_DATA);
    }
}
