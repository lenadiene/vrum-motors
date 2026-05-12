package br.com.vrum.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "vendedores")
@DiscriminatorValue("VENDEDOR")
public class Vendedor extends Usuario {

    @ManyToOne
    @JoinColumn(name = "concessionaria_id")
    private Concessionaria concessionaria;

    @OneToMany(mappedBy = "vendedor", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    public Vendedor() {
        setPerfil(PerfilUsuario.VENDEDOR);
    }

    public Concessionaria getConcessionaria() { return concessionaria; }
    public void setConcessionaria(Concessionaria concessionaria) { this.concessionaria = concessionaria; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
}
