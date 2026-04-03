package observaacao.model;

import java.util.UUID;
import observaacao.model.enums.TipoUsuario;

public class Usuario {

    private static final String NOME_ANONIMO = "Anônimo";

    private final String id;
    private final String nome;
    private final String contato;
    private final TipoUsuario tipo;
    private final boolean anonimo;

    public Usuario(
        String nome,
        String contato,
        TipoUsuario tipo,
        boolean anonimo
    ) {
        validarTipo(tipo);
        this.id = UUID.randomUUID().toString();
        this.anonimo = anonimo;
        this.nome = definirNome(nome, anonimo);
        this.contato = definirContato(contato, anonimo);
        this.tipo = tipo;
    }

    public Usuario(
        String id,
        String nome,
        String contato,
        TipoUsuario tipo,
        boolean anonimo
    ) {
        validarId(id);
        validarTipo(tipo);
        this.id = id;
        this.anonimo = anonimo;
        this.nome = definirNome(nome, anonimo);
        this.contato = definirContato(contato, anonimo);
        this.tipo = tipo;
    }

    private void validarId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "O id do usuário é obrigatório."
            );
        }
    }

    private void validarTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException(
                "O tipo de usuário é obrigatório."
            );
        }
    }

    private String definirNome(String nomeInformado, boolean anonimo) {
        if (anonimo) {
            return NOME_ANONIMO;
        }

        if (nomeInformado == null || nomeInformado.isBlank()) {
            throw new IllegalArgumentException(
                "O nome do usuário é obrigatório quando o registro não é anônimo."
            );
        }

        return nomeInformado.trim();
    }

    private String definirContato(String contatoInformado, boolean anonimo) {
        if (anonimo) {
            return null;
        }

        if (contatoInformado == null || contatoInformado.isBlank()) {
            return null;
        }

        return contatoInformado.trim();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getContato() {
        return contato;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public boolean isAnonimo() {
        return anonimo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario)) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        if (id == null) {
            return usuario.id == null;
        }
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return (
            "Usuario{" +
            "id='" +
            id +
            '\'' +
            ", nome='" +
            nome +
            '\'' +
            ", contato='" +
            contato +
            '\'' +
            ", tipo=" +
            tipo +
            ", anonimo=" +
            anonimo +
            '}'
        );
    }
}
