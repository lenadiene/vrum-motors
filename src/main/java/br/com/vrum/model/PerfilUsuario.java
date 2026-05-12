package br.com.vrum.model;

public enum PerfilUsuario {
    ADMIN_EMPRESA("Administrador da Empresa"),
    GERENTE("Gerente da Concessionária"),
    ADMIN_FABRICA("Administrador da Fábrica"),
    VENDEDOR("Vendedor"),
    CLIENTE("Cliente");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
