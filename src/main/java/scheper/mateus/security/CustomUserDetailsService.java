package scheper.mateus.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import scheper.mateus.entity.Usuario;
import scheper.mateus.exception.BusinessException;
import scheper.mateus.repository.UsuarioRepository;

import static scheper.mateus.utils.ConstantUtils.USUARIO_NAO_ENCONTRADO;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email);
        String emailRetorno;
        String senhaRetorno;
        String[] roles;

        if (usuario == null) {
            usuario = usuarioRepository.findByEmail(email);
            if (usuario == null) {
                throw new BusinessException(USUARIO_NAO_ENCONTRADO);
            }
        }

        emailRetorno = usuario.getEmail();
        senhaRetorno = usuario.getSenha();
        roles = usuario.getRolesAsString();

        return User
                .builder()
                .username(emailRetorno)
                .password(senhaRetorno)
                .roles(roles)
                .build();
    }
}
