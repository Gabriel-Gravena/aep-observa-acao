package observaacao.service;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

public class GeradorProtocolo {

    private static final String PREFIXO = "OBS";
    private static final String MASCARA_SEQUENCIAL = "%06d";

    private final AtomicInteger sequencial;

    public GeradorProtocolo() {
        this(0);
    }

    public GeradorProtocolo(int valorInicial) {
        if (valorInicial < 0) {
            throw new IllegalArgumentException("O valor inicial do sequencial não pode ser negativo.");
        }
        this.sequencial = new AtomicInteger(valorInicial);
    }

    public String gerar() {
        int anoAtual = Year.now().getValue();
        int numero = sequencial.incrementAndGet();
        return PREFIXO + "-" + anoAtual + "-" + formatarSequencial(numero);
    }

    private String formatarSequencial(int numero) {
        return String.format(MASCARA_SEQUENCIAL, numero);
    }
}
