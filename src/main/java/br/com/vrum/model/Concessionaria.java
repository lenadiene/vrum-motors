package br.com.vrum.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "concessionarias")
public class Concessionaria implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String estado;

    @Column(length = 500)
    private String endereco;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "concessionaria", fetch = FetchType.LAZY)
    private List<Vendedor> vendedores;

    @OneToMany(mappedBy = "concessionaria", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    @OneToOne(mappedBy = "concessionaria")
    private Gerente gerente;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public List<Vendedor> getVendedores() { return vendedores; }
    public void setVendedores(List<Vendedor> vendedores) { this.vendedores = vendedores; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }

    public Gerente getGerente() { return gerente; }
    public void setGerente(Gerente gerente) { this.gerente = gerente; }

    @Override
    public String toString() {
        return cidade + " - " + estado;
    }
}
