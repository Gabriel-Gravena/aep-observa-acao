package observaacao.service;

import java.util.ArrayList;
import java.util.List;
import observaacao.model.Solicitacao;
import observaacao.model.enums.StatusSolicitacao;
import observaacao.repository.RepositorioSolicitacoes;

public class FilaAtendimento {

    private final RepositorioSolicitacoes repositorio;

    public FilaAtendimento(RepositorioSolicitacoes repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("O repositório é obrigatório.");
        }
        this.repositorio = repositorio;
    }

    public void adicionar(Solicitacao solicitacao) {
        validarSolicitacao(solicitacao);
        repositorio.salvar(solicitacao);
    }

    public List<Solicitacao> obterFilaPorPrioridade() {
        List<Solicitacao> fila = new ArrayList<Solicitacao>();
        List<Solicitacao> todas = repositorio.listarTodas();

        for (int i = 0; i < todas.size(); i++) {
            Solicitacao solicitacao = todas.get(i);
            if (estaAbertaParaAtendimento(solicitacao)) {
                fila.add(solicitacao);
            }
        }

        ordenarFila(fila);
        return fila;
    }

    public List<Solicitacao> obterAtrasadas() {
        List<Solicitacao> atrasadas = repositorio.listarAtrasadas();
        ordenarFila(atrasadas);
        return atrasadas;
    }

    public int totalAbertos() {
        int total = 0;
        List<Solicitacao> todas = repositorio.listarTodas();

        for (int i = 0; i < todas.size(); i++) {
            if (estaAbertaParaAtendimento(todas.get(i))) {
                total++;
            }
        }

        return total;
    }

    private void validarSolicitacao(Solicitacao solicitacao) {
        if (solicitacao == null) {
            throw new IllegalArgumentException("A solicitação é obrigatória.");
        }
    }

    private boolean estaAbertaParaAtendimento(Solicitacao solicitacao) {
        return solicitacao.getStatus() == StatusSolicitacao.ABERTO;
    }

    private void ordenarFila(List<Solicitacao> fila) {
        for (int i = 1; i < fila.size(); i++) {
            Solicitacao atual = fila.get(i);
            int j = i - 1;

            while (j >= 0 && deveVirAntes(atual, fila.get(j))) {
                fila.set(j + 1, fila.get(j));
                j--;
            }

            fila.set(j + 1, atual);
        }
    }

    private boolean deveVirAntes(Solicitacao atual, Solicitacao comparada) {
        if (
            atual.getPrioridade().ordinal() >
            comparada.getPrioridade().ordinal()
        ) {
            return true;
        }

        if (
            atual.getPrioridade().ordinal() <
            comparada.getPrioridade().ordinal()
        ) {
            return false;
        }

        return atual.getDataAbertura().isBefore(comparada.getDataAbertura());
    }
}
