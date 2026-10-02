package br.gov.sp.etec.estacionamento.service;

import br.gov.sp.etec.estacionamento.entity.UsuarioEntity;
import br.gov.sp.etec.estacionamento.model.Usuario;
import br.gov.sp.etec.estacionamento.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    UsuarioRepository repository;

    @Override
    public String cadastrarUsuario(Usuario usuario) {
        UsuarioEntity emailExistente = repository.findByEmail(usuario.getEmail());
        if (emailExistente != null) {
            return "Este e-mail já está cadastrado no sistema!";
        }

        UsuarioEntity cpfExistente = repository.findByCpf(usuario.getCpf());
        if (cpfExistente != null) {
            return "Este CPF já está cadastrado no sistema!";
        }

        UsuarioEntity telefoneExistente = repository.findByTelefone(usuario.getTelefone());
        if (telefoneExistente != null) {
            return "Este telefone já está cadastrado no sistema!";
        }

        UsuarioEntity usuarioEntity = new UsuarioEntity();

        usuarioEntity.setNome(usuario.getNome());
        usuarioEntity.setCpf(usuario.getCpf());
        usuarioEntity.setEmail(usuario.getEmail());
        usuarioEntity.setSenha(usuario.getSenha());
        usuarioEntity.setNascimento(usuario.getNascimento());
        usuarioEntity.setTelefone(usuario.getTelefone());

        repository.save(usuarioEntity);
        return "Usuario cadastrado com sucesso";
    }

    @Override
    public List<Usuario> listarUsuario() {
        return List.of();
    }

    @Override
    public String atualizarUsuario(Usuario usuario) {
        return "";
    }

    @Override
    public String deletarUsuario(Long id) {
        return "";
    }

    @Override
    public void cadastroUsuario(Usuario usuario) {
        cadastrarUsuario(usuario);
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        UsuarioEntity entity = repository.findByEmail(email);

        if (entity == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(entity.getNome());
        usuario.setCpf(entity.getCpf());
        usuario.setEmail(entity.getEmail());
        usuario.setSenha(entity.getSenha());
        usuario.setTelefone(entity.getTelefone());
        usuario.setNascimento(entity.getNascimento());

        return usuario;
    }

    @Override
    public boolean autenticar(String email, String senha) {
        UsuarioEntity entity = repository.findByEmail(email);
        if (entity != null && entity.getSenha().equals(senha)) {
            return true;
        }
        return false;
    }
}