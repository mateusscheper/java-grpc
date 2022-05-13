package scheper.mateus.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import scheper.mateus.entity.Role;
import scheper.mateus.entity.Usuario;
import scheper.mateus.exception.BusinessException;
import scheper.mateus.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static scheper.mateus.utils.ConstantUtils.USUARIO_NAO_ENCONTRADO;


@Service
public class JwtCustomService {

    @Value(value = "${jwt.security.expiracao}")
    private String expiracao;

    @Value(value = "${jwt.security.chave}")
    private String chaveAssinatura;

    private final UsuarioRepository usuarioRepository;

    List<String> tokenBlocklist = new ArrayList<>();

    public JwtCustomService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public String gerarTokenLogin(Usuario usuario) {
        return buildJwt(usuario);
    }

    private String buildJwt(Usuario usuario) {
        String subject = usuario.getEmail();
        Date expiration = obterDataExpiracao();
        String[] roles = mapRoles(usuario);
        Map<String, Object> claims = obterClaimsDoUsuario(usuario, roles);

        return Jwts.builder()
                .setSubject(subject)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, chaveAssinatura)
                .compact();
    }

    private Map<String, Object> obterClaimsDoUsuario(Usuario usuario, String[] roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, usuario.getEmail());
        claims.put("uid", usuario.getIdUsuario());
        claims.put("nmame", usuario.getNome());
        claims.put("roles", roles);
        return claims;
    }

    private String[] mapRoles(Usuario usuario) {
        return usuario.getRoles()
                .stream()
                .map(Role::getNome)
                .toArray(String[]::new);
    }

    private Date obterDataExpiracao() {
        long tempoExpiracao = Long.parseLong(expiracao);
        LocalDateTime dataHoraExpiracao = LocalDateTime.now().plusMinutes(tempoExpiracao);
        return Date.from(dataHoraExpiracao.atZone(ZoneId.systemDefault()).toInstant());
    }

    public Claims obterClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(chaveAssinatura)
                .parseClaimsJws(token)
                .getBody();
    }

    public String obterEmailByToken(String token) {
        return Jwts
                .parser()
                .setSigningKey(chaveAssinatura)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Usuario obterUsuarioDoRequest(HttpServletRequest request) {
        String email = obterEmailDoRequest(request);

        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new BusinessException(USUARIO_NAO_ENCONTRADO);
        }

        return usuario;
    }

    public String obterEmailDoRequest(HttpServletRequest request) {
        String token = obterTokenDoRequest(request);
        String email = obterEmailByToken(token);
        if (ObjectUtils.isEmpty(email))
            throw new BusinessException(USUARIO_NAO_ENCONTRADO);
        return email;
    }

    private String obterTokenDoRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (!token.startsWith("Bearer "))
            throw new BusinessException(USUARIO_NAO_ENCONTRADO);
        return token.replace("Bearer ", "");
    }

    public boolean isTokenValido(String token) {
        if (ObjectUtils.isEmpty(token) || tokenBlocklist.contains(token))
            return false;

        try {
            Claims claims = obterClaims(token);
            Date dataExpiracao = claims.getExpiration();
            LocalDateTime data = dataExpiracao.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            return !LocalDateTime.now().isAfter(data);
        } catch (Exception e) {
            return false;
        }
    }

    public void bloquearTokenJwt(HttpServletRequest request) {
        String token = obterTokenDoRequest(request);
        if (!StringUtils.isBlank(token)) {
            bloquearTokenJwt(token);
        }
    }

    public void bloquearTokenJwt(String token) {
        if (!tokenBlocklist.contains(token))
            tokenBlocklist.add(token);
    }
}
