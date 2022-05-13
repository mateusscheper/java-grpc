package scheper.mateus.service;

import io.github.majusko.grpc.jwt.service.JwtService;
import io.github.majusko.grpc.jwt.service.dto.JwtData;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scheper.mateus.entity.Usuario;
import scheper.mateus.exception.BusinessException;
import grpc.LoginRequest;
import scheper.mateus.repository.UsuarioRepository;

import static scheper.mateus.utils.ConstantUtils.EMAIL_SENHA_INVALIDOS;

@Service
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final io.github.majusko.grpc.jwt.service.JwtService jwtService;

    public LoginService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail());
        if (usuario == null) {
            throw new BusinessException(EMAIL_SENHA_INVALIDOS);
        }

        if (passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            JwtData jwtData = new JwtData(usuario.getIdUsuario().toString(), usuario.getRolesAsHashSet());
            return jwtService.generate(jwtData);
        } else
            throw new BusinessException(EMAIL_SENHA_INVALIDOS);
    }
}
