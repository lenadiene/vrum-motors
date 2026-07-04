package br.com.vrum.model;

public enum TipoConfiguracaoVeiculo {

    MOTOR("Motor"),
    COMBUSTIVEL("Combustível"),
    TRANSMISSAO("Transmissão"),
    TRACAO("Tração"),
    ANO("Ano"),
    MARCA("Marca");

    private final String descricao;

    TipoConfiguracaoVeiculo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}
