package br.com.vrum.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "veiculos")
public class Veiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @Column(precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(length = 30)
    private String motor;

    @Column(length = 20)
    private String potencia;

    @Column(length = 20)
    private String torque;

    @Column(length = 20)
    private String transmissao;

    @Column(length = 20)
    private String combustivel;

    @Column(length = 20)
    private String tracao;

    @Column(length = 30)
    private String consumo;

    @Column(length = 20)
    private String velocidadeMax;

    @Column(length = 20)
    private String aceleracao;

    @Column(length = 50)
    private String cor;

    @Column(length = 200)
    private String descricao;

    @Column(name = "descricao_longa", columnDefinition = "TEXT")
    private String descricaoLonga;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVeiculo tipo;

    @Column(length = 200)
    private String imagemPrincipal;

    @Column(name = "imagem_interior", length = 200)
    private String imagemInterior;

    @Column(name = "imagem_perfil", length = 200)
    private String imagemPerfil;

    @Column(name = "imagem_traseira", length = 200)
    private String imagemTraseira;

    @Column(nullable = false)
    private boolean disponivel = true;

    @Column(name = "destaque_home")
    private boolean destaqueHome = false;

    @OneToMany(mappedBy = "veiculo", fetch = FetchType.LAZY)
    private List<Pedido> pedidos;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getMotor() { return motor; }
    public void setMotor(String motor) { this.motor = motor; }

    public String getPotencia() { return potencia; }
    public void setPotencia(String potencia) { this.potencia = potencia; }

    public String getTorque() { return torque; }
    public void setTorque(String torque) { this.torque = torque; }

    public String getTransmissao() { return transmissao; }
    public void setTransmissao(String transmissao) { this.transmissao = transmissao; }

    public String getCombustivel() { return combustivel; }
    public void setCombustivel(String combustivel) { this.combustivel = combustivel; }

    public String getTracao() { return tracao; }
    public void setTracao(String tracao) { this.tracao = tracao; }

    public String getConsumo() { return consumo; }
    public void setConsumo(String consumo) { this.consumo = consumo; }

    public String getVelocidadeMax() { return velocidadeMax; }
    public void setVelocidadeMax(String velocidadeMax) { this.velocidadeMax = velocidadeMax; }

    public String getAceleracao() { return aceleracao; }
    public void setAceleracao(String aceleracao) { this.aceleracao = aceleracao; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getDescricaoLonga() { return descricaoLonga; }
    public void setDescricaoLonga(String descricaoLonga) { this.descricaoLonga = descricaoLonga; }

    public TipoVeiculo getTipo() { return tipo; }
    public void setTipo(TipoVeiculo tipo) { this.tipo = tipo; }

    public String getImagemPrincipal() { return imagemPrincipal; }
    public void setImagemPrincipal(String imagemPrincipal) { this.imagemPrincipal = imagemPrincipal; }

    public String getImagemInterior() { return imagemInterior; }
    public void setImagemInterior(String imagemInterior) { this.imagemInterior = imagemInterior; }

    public String getImagemPerfil() { return imagemPerfil; }
    public void setImagemPerfil(String imagemPerfil) { this.imagemPerfil = imagemPerfil; }

    public String getImagemTraseira() { return imagemTraseira; }
    public void setImagemTraseira(String imagemTraseira) { this.imagemTraseira = imagemTraseira; }

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }

    public boolean isDestaqueHome() { return destaqueHome; }
    public void setDestaqueHome(boolean destaqueHome) { this.destaqueHome = destaqueHome; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
}
