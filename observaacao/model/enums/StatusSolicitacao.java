package observaacao.model.enums;

public enum StatusSolicitacao {
    ABERTO,
    TRIAGEM,
    EM_EXECUCAO,
    RESOLVIDO,
    ENCERRADO;

    public boolean isTransicaoValida(StatusSolicitacao novo) {
        if (novo == null) {
            return false;
        }

        switch (this) {
            case ABERTO:
                return novo == TRIAGEM;
            case TRIAGEM:
                return novo == EM_EXECUCAO;
            case EM_EXECUCAO:
                return novo == RESOLVIDO;
            case RESOLVIDO:
                return novo == ENCERRADO;
            default:
                return false;
        }
    }
}
