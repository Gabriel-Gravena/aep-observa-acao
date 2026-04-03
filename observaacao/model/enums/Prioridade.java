package observaacao.model.enums;

import java.time.LocalDate;

public enum Prioridade {
    BAIXA(30),
    MEDIA(15),
    ALTA(7),
    CRITICA(2);

    private final int slaDias;

    Prioridade(int slaDias) {
        this.slaDias = slaDias;
    }

    public int getSlaDias() {
        return slaDias;
    }

    public LocalDate calcularPrazo(LocalDate dataAbertura) {
        if (dataAbertura == null) {
            throw new IllegalArgumentException("A data de abertura é obrigatória.");
        }
        return dataAbertura.plusDays(slaDias);
    }
}
