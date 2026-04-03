package observaacao.model.enums;

public enum TipoCategoria {
    ILUMINACAO(
        "Iluminação",
        "Demandas relacionadas à iluminação pública."
    ),
    BURACO_VIA(
        "Buraco em Via",
        "Solicitações sobre buracos, crateras e danos no pavimento."
    ),
    LIMPEZA_PUBLICA(
        "Limpeza Pública",
        "Demandas de coleta, varrição e limpeza urbana."
    ),
    SAUDE("Saúde", "Solicitações relacionadas a serviços e unidades de saúde."),
    SEGURANCA_ESCOLAR(
        "Segurança Escolar",
        "Ocorrências e demandas ligadas à segurança no entorno escolar."
    ),
    ZELADORIA(
        "Zeladoria",
        "Serviços gerais de manutenção e conservação urbana."
    ),
    DENUNCIA_IRREGULARIDADE(
        "Denúncia de Irregularidade",
        "Registro de possíveis irregularidades administrativas ou operacionais."
    ),
    ASSEDIO(
        "Assédio",
        "Registro sigiloso de denúncias relacionadas a assédio."
    ),
    OUTROS(
        "Outros",
        "Categorias diversas não contempladas nas opções principais."
    );

    private final String nome;
    private final String descricao;

    TipoCategoria(String nome, String descricao) {
        this.nome = validarTexto(nome, "nome");
        this.descricao = validarTexto(descricao, "descricao");
    }

    public String getId() {
        return name();
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "O campo " + campo + " é obrigatório."
            );
        }
        return valor.trim();
    }
}
