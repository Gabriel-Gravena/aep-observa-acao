package observaacao.ui;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import observaacao.model.Categoria;
import observaacao.model.HistoricoStatus;
import observaacao.model.Solicitacao;
import observaacao.model.Usuario;
import observaacao.model.enums.Prioridade;
import observaacao.model.enums.StatusSolicitacao;
import observaacao.model.enums.TipoUsuario;
import observaacao.service.ServicoSolicitacoes;
import observaacao.service.ServicoUsuarios;

public class MenuCLI {

    private static final String LINHA =
        "=========================================";
    private static final String TITULO = "  ObservaAcao - Sistema de Demandas";
    private static final String MENSAGEM_SAIDA =
        "Encerrando o sistema. Ate logo!";
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

    private enum OpcaoInicial {
        LOGIN(1, "Login"),
        REGISTRO(2, "Registro"),
        SAIR(3, "Sair");

        private final int codigo;
        private final String descricao;

        OpcaoInicial(int codigo, String descricao) {
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

    private enum OpcaoCidadao {
        REGISTRAR(1, "Registrar nova solicitacao"),
        ACOMPANHAR(2, "Acompanhar solicitacao (buscar por protocolo)"),
        HISTORICO(3, "Ver historico completo de solicitacao"),
        LOGOUT(4, "Sair da conta");

        private final int codigo;
        private final String descricao;

        OpcaoCidadao(int codigo, String descricao) {
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

    private enum OpcaoServidor {
        ACOMPANHAR(1, "Acompanhar solicitacao (buscar por protocolo)"),
        HISTORICO(2, "Ver historico completo de solicitacao"),
        LISTAR_FILA(3, "Listar fila de atendimento (por prioridade)"),
        ATUALIZAR_STATUS(4, "Atualizar status de solicitacao"),
        LISTAR_ATRASADAS(5, "Listar demandas atrasadas"),
        FILTRAR(6, "Filtrar por bairro e categoria"),
        LOGOUT(7, "Sair da conta");

        private final int codigo;
        private final String descricao;

        OpcaoServidor(int codigo, String descricao) {
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
    private final ServicoUsuarios servicoUsuarios;
    private final Scanner scanner;
    private Usuario usuarioLogado;

    public MenuCLI(
        ServicoSolicitacoes servicoSolicitacoes,
        ServicoUsuarios servicoUsuarios
    ) {
        this(servicoSolicitacoes, servicoUsuarios, new Scanner(System.in));
    }

    public MenuCLI(
        ServicoSolicitacoes servicoSolicitacoes,
        ServicoUsuarios servicoUsuarios,
        Scanner scanner
    ) {
        if (servicoSolicitacoes == null) {
            throw new IllegalArgumentException(
                "O servico de solicitacoes e obrigatorio."
            );
        }
        if (servicoUsuarios == null) {
            throw new IllegalArgumentException(
                "O servico de usuarios e obrigatorio."
            );
        }
        if (scanner == null) {
            throw new IllegalArgumentException("O scanner e obrigatorio.");
        }
        this.servicoSolicitacoes = servicoSolicitacoes;
        this.servicoUsuarios = servicoUsuarios;
        this.scanner = scanner;
    }

    public void iniciar() {
        boolean executando = true;
        while (executando) {
            if (usuarioLogado == null) {
                limparTela();
                exibirMenuInicial();
                executando = processarOpcaoInicial(lerOpcao());
                if (executando && usuarioLogado == null) {
                    aguardarContinuacao();
                }
            } else {
                executarSessaoUsuario();
            }
        }
    }

    private void executarSessaoUsuario() {
        boolean sessaoAtiva = true;
        while (sessaoAtiva && usuarioLogado != null) {
            limparTela();
            if (usuarioLogadoEhServidor()) {
                exibirMenuServidor();
                sessaoAtiva = processarOpcaoServidor(lerOpcao());
            } else {
                exibirMenuCidadao();
                sessaoAtiva = processarOpcaoCidadao(lerOpcao());
            }
            if (sessaoAtiva && usuarioLogado != null) {
                aguardarContinuacao();
            }
        }
    }

    private void exibirMenuInicial() {
        exibirCabecalho("Acesso ao sistema");
        exibirOpcoesIniciais();
        System.out.println();
        System.out.print("Opcao: ");
    }

    private void exibirMenuCidadao() {
        exibirCabecalhoSessao("Menu do cidadao");
        exibirOpcoesCidadao();
        System.out.println();
        System.out.print("Opcao: ");
    }

    private void exibirMenuServidor() {
        exibirCabecalhoSessao("Menu do servidor");
        exibirOpcoesServidor();
        System.out.println();
        System.out.print("Opcao: ");
    }

    private void exibirCabecalho(String titulo) {
        System.out.println();
        System.out.println(LINHA);
        System.out.println(TITULO);
        System.out.println(LINHA);
        System.out.println(" " + titulo);
        System.out.println(LINHA);
        System.out.println();
    }

    private void exibirCabecalhoSessao(String titulo) {
        exibirCabecalho(titulo);
        System.out.println("Usuario: " + usuarioLogado.getNome());
        System.out.println("Perfil: " + descreverTipoUsuario(usuarioLogado));
        System.out.println();
    }

    private boolean processarOpcaoInicial(int opcao) {
        if (opcao == OpcaoInicial.LOGIN.getCodigo()) {
            fazerLogin();
            return true;
        }
        if (opcao == OpcaoInicial.REGISTRO.getCodigo()) {
            registrarUsuario();
            return true;
        }
        if (opcao == OpcaoInicial.SAIR.getCodigo()) {
            System.out.println(MENSAGEM_SAIDA);
            return false;
        }
        System.out.println("Opcao invalida. Escolha um numero entre 1 e 3.");
        return true;
    }

    private boolean processarOpcaoCidadao(int opcao) {
        if (opcao == OpcaoCidadao.REGISTRAR.getCodigo()) {
            registrarNovaSolicitacao();
            return true;
        }
        if (opcao == OpcaoCidadao.ACOMPANHAR.getCodigo()) {
            acompanharSolicitacao();
            return true;
        }
        if (opcao == OpcaoCidadao.HISTORICO.getCodigo()) {
            verHistoricoCompleto();
            return true;
        }
        if (opcao == OpcaoCidadao.LOGOUT.getCodigo()) {
            encerrarSessao();
            return false;
        }
        System.out.println("Opcao invalida. Escolha um numero entre 1 e 4.");
        return true;
    }

    private boolean processarOpcaoServidor(int opcao) {
        if (opcao == OpcaoServidor.ACOMPANHAR.getCodigo()) {
            acompanharSolicitacao();
            return true;
        }
        if (opcao == OpcaoServidor.HISTORICO.getCodigo()) {
            verHistoricoCompleto();
            return true;
        }
        if (opcao == OpcaoServidor.LISTAR_FILA.getCodigo()) {
            listarFilaAtendimento();
            return true;
        }
        if (opcao == OpcaoServidor.ATUALIZAR_STATUS.getCodigo()) {
            atualizarStatusSolicitacao();
            return true;
        }
        if (opcao == OpcaoServidor.LISTAR_ATRASADAS.getCodigo()) {
            listarDemandasAtrasadas();
            return true;
        }
        if (opcao == OpcaoServidor.FILTRAR.getCodigo()) {
            filtrarPorBairroECategoria();
            return true;
        }
        if (opcao == OpcaoServidor.LOGOUT.getCodigo()) {
            encerrarSessao();
            return false;
        }
        System.out.println("Opcao invalida. Escolha um numero entre 1 e 7.");
        return true;
    }

    private void fazerLogin() {
        exibirCabecalhoAcao("Login");
        try {
            String login = lerTexto("Login: ");
            String senha = lerTexto("Senha: ");
            usuarioLogado = servicoUsuarios.autenticar(login, senha);
            limparTela();
            System.out.println(LINHA);
            System.out.println("Login realizado com sucesso.");
            System.out.println("Usuario: " + usuarioLogado.getNome());
            System.out.println("Perfil: " + descreverTipoUsuario(usuarioLogado));
            System.out.println(LINHA);
        } catch (IllegalArgumentException e) {
            usuarioLogado = null;
            limparTela();
            System.out.println("Falha no login: " + e.getMessage());
        }
    }

    private void registrarUsuario() {
        exibirCabecalhoAcao("Registro");
        try {
            String nome = lerTexto("Nome: ");
            String contato = lerTexto("Contato (email/telefone, opcional): ");
            String login = lerTexto("Login: ");
            String senha = lerTexto("Senha (minimo 4 caracteres): ");
            TipoUsuario tipoUsuario = lerTipoUsuario(
                "Perfil [1] Cidadao [2] Servidor: "
            );

            Usuario usuario = servicoUsuarios.cadastrarUsuario(
                nome,
                contato,
                login,
                senha,
                tipoUsuario
            );

            limparTela();
            System.out.println(LINHA);
            System.out.println("Cadastro realizado com sucesso.");
            System.out.println("Usuario: " + usuario.getNome());
            System.out.println("Perfil: " + descreverTipoUsuario(usuario));
            System.out.println("Agora faca login para acessar o sistema.");
            System.out.println(LINHA);
        } catch (IllegalArgumentException e) {
            limparTela();
            System.out.println("Nao foi possivel concluir o cadastro: " + e.getMessage());
        }
    }

    private void encerrarSessao() {
        limparTela();
        System.out.println("Sessao encerrada para " + usuarioLogado.getNome() + ".");
        usuarioLogado = null;
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
        exibirCabecalhoAcao("Cadastro de nova solicitacao");
        try {
            Categoria categoria = lerCategoria(
                "Categoria " + categoriasDisponiveis() + ": "
            );
            String descricao = lerTexto("Descricao (minimo 20 caracteres): ");
            String localizacao = lerTexto("Localizacao/Bairro: ");
            Prioridade prioridade = lerPrioridade(
                "Prioridade " + prioridadesDisponiveis() + ": "
            );
            boolean anonimo = lerConfirmacao(
                "Registrar de forma anonima? (S/N): "
            );

            Solicitacao solicitacao = servicoSolicitacoes.registrarSolicitacao(
                usuarioLogado,
                anonimo,
                categoria,
                descricao,
                localizacao,
                prioridade
            );

            exibirResumoCadastro(solicitacao);
        } catch (IllegalArgumentException e) {
            limparTela();
            System.out.println(
                "Nao foi possivel registrar a solicitacao: " + e.getMessage()
            );
        } catch (Exception e) {
            limparTela();
            System.out.println(
                "Ocorreu um erro ao processar sua solicitacao. Tente novamente."
            );
        }
    }

    private void acompanharSolicitacao() {
        exibirCabecalhoAcao("Acompanhar solicitacao");
        String protocolo = lerTexto("Informe o protocolo: ");
        Optional<Solicitacao> solicitacao =
            servicoSolicitacoes.buscarPorProtocolo(protocolo);
        limparTela();
        if (solicitacao.isEmpty()) {
            System.out.println("Solicitacao nao encontrada.");
            return;
        }
        exibirResumoSolicitacao(solicitacao.get());
    }

    private void verHistoricoCompleto() {
        exibirCabecalhoAcao("Historico completo");
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
                "Nao foi possivel gerar o historico: " + e.getMessage()
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
            String comentario = lerTexto("Comentario obrigatorio: ");
            limparTela();

            servicoSolicitacoes.atualizarStatus(
                usuarioLogado,
                protocolo,
                novoStatus,
                comentario
            );
            System.out.println("Status atualizado com sucesso.");
        } catch (IllegalArgumentException e) {
            limparTela();
            System.out.println("Atualizacao cancelada: " + e.getMessage());
        } catch (IllegalStateException e) {
            limparTela();
            System.out.println("Transicao invalida: " + e.getMessage());
            System.out.println(
                "Tentativa registrada no console para auditoria."
            );
        } catch (Exception e) {
            limparTela();
            System.out.println("Ocorreu um erro ao atualizar a solicitacao.");
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
                limparTela();
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
        exibirListaSolicitacoes("Solicitacoes filtradas", filtradas);
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
        System.out.println("Solicitacao registrada com sucesso.");
        System.out.println("Protocolo: " + solicitacao.getProtocolo());
        System.out.println(
            "Prazo alvo: " + solicitacao.getPrazoAlvo().format(FORMATADOR_DATA)
        );
        System.out.println("Status: " + solicitacao.getStatus());
        System.out.println("Modo: " + (solicitacao.isAnonimo() ? "ANONIMO" : "IDENTIFICADO"));
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
        System.out.println("Localizacao: " + solicitacao.getLocalizacao());
        System.out.println(
            "Prazo em dia: " + (solicitacao.estaNoPrazo() ? "SIM" : "NAO")
        );
        System.out.println("Descricao: " + solicitacao.getDescricao());
        exibirHistoricoResumido(solicitacao.getHistorico());
        System.out.println(LINHA);
    }

    private void exibirHistoricoResumido(List<HistoricoStatus> historico) {
        if (historico.isEmpty()) {
            System.out.println("Historico: nenhuma movimentacao registrada.");
            return;
        }
        HistoricoStatus ultimo = historico.get(historico.size() - 1);
        System.out.println(
            "Ultima movimentacao: " +
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
            System.out.println("Nenhuma solicitacao encontrada.");
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

    private void exibirCabecalhoAcao(String titulo) {
        limparTela();
        exibirCabecalho(titulo);
    }

    private void aguardarContinuacao() {
        System.out.println();
        System.out.print(MENSAGEM_CONTINUAR);
        scanner.nextLine();
    }

    private String lerTexto(String mensagem) {
        System.out.print(mensagem);
        String valor = scanner.nextLine();
        return valor == null ? ENTRADA_VAZIA : valor.trim();
    }

    private boolean lerConfirmacao(String mensagem) {
        String resposta = lerTexto(mensagem);
        if (
            "S".equalsIgnoreCase(resposta) ||
            "SIM".equalsIgnoreCase(resposta)
        ) {
            return true;
        }
        if (
            "N".equalsIgnoreCase(resposta) ||
            "NAO".equalsIgnoreCase(resposta)
        ) {
            return false;
        }
        throw new IllegalArgumentException("Resposta invalida. Use S ou N.");
    }

    private TipoUsuario lerTipoUsuario(String mensagem) {
        String entrada = lerTexto(mensagem);
        if ("1".equals(entrada) || "CIDADAO".equalsIgnoreCase(entrada)) {
            return TipoUsuario.CIDADAO;
        }
        if ("2".equals(entrada) || "SERVIDOR".equalsIgnoreCase(entrada)) {
            return TipoUsuario.SERVIDOR;
        }
        throw new IllegalArgumentException(
            "Perfil invalido. Escolha cidadao ou servidor."
        );
    }

    private Prioridade lerPrioridade(String mensagem) {
        String entrada = lerTexto(mensagem);
        try {
            return Prioridade.valueOf(entrada.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Prioridade invalida. Use uma das opcoes " +
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
            "Categoria invalida. Use uma das opcoes " +
                categoriasDisponiveis() +
                "."
        );
    }

    private StatusSolicitacao lerStatus(String mensagem) {
        String entrada = lerTexto(mensagem);
        if (entrada.isBlank()) {
            throw new IllegalArgumentException(
                "Status nao informado. Informe um status para continuar."
            );
        }
        try {
            return StatusSolicitacao.valueOf(entrada.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Status invalido. Use uma das opcoes " +
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

    private void exibirOpcoesIniciais() {
        OpcaoInicial[] opcoes = OpcaoInicial.values();
        int i = 0;

        while (i < opcoes.length) {
            System.out.println(
                "  [" + opcoes[i].getCodigo() + "] " + opcoes[i].getDescricao()
            );
            i++;
        }
    }

    private void exibirOpcoesCidadao() {
        OpcaoCidadao[] opcoes = OpcaoCidadao.values();
        int i = 0;

        while (i < opcoes.length) {
            System.out.println(
                "  [" + opcoes[i].getCodigo() + "] " + opcoes[i].getDescricao()
            );
            i++;
        }
    }

    private void exibirOpcoesServidor() {
        OpcaoServidor[] opcoes = OpcaoServidor.values();
        int i = 0;

        while (i < opcoes.length) {
            System.out.println(
                "  [" + opcoes[i].getCodigo() + "] " + opcoes[i].getDescricao()
            );
            i++;
        }
    }

    private String descreverTipoUsuario(Usuario usuario) {
        if (usuarioLogadoEhServidor(usuario)) {
            return "SERVIDOR";
        }
        return "CIDADAO";
    }

    private boolean usuarioLogadoEhServidor() {
        return usuarioLogadoEhServidor(usuarioLogado);
    }

    private boolean usuarioLogadoEhServidor(Usuario usuario) {
        return servicoUsuarios.isServidor(usuario);
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
}
