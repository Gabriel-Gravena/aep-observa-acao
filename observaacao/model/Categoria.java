package observaacao.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import observaacao.model.enums.TipoCategoria;

public class Categoria {

    public static final Categoria ILUMINACAO = new Categoria(TipoCategoria.ILUMINACAO);
    public static final Categoria BURACO_VIA = new Categoria(TipoCategoria.BURACO_VIA);
    public static final Categoria LIMPEZA_PUBLICA = new Categoria(TipoCategoria.LIMPEZA_PUBLICA);
    public static final Categoria SAUDE = new Categoria(TipoCategoria.SAUDE);
    public static final Categoria SEGURANCA_ESCOLAR = new Categoria(TipoCategoria.SEGURANCA_ESCOLAR);
    public static final Categoria ZELADORIA = new Categoria(TipoCategoria.ZELADORIA);
    public static final Categoria DENUNCIA_IRREGULARIDADE = new Categoria(TipoCategoria.DENUNCIA_IRREGULARIDADE);
    public static final Categoria ASSEDIO = new Categoria(TipoCategoria.ASSEDIO);
    public static final Categoria OUTROS = new Categoria(TipoCategoria.OUTROS);

    private static final List<Categoria> CATEGORIAS = Collections.unmodifiableList(
        Arrays.asList(
            ILUMINACAO,
            BURACO_VIA,
            LIMPEZA_PUBLICA,
            SAUDE,
            SEGURANCA_ESCOLAR,
            ZELADORIA,
            DENUNCIA_IRREGULARIDADE,
            ASSEDIO,
            OUTROS
        )
    );

    private final TipoCategoria tipo;

    private Categoria(TipoCategoria tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da categoria é obrigatório.");
        }
        this.tipo = tipo;
    }

    public static List<Categoria> todas() {
        return CATEGORIAS;
    }

    public static Optional<Categoria> porId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        String idProcurado = normalizarChave(id);

        for (Categoria categoria : CATEGORIAS) {
            if (
                normalizarChave(categoria.getId()).equals(idProcurado) ||
                normalizarChave(categoria.getNome()).equals(idProcurado)
            ) {
                return Optional.of(categoria);
            }
        }

        return Optional.empty();
    }

    public String getId() {
        return tipo.getId();
    }

    public String getNome() {
        return tipo.getNome();
    }

    public String getDescricao() {
        return tipo.getDescricao();
    }

    public TipoCategoria getTipo() {
        return tipo;
    }

    private static String normalizarChave(String valor) {
        String semAcento = Normalizer
            .normalize(valor, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
        return semAcento
            .trim()
            .toLowerCase(Locale.ROOT)
            .replace('-', '_')
            .replace(' ', '_');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Categoria)) {
            return false;
        }
        Categoria categoria = (Categoria) o;
        return tipo == categoria.tipo;
    }

    @Override
    public int hashCode() {
        return tipo.hashCode();
    }

    @Override
    public String toString() {
        return getId();
    }
}
