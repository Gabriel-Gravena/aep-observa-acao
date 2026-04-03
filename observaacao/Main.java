package observaacao;

import java.time.LocalDateTime;
import observaacao.model.Categoria;
import observaacao.model.HistoricoStatus;
import observaacao.model.Solicitacao;
import observaacao.model.Usuario;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;
import observaacao.model.enums.TipoUsuario;
import observaacao.repository.RepositorioSolicitacoes;
import observaacao.service.FilaAtendimento;
import observaacao.service.GeradorProtocolo;
import observaacao.service.ServicoAnonimato;
import observaacao.service.ServicoSolicitacoes;
import observaacao.ui.MenuCLI;

public class Main {

    private static final String SISTEMA = "SISTEMA";
    private static final String SERVIDOR_JOANA = "Joana Servidora";
    private static final String SERVIDOR_CARLOS = "Carlos Fiscal";

    public static void main(String[] args) {
        RepositorioSolicitacoes repositorio = new RepositorioSolicitacoes();
        FilaAtendimento filaAtendimento = new FilaAtendimento(repositorio);
        GeradorProtocolo geradorProtocolo = new GeradorProtocolo();
        ServicoAnonimato servicoAnonimato = new ServicoAnonimato();
        ServicoSolicitacoes servicoSolicitacoes = new ServicoSolicitacoes(
            repositorio,
            filaAtendimento,
            geradorProtocolo,
            servicoAnonimato
        );

        popularDadosIniciais(
            filaAtendimento,
            geradorProtocolo,
            servicoAnonimato
        );

        MenuCLI menuCLI = new MenuCLI(servicoSolicitacoes);
        menuCLI.iniciar();
    }

    private static void popularDadosIniciais(
        FilaAtendimento filaAtendimento,
        GeradorProtocolo geradorProtocolo,
        ServicoAnonimato servicoAnonimato
    ) {
        adicionarSolicitacao(
            filaAtendimento,
            criarSolicitacaoAnonima(
                geradorProtocolo,
                servicoAnonimato,
                Categoria.ASSEDIO,
                "Relato de assédio recorrente em unidade administrativa municipal.",
                "Centro Cívico",
                Prioridade.ALTA,
                LocalDateTime.now().minusDays(1)
            )
        );

        adicionarSolicitacao(
            filaAtendimento,
            criarSolicitacaoAnonima(
                geradorProtocolo,
                servicoAnonimato,
                Categoria.DENUNCIA_IRREGULARIDADE,
                "Denúncia de possível irregularidade em processo de atendimento local.",
                "Jardim Alvorada",
                Prioridade.MEDIA,
                LocalDateTime.now().minusDays(3)
            )
        );

        adicionarSolicitacao(
            filaAtendimento,
            criarSolicitacaoComUsuario(
                geradorProtocolo,
                new Usuario(
                    "Marcos Lima",
                    "44999990001",
                    TipoUsuario.CIDADAO,
                    false
                ),
                Categoria.BURACO_VIA,
                "Buraco profundo na via principal com risco grave para carros e motos.",
                "Avenida Colombo",
                Prioridade.CRITICA,
                LocalDateTime.now().minusHours(10)
            )
        );

        adicionarSolicitacao(
            filaAtendimento,
            criarSolicitacaoEmExecucao(geradorProtocolo)
        );

        adicionarSolicitacao(
            filaAtendimento,
            criarSolicitacaoAtrasada(geradorProtocolo)
        );
    }

    private static void adicionarSolicitacao(
        FilaAtendimento filaAtendimento,
        Solicitacao solicitacao
    ) {
        filaAtendimento.adicionar(solicitacao);
    }

    private static Solicitacao criarSolicitacaoAnonima(
        GeradorProtocolo geradorProtocolo,
        ServicoAnonimato servicoAnonimato,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        LocalDateTime dataAbertura
    ) {
        return new Solicitacao(
            geradorProtocolo.gerar(),
            servicoAnonimato.criarUsuarioAnonimo(),
            categoria,
            descricao,
            localizacao,
            prioridade,
            true,
            dataAbertura
        );
    }

    private static Solicitacao criarSolicitacaoComUsuario(
        GeradorProtocolo geradorProtocolo,
        Usuario usuario,
        Categoria categoria,
        String descricao,
        String localizacao,
        Prioridade prioridade,
        LocalDateTime dataAbertura
    ) {
        return new Solicitacao(
            geradorProtocolo.gerar(),
            usuario,
            categoria,
            descricao,
            localizacao,
            prioridade,
            false,
            dataAbertura
        );
    }

    private static Solicitacao criarSolicitacaoEmExecucao(
        GeradorProtocolo geradorProtocolo
    ) {
        Solicitacao solicitacao = criarSolicitacaoComUsuario(
            geradorProtocolo,
            new Usuario(
                "Ana Souza",
                "ana.souza@email.com",
                TipoUsuario.CIDADAO,
                false
            ),
            Categoria.ILUMINACAO,
            "Postes apagados em sequência deixando a rua completamente escura à noite.",
            "Zona 7",
            Prioridade.ALTA,
            LocalDateTime.now().minusDays(2)
        );

        adicionarHistorico(
            solicitacao,
            StatusSolicitacao.ABERTO,
            StatusSolicitacao.TRIAGEM,
            SERVIDOR_JOANA,
            "Chamado analisado e encaminhado para equipe técnica.",
            LocalDateTime.now().minusDays(2).plusHours(3)
        );

        adicionarHistorico(
            solicitacao,
            StatusSolicitacao.TRIAGEM,
            StatusSolicitacao.EM_EXECUCAO,
            SERVIDOR_CARLOS,
            "Equipe deslocada para avaliação e início da manutenção.",
            LocalDateTime.now().minusDays(1).plusHours(2)
        );

        return solicitacao;
    }

    private static Solicitacao criarSolicitacaoAtrasada(
        GeradorProtocolo geradorProtocolo
    ) {
        Solicitacao solicitacao = criarSolicitacaoComUsuario(
            geradorProtocolo,
            new Usuario(
                "Paulo Mendes",
                "44988887777",
                TipoUsuario.CIDADAO,
                false
            ),
            Categoria.LIMPEZA_PUBLICA,
            "Acúmulo de lixo e entulho há semanas causando mau cheiro no bairro.",
            "Vila Esperança",
            Prioridade.BAIXA,
            LocalDateTime.now().minusDays(45)
        );

        adicionarHistorico(
            solicitacao,
            null,
            StatusSolicitacao.ABERTO,
            SISTEMA,
            "Solicitação registrada na base de testes.",
            LocalDateTime.now().minusDays(45)
        );

        return solicitacao;
    }

    private static void adicionarHistorico(
        Solicitacao solicitacao,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String responsavel,
        String comentario,
        LocalDateTime dataMovimentacao
    ) {
        HistoricoStatus historico = new HistoricoStatus(
            statusAnterior,
            statusNovo,
            dataMovimentacao,
            responsavel,
            comentario
        );
        solicitacao.adicionarHistorico(historico);
    }
}
