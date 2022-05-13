package scheper.mateus.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scheper.mateus.builder.UsuarioBuilder;
import scheper.mateus.entity.Role;
import scheper.mateus.entity.Usuario;
import scheper.mateus.exception.BusinessException;
import grpc.ListaUsuarioResponse;
import grpc.NovoUsuarioRequest;
import scheper.mateus.repository.RoleRepository;
import scheper.mateus.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void criarUsuario(NovoUsuarioRequest novoUsuarioDTO) {
        String senha = passwordEncoder.encode(novoUsuarioDTO.getSenha());
        Usuario usuario = UsuarioBuilder.fromGrpc(novoUsuarioDTO, senha);
        Role role = obterRole(novoUsuarioDTO);
        usuario.getRoles().add(role);

        usuarioRepository.save(usuario);
    }

    private Role obterRole(NovoUsuarioRequest novoUsuarioDTO) {
        long idRole = novoUsuarioDTO.getIdRole();
        if (idRole == 0) {
            throw new BusinessException("Role obrigatória.");
        }

        Optional<Role> roleOptional = roleRepository.findById(idRole);
        if (roleOptional.isEmpty()) {
            throw new BusinessException("Role inválida.");
        }

        return roleOptional.get();
    }

    public ListaUsuarioResponse listarUsuarios(grpc.Usuario filtro) {
        return usuarioRepository.listarUsuarios(filtro);
    }
}
