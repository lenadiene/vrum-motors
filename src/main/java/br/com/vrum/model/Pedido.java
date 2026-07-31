package br.com.vrum.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String numeroPedido;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "concessionaria_id", nullable = false)
    private Concessionaria concessionaria;

    @ManyToOne
    @JoinColumn(name = "vendedor_id")
    private Vendedor vendedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Column(name = "data_pedido", nullable = false)
    private LocalDateTime dataPedido;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "prazo_fabricacao")
    private LocalDate prazoFabricacao;

    @Column(name = "prazo_entrega")
    private LocalDate prazoEntrega;

    @Column(name = "data_retirada")
    private LocalDate dataRetirada;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "observacoes_fabrica", columnDefinition = "TEXT")
    private String observacoesFabrica;

    @Column(name = "cor_escolhida", length = 50)
    private String corEscolhida;

    @Column(name = "forma_pagamento", length = 100)
    private String formaPagamento;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Anexo> anexos;

    @PrePersist
    public void prePersist() {
        if (this.dataPedido == null) this.dataPedido = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        if (this.status == null) this.status = StatusPedido.AGUARDANDO_ATENDIMENTO;
        if (this.numeroPedido == null) this.numeroPedido = gerarNumeroPedido();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    private String gerarNumeroPedido() {
        return "VRM" + System.currentTimeMillis();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Veiculo getVeiculo() { return veiculo; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    public Concessionaria getConcessionaria() { return concessionaria; }
    public void setConcessionaria(Concessionaria concessionaria) { this.concessionaria = concessionaria; }

    public Vendedor getVendedor() { return vendedor; }
    public void setVendedor(Vendedor vendedor) { this.vendedor = vendedor; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDate getPrazoFabricacao() { return prazoFabricacao; }
    public void setPrazoFabricacao(LocalDate prazoFabricacao) { this.prazoFabricacao = prazoFabricacao; }

    public LocalDate getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(LocalDate prazoEntrega) { this.prazoEntrega = prazoEntrega; }

    public LocalDate getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDate dataRetirada) { this.dataRetirada = dataRetirada; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getObservacoesFabrica() { return observacoesFabrica; }
    public void setObservacoesFabrica(String observacoesFabrica) { this.observacoesFabrica = observacoesFabrica; }

    public String getCorEscolhida() { return corEscolhida; }
    public void setCorEscolhida(String corEscolhida) { this.corEscolhida = corEscolhida; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public List<Anexo> getAnexos() { return anexos; }
    public void setAnexos(List<Anexo> anexos) { this.anexos = anexos; }
}
