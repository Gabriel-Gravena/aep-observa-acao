package observaacao.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import observaacao.model.Categoria;
import observaacao.model.HistoricoStatus;
import observaacao.model.Solicitacao;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;
import observaacao.service.ServicoSolicitacoes;

public class MenuCLI {

    private static final String LINHA =
        "=========================================";
    private static final String TITULO = "  ObservaAção — Sistema de Demandas";
    private static final String MENSAGEM_SAIDA =
        "Encerrando o sistema. Até logo!";
    private static final String MENSAGEM_CONTINUAR =
        "Pressione Enter para voltar ao menu...";
    private static final String BAIRRO_TODOS = "TODOS";
    private static final String ENTRADA_VAZIA = "";
    private static final String ATRASADO = "[ATRASADO] ";
    private static final String ANSI_LIMPAR_TELA = "\u001B[H\u001B[2J";
    private static final DateTimeFormatter FORMATADOR_DATA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATADOR_DATA_HORA =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private enum OpcaoMenu {
        REGISTRAR(1, "Registrar nova solicitação"),
        ACOMPANHAR(2, "Acompanhar solicitação (buscar por protocolo)"),
        HISTORICO(3, "Ver histórico completo de solicitação"),
        LISTAR_FILA(4, "Listar fila de atendimento (por prioridade)"),
        ATUALIZAR_STATUS(5, "Atualizar status de solicitação"),
        LISTAR_ATRASADAS(6, "Listar demandas atrasadas"),
        FILTRAR(7, "Filtrar por bairro e categoria"),
        SAIR(8, "Sair");

        private final int codigo;
        private final String descricao;

        OpcaoMenu(int codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }

        public int getCodigo() {
            return codigo;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final ServicoSolicitacoes servicoSolicitacoes;
    private final Scanner scanner;

    public MenuCLI(ServicoSolicitacoes servicoSolicitacoes) {
        this(servicoSolicitacoes, new Scanner(System.in));
    }

    public MenuCLI(ServicoSolicitacoes servicoSolicitacoes, Scanner scanner) {
        if (servicoSolicitacoes == null) {
            throw new IllegalArgumentException(
                "O serviço de solicitações é obrigatório."
            );
        }
        if (scanner == null) {
            throw new IllegalArgumentException("O scanner é obrigatório.");
        }
        this.servicoSolicitacoes = servicoSolicitacoes;
        this.scanner = scanner;
    }

    public void iniciar() {
        boolean executando = true;
        while (executando) {
            limparTela();
            exibirMenu();
            executando = processarOpcao(lerOpcao());
            if (executando) {
                aguardarContinuacao();
            }
        }
    }

    private void exibirMenu() {
        OpcaoMenu[] opcoes = OpcaoMenu.values();
        int i = 0;

        System.out.println();
        System.out.println(LINHA);
        System.out.println(TITULO);
        System.out.println(LINHA);
        System.out.println();

        while (i < opcoes.length) {
            exibirOpcaoMenu(opcoes[i]);
            i++;
        }

        System.out.println();
        System.out.print("Opção: ");
    }

    private boolean processarOpcao(int opcao) {
        if (opcao == OpcaoMenu.REGISTRAR.getCodigo()) {
            registrarNovaSolicitacao();
            return true;
        }
        if (opcao == OpcaoMenu.ACOMPANHAR.getCodigo()) {
            acompanharSolicitacao();
            return true;
        }
        if (opcao == OpcaoMenu.HISTORICO.getCodigo()) {
            verHistoricoCompleto();
            return true;
        }
        if (opcao == OpcaoMenu.LISTAR_FILA.getCodigo()) {
            listarFilaAtendimento();
            return true;
        }
        if (opcao == OpcaoMenu.ATUALIZAR_STATUS.getCodigo()) {
            atualizarStatusSolicitacao();
            return true;
        }
        if (opcao == OpcaoMenu.LISTAR_ATRASADAS.getCodigo()) {
            listarDemandasAtrasadas();
            return true;
        }
        if (opcao == OpcaoMenu.FILTRAR.getCodigo()) {
            filtrarPorBairroECategoria();
            return true;
        }
        if (opcao == OpcaoMenu.SAIR.getCodigo()) {
            System.out.println(MENSAGEM_SAIDA);
            return false;
        }
        System.out.println("Opção inválida. Escolha um número entre 1 e 8.");
        return true;
    }

    private int lerOpcao() {
        try {
            String entrada = scanner.nextLine();
            return Integer.parseInt(entrada.trim());
        } catch (RuntimeException e) {
            return -1;
        }
    }

    private void registrarNovaSolicitacao() {
        exibirCabecalhoAcao("Cadastro de nova solicitação");
        try {
            String nome = lerTexto("Nome (Enter para anônimo): ");
            String contato = lerTexto("Contato (email/telefone, opcional): ");
            Categoria categoria = lerCategoria(
                "Categoria " + categoriasDisponiveis() + ": "
            );
            String descricao = lerTexto("Descrição (mínimo 20 caracteres): ");
            String localizacao = lerTexto("Localização/Bairro: ");
            Prioridade prioridade = lerPrioridade(
                "Prioridade " + prioridadesDisponiveis() + ": "
            );
            boolean anonimo = nome.isBlank();

            Solicitacao solicitacao = servicoSolicitacoes.registrarSolicitacao(
                nome,
                contato,
                anonimo,
                categoria,
                descricao,
                localizacao,
                prioridade
            );

            exibirResumoCadastro(solicitacao);
        } catch (IllegalArgumentException e) {
            System.out.println(
                "Não foi possível registrar a solicitação: " + e.getMessage()
            );
        } catch (Exception e) {
            System.out.println(
                "Ocorreu um erro ao processar sua solicitação. Tente novamente."
            );
        }
    }

    private void acompanharSolicitacao() {
        exibirCabecalhoAcao("Acompanhar solicitação");
        String protocolo = lerTexto("Informe o protocolo: ");
        Optional<Solicitacao> solicitacao =
            servicoSolicitacoes.buscarPorProtocolo(protocolo);
        limparTela();
        if (solicitacao.isEmpty()) {
            System.out.println("Solicitação não encontrada.");
            return;
        }
        exibirResumoSolicitacao(solicitacao.get());
    }

    private void verHistoricoCompleto() {
        exibirCabecalhoAcao("Histórico completo");
        String protocolo = lerTexto("Informe o protocolo: ");
        try {
            String relatorio = servicoSolicitacoes.gerarRelatorioSolicitacao(
                protocolo
            );
            limparTela();
            System.out.println(LINHA);
            System.out.println(relatorio);
            System.out.println(LINHA);
        } catch (IllegalArgumentException e) {
            limparTela();
            System.out.println(
                "Não foi possível gerar o histórico: " + e.getMessage()
            );
        }
    }

    private void listarFilaAtendimento() {
        exibirCabecalhoAcao("Fila de atendimento");
        String bairro = lerTexto("Filtrar por bairro (Enter para todas): ");
        String filtro = bairro.isBlank() ? null : bairro;
        List<Solicitacao> fila = servicoSolicitacoes.listarFilaServidor(filtro);
        exibirListaSolicitacoes("Fila de atendimento", fila);
    }

    private void atualizarStatusSolicitacao() {
        exibirCabecalhoAcao("Atualizar status");
        try {
            String protocolo = lerTexto("Protocolo: ");
            StatusSolicitacao novoStatus = lerStatus(
                "Novo status " + statusDisponiveis() + ": "
            );
            String responsavel = lerTexto("Nome do responsável: ");
            String comentario = lerTexto("Comentário obrigatório: ");
            limparTela();

            servicoSolicitacoes.atualizarStatus(
                protocolo,
                novoStatus,
                responsavel,
                comentario
            );
            System.out.println("✔ Status atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            limparTela();
            System.out.println(
                "Atualização cancelada: " + e.getMessage()
            );
        } catch (IllegalStateException e) {
            limparTela();
            System.out.println("Transição inválida: " + e.getMessage());
            System.out.println(
                "Tentativa registrada no console para auditoria."
            );
        } catch (Exception e) {
            limparTela();
            System.out.println("Ocorreu um erro ao atualizar a solicitação.");
        }
    }

    private void listarDemandasAtrasadas() {
        exibirCabecalhoAcao("Demandas atrasadas");
        List<Solicitacao> atrasadas =
            servicoSolicitacoes.listarDemandasAtrasadas();
        exibirListaSolicitacoes("Demandas atrasadas", atrasadas);
    }

    private void filtrarPorBairroECategoria() {
        exibirCabecalhoAcao("Filtro por bairro e categoria");
        String bairro = lerTexto("Bairro (Enter para todos): ");
        String categoriaEntrada = lerTexto(
            "Categoria " + categoriasDisponiveis() + " (Enter para todas): "
        );
        Categoria categoriaFiltro = null;
        if (!categoriaEntrada.isBlank()) {
            try {
                categoriaFiltro = interpretarCategoria(categoriaEntrada);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        List<Solicitacao> fila = servicoSolicitacoes.listarFilaServidor(
            bairroOuNulo(bairro)
        );
        List<Solicitacao> filtradas = aplicarFiltroCategoria(
            fila,
            categoriaFiltro
        );
        exibirListaSolicitacoes("Solicitações filtradas", filtradas);
    }

    private List<Solicitacao> aplicarFiltroCategoria(
        List<Solicitacao> solicitacoes,
        Categoria categoria
    ) {
        java.util.List<Solicitacao> filtradas = new java.util.ArrayList<
            Solicitacao
        >();
        int i = 0;

        if (categoria == null) {
            return solicitacoes;
        }

        while (i < solicitacoes.size()) {
            Solicitacao solicitacao = solicitacoes.get(i);
            if (solicitacao.getCategoria() == categoria) {
                filtradas.add(solicitacao);
            }
            i++;
        }

        return filtradas;
    }

    private String bairroOuNulo(String bairro) {
        if (
            bairro == null ||
            bairro.isBlank() ||
            BAIRRO_TODOS.equalsIgnoreCase(bairro.trim())
        ) {
            return null;
        }
        return bairro.trim();
    }

    private void exibirResumoCadastro(Solicitacao solicitacao) {
        limparTela();
        System.out.println();
        System.out.println(LINHA);
        System.out.println("✔ Solicitação registrada com sucesso!");
        System.out.println("Protocolo: " + solicitacao.getProtocolo());
        System.out.println(
            "Prazo alvo: " + solicitacao.getPrazoAlvo().format(FORMATADOR_DATA)
        );
        System.out.println("Status: " + solicitacao.getStatus());
        System.out.println("Guarde seu protocolo para acompanhar o andamento.");
        System.out.println(LINHA);
    }

    private void exibirResumoSolicitacao(Solicitacao solicitacao) {
        limparTela();
        System.out.println();
        System.out.println(LINHA);
        System.out.println("Protocolo: " + solicitacao.getProtocolo());
        System.out.println("Status: " + solicitacao.getStatus());
        System.out.println(
            "Categoria: " + solicitacao.getCategoria().getNome()
        );
        System.out.println("Prioridade: " + solicitacao.getPrioridade());
        System.out.println(
            "Prazo alvo: " + solicitacao.getPrazoAlvo().format(FORMATADOR_DATA)
        );
        System.out.println("Localização: " + solicitacao.getLocalizacao());
        System.out.println(
            "Prazo em dia: " + (solicitacao.estaNoPrazo() ? "SIM" : "NÃO")
        );
        System.out.println("Descrição: " + solicitacao.getDescricao());
        exibirHistoricoResumido(solicitacao.getHistorico());
        System.out.println(LINHA);
    }

    private void exibirHistoricoResumido(List<HistoricoStatus> historico) {
        if (historico.isEmpty()) {
            System.out.println("Histórico: nenhuma movimentação registrada.");
            return;
        }
        HistoricoStatus ultimo = historico.get(historico.size() - 1);
        System.out.println(
            "Última movimentação: " +
                ultimo.getDataMovimentacao().format(FORMATADOR_DATA_HORA) +
                " | " +
                ultimo.getResponsavel() +
                " | " +
                ultimo.getComentario()
        );
    }

    private void exibirListaSolicitacoes(
        String titulo,
        List<Solicitacao> solicitacoes
    ) {
        int i = 0;

        limparTela();
        System.out.println();
        System.out.println(LINHA);
        System.out.println(titulo);
        System.out.println(LINHA);
        if (solicitacoes == null || solicitacoes.isEmpty()) {
            System.out.println("Nenhuma solicitação encontrada.");
            System.out.println(LINHA);
            return;
        }

        while (i < solicitacoes.size()) {
            exibirLinhaSolicitacao(solicitacoes.get(i));
            i++;
        }

        System.out.println(LINHA);
    }

    private void exibirLinhaSolicitacao(Solicitacao solicitacao) {
        String marcadorAtraso = solicitacao.estaNoPrazo()
            ? ENTRADA_VAZIA
            : ATRASADO;
        System.out.println(
            marcadorAtraso +
                solicitacao.getProtocolo() +
                " | " +
                solicitacao.getPrioridade() +
                " | " +
                solicitacao.getStatus() +
                " | " +
                solicitacao.getCategoria().getId() +
                " | " +
                solicitacao.getLocalizacao()
        );
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        String valor = scanner.nextLine();
        return valor == null ? ENTRADA_VAZIA : valor.trim();
    }

    private void exibirCabecalhoAcao(String titulo) {
        limparTela();
        System.out.println();
        System.out.println(LINHA);
        System.out.println(" " + titulo);
        System.out.println(LINHA);
    }

    private void aguardarContinuacao() {
        System.out.println();
        System.out.print(MENSAGEM_CONTINUAR);
        scanner.nextLine();
    }

    private void limparTela() {
        if (executarComandoLimpeza()) {
            return;
        }
        System.out.print(ANSI_LIMPAR_TELA);
        System.out.flush();
    }

    private boolean executarComandoLimpeza() {
        String sistemaOperacional = System
            .getProperty("os.name", "")
            .toLowerCase();
        ProcessBuilder processBuilder;

        if (sistemaOperacional.contains("win")) {
            processBuilder = new ProcessBuilder("cmd", "/c", "cls");
        } else {
            processBuilder = new ProcessBuilder("clear");
        }

        processBuilder.inheritIO();

        try {
            Process processo = processBuilder.start();
            return processo.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Prioridade lerPrioridade(String mensagem) {
        String entrada = lerTexto(mensagem);
        try {
            return Prioridade.valueOf(entrada.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Prioridade inválida. Use uma das opções " +
                    prioridadesDisponiveis() +
                    "."
            );
        }
    }

    private Categoria lerCategoria(String mensagem) {
        String entrada = lerTexto(mensagem);
        return interpretarCategoria(entrada);
    }

    private Categoria interpretarCategoria(String entrada) {
        Optional<Categoria> categoria = Categoria.porId(entrada);
        if (categoria.isPresent()) {
            return categoria.get();
        }
        throw new IllegalArgumentException(
            "Categoria inválida. Use uma das opções " +
                categoriasDisponiveis() +
                "."
        );
    }

    private StatusSolicitacao lerStatus(String mensagem) {
        String entrada = lerTexto(mensagem);
        if (entrada.isBlank()) {
            throw new IllegalArgumentException(
                "Status não informado. Informe um status para continuar."
            );
        }
        try {
            return StatusSolicitacao.valueOf(entrada.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Status inválido. Use uma das opções " +
                    statusDisponiveis() +
                    "."
            );
        }
    }

    private String categoriasDisponiveis() {
        List<Categoria> categorias = Categoria.todas();
        StringBuilder texto = new StringBuilder("(");
        int i = 0;

        while (i < categorias.size()) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(categorias.get(i).getId());
            i++;
        }

        texto.append(")");
        return texto.toString();
    }

    private String prioridadesDisponiveis() {
        return valoresEnum(Prioridade.values());
    }

    private String statusDisponiveis() {
        return valoresEnum(StatusSolicitacao.values());
    }

    private String valoresEnum(Enum<?>[] valores) {
        StringBuilder texto = new StringBuilder("(");
        int i = 0;

        while (i < valores.length) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(valores[i].name());
            i++;
        }

        texto.append(")");
        return texto.toString();
    }

    private void exibirOpcaoMenu(OpcaoMenu opcaoMenu) {
        System.out.println(
            "  [" + opcaoMenu.getCodigo() + "] " + opcaoMenu.getDescricao()
        );
    }
}
