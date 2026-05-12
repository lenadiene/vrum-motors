package br.com.vrum.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_fabrica")
@DiscriminatorValue("ADMIN_FABRICA")
public class AdminFabrica extends Usuario {

    public AdminFabrica() {
        setPerfil(PerfilUsuario.ADMIN_FABRICA);
    }
}
