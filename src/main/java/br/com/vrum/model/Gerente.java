package br.com.vrum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gerentes")
@DiscriminatorValue("GERENTE")
public class Gerente extends Usuario {

    @OneToOne
    @JoinColumn(name = "concessionaria_id", unique = true)
    private Concessionaria concessionaria;

    public Gerente() {
        setPerfil(PerfilUsuario.GERENTE);
    }

    public Concessionaria getConcessionaria() { return concessionaria; }
    public void setConcessionaria(Concessionaria concessionaria) { this.concessionaria = concessionaria; }
}
