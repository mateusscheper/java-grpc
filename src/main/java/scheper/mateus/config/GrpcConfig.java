package scheper.mateus.config;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.SSLContextGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.X509CertificateAuthenticationProvider;
import net.devh.boot.grpc.server.security.check.AccessPredicate;
import net.devh.boot.grpc.server.security.check.AccessPredicateVoter;
import net.devh.boot.grpc.server.security.check.GrpcSecurityMetadataSource;
import net.devh.boot.grpc.server.security.check.ManualGrpcSecurityMetadataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import scheper.mateus.grpc.UsuarioServiceGrpc;
import scheper.mateus.security.CustomUserDetailsService;
import scheper.mateus.service.JwtService;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class GrpcConfig {

    private final CustomUserDetailsService userDetailsService;

    public GrpcConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @GrpcGlobalServerInterceptor
    GrpcInterceptor grpcInterceptor() {
        return new GrpcInterceptor();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    GrpcSecurityMetadataSource grpcSecurityMetadataSource() {
        final ManualGrpcSecurityMetadataSource source = new ManualGrpcSecurityMetadataSource();
        source.set(UsuarioServiceGrpc.METHOD_CRIAR_USUARIO, AccessPredicate.hasRole("ROLE_ADMIN"));
        /*source.set(MyServiceGrpc.getMethodB(), AccessPredicate.hasRole("ROLE_USER"));
        source.set(MyServiceGrpc.getMethodC(), AccessPredicate.hasAllRoles("ROLE_FOO", "ROLE_BAR"));
        source.set(MyServiceGrpc.getMethodD(), auth -> "admin".equals(auth.getName()));*/
        source.setDefault(AccessPredicate.permitAll());
        return source;
    }

    @Bean
    AccessDecisionManager accessDecisionManager() {
        final List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        voters.add(new AccessPredicateVoter());
        return new UnanimousBased(voters);
    }

    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        // The actual token class is dependent on your spring-security library (OAuth2/JWT/...)
        readers.add(new BearerAuthenticationReader(accessToken -> new JwtService(accessToken)));
        return new CompositeGrpcAuthenticationReader(readers);    }
/*
    @Bean
    AuthenticationManager authenticationManager() {
        final List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(new X509CertificateAuthenticationProvider(userDetailsService));
        return new ProviderManager(providers);
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        readers.add(new SSLContextGrpcAuthenticationReader());
        return new CompositeGrpcAuthenticationReader(readers);
    }*/
}
