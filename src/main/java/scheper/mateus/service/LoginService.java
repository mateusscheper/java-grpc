package scheper.mateus.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import scheper.mateus.entity.Usuario;
import scheper.mateus.exception.BusinessException;
import scheper.mateus.grpc.LoginRequest;
import scheper.mateus.repository.UsuarioRepository;

import static scheper.mateus.utils.ConstantUtils.EMAIL_SENHA_INVALIDOS;

@Service
public class LoginService {


    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail());
        if (usuario == null) {
            throw new BusinessException(EMAIL_SENHA_INVALIDOS);
        }

        if (passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            return jwtService.gerarTokenLogin(usuario);
        } else
            throw new BusinessException(EMAIL_SENHA_INVALIDOS);
    }
}
