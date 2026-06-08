package br.com.vrum.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "configuracoes_veiculo",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "valor"}))
public class ConfiguracaoVeiculo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConfiguracaoVeiculo tipo;

    @Column(nullable = false, length = 100)
    private String valor;

    @Column(nullable = false)
    private int ordem = 0;

    public Long getId()                           { return id; }
    public void setId(Long id)                    { this.id = id; }

    public TipoConfiguracaoVeiculo getTipo()               { return tipo; }
    public void setTipo(TipoConfiguracaoVeiculo tipo)       { this.tipo = tipo; }

    public String getValor()                      { return valor; }
    public void setValor(String valor)            { this.valor = valor; }

    public int getOrdem()                         { return ordem; }
    public void setOrdem(int ordem)               { this.ordem = ordem; }
}
