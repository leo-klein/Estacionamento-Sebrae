package br.gov.sp.etec.estacionamento.service;

import br.gov.sp.etec.estacionamento.model.Usuario;
import java.util.List;

public interface UsuarioService {
    String cadastrarUsuario(Usuario usuario);
    List<Usuario> listarUsuario();
    String atualizarUsuario(Usuario usuario);
    String deletarUsuario(Long id);
    void cadastroUsuario(Usuario usuario);
    Usuario buscarPorEmail(String email);
    boolean autenticar(String email, String senha);
}