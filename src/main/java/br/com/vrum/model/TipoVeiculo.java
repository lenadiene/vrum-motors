package br.com.vrum.model;

public enum TipoVeiculo {
    DISPONIVEL("Disponível"),
    LANCAMENTO("Lançamento"),
    SOB_ENCOMENDA("Sob Encomenda");

    private final String descricao;

    TipoVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
