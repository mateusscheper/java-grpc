package scheper.mateus.config;

import grpc.UsuarioServiceGrpc;
import org.lognet.springboot.grpc.security.AuthenticationSchemeSelector;
import org.lognet.springboot.grpc.security.GrpcSecurity;
import org.lognet.springboot.grpc.security.GrpcSecurityConfigurerAdapter;
import org.lognet.springboot.grpc.security.jwt.JwtAuthProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

@Configuration
public class GrpcSecurityConfiguration extends GrpcSecurityConfigurerAdapter {
    @Override
    public void configure(GrpcSecurity builder) throws Exception {
        builder.authorizeRequests()
                .anyMethod().authenticated()
                .and()
                .authenticationSchemeSelector(new AuthenticationSchemeSelector() {
                    @Override
                    public Optional<Authentication> getAuthScheme(CharSequence authorization) {
                        return new MyAuthenticationObject();
                    }
                })
                .authenticationProvider(new AuthenticationProvider() {
                    @Override
                    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                        MyAuthenticationObject myAuth= (MyAuthenticationObject)authentication;
                        //validate myAuth
                        return MyValidatedAuthenticationObject(withAuthorities);
                    }

                    @Override
                    public boolean supports(Class<?> authentication) {
                        return MyAuthenticationObject.class.isInstance(authentication);
                    }
                });
    }
}
