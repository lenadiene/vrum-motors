package br.com.vrum.model;

import jakarta.persistence.*;

// ========== AdminEmpresa ==========
@Entity
@Table(name = "admin_empresa")
@DiscriminatorValue("ADMIN_EMPRESA")
public class AdminEmpresa extends Usuario {
    
    public AdminEmpresa() {
        setPerfil(PerfilUsuario.ADMIN_EMPRESA);
    }
}