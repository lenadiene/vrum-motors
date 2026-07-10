package br.com.vrum.model;

public enum StatusPedido {
    AGUARDANDO_ATENDIMENTO("Aguardando Atendimento"),
    EM_NEGOCIACAO("Em Negociação"),
    AGUARDANDO_FABRICACAO("Aguardando Fabricação"),
    EM_FABRICACAO("Em Fabricação"),
    FABRICADO("Fabricado"),
    ENVIADO_CIDADE("Enviado para a Cidade"),
    PRONTO_ENTREGA("Pronto para Entrega"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
