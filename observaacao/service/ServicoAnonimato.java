package observaacao.service;

import observaacao.model.Categoria;
import observaacao.model.Usuario;
import observaacao.model.enums.TipoUsuario;

public class ServicoAnonimato {

    private static final String IDENTIDADE_PROTEGIDA = "Solicitante: [identidade protegida]";
    private static final String CONTATO_NAO_INFORMADO = "não informado";
    private static final int TAMANHO_MINIMO_MASCARA = 4;

    public boolean deveSerAnonimo(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("A categoria é obrigatória.");
        }

        return categoria == Categoria.ASSEDIO
                || categoria == Categoria.DENUNCIA_IRREGULARIDADE;
    }

    public Usuario criarUsuarioAnonimo() {
        return new Usuario("Anônimo", null, TipoUsuario.CIDADAO, true);
    }

    public String protegerDados(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }

        if (usuario.isAnonimo()) {
            return IDENTIDADE_PROTEGIDA;
        }

        return "Solicitante: " + usuario.getNome() + " | Contato: " + mascararContato(usuario.getContato());
    }

    private String mascararContato(String contato) {
        if (contato == null || contato.isBlank()) {
            return CONTATO_NAO_INFORMADO;
        }

        String contatoLimpo = contato.trim();
        if (contatoLimpo.length() <= TAMANHO_MINIMO_MASCARA) {
            return repetirMascara(contatoLimpo.length());
        }

        String parteVisivel = contatoLimpo.substring(contatoLimpo.length() - TAMANHO_MINIMO_MASCARA);
        return repetirMascara(contatoLimpo.length() - TAMANHO_MINIMO_MASCARA) + parteVisivel;
    }

    private String repetirMascara(int quantidade) {
        StringBuilder mascara = new StringBuilder();
        for (int i = 0; i < quantidade; i++) {
            mascara.append('*');
        }
        return mascara.toString();
    }
}
