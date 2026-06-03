package br.com.vrum.service;

import br.com.vrum.dao.UsuarioDAO;
import br.com.vrum.model.*;
import br.com.vrum.util.SenhaUtil;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public Usuario autenticar(String email, String senha) {
        if (email == null || senha == null) return null;
        return usuarioDAO.autenticar(email.trim().toLowerCase(), senha);
    }

    public Usuario salvarUsuario(Usuario usuario, String senhaPlana) {
        // Normaliza antes de qualquer checagem para comparação consistente
        String emailNorm = usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(emailNorm);

        if (usuarioDAO.emailJaExiste(emailNorm, usuario.getId())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        String tel = usuario.getTelefone();
        if (tel != null && !tel.isEmpty() && usuarioDAO.telefoneJaExiste(tel, usuario.getId())) {
            throw new IllegalArgumentException("Telefone já cadastrado.");
        }

        String cpf = usuario.getCpf();
        if (cpf != null && !cpf.isEmpty() && usuarioDAO.cpfJaExiste(cpf, usuario.getId())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (senhaPlana != null && !senhaPlana.isEmpty()) {
            usuario.setSenha(SenhaUtil.hashSenha(senhaPlana));
        }
        if (usuario.getId() == null) {
            return usuarioDAO.salvar(usuario);
        } else {
            return usuarioDAO.atualizar(usuario);
        }
    }

    public void alterarSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (!SenhaUtil.verificarSenha(senhaAtual, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        usuario.setSenha(SenhaUtil.hashSenha(novaSenha));
        usuarioDAO.atualizar(usuario);
    }

    public void inativar(Usuario usuario) {
        usuario.setAtivo(false);
        usuarioDAO.atualizar(usuario);
    }

    public void reativar(Usuario usuario) {
        usuario.setAtivo(true);
        usuarioDAO.atualizar(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public List<Usuario> listarPorPerfil(PerfilUsuario perfil) {
        return usuarioDAO.listarPorPerfil(perfil);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }

    public boolean cpfJaExiste(String cpf, Long idExcluir) {
        return usuarioDAO.cpfJaExiste(cpf, idExcluir);
    }

    public boolean telefoneJaExiste(String telefone, Long idExcluir) {
        return usuarioDAO.telefoneJaExiste(telefone, idExcluir);
    }
}
