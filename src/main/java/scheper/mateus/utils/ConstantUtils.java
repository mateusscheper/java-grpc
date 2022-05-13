package scheper.mateus.utils;

import io.grpc.Context;
import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class ConstantUtils {

    private ConstantUtils() {
        throw new UnsupportedOperationException();
    }

    public static final String BEARER_TYPE = "Bearer";

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");

    public static final String USUARIO_NAO_ENCONTRADO = "usuario.naoEncontrado";
    public static final String EMAIL_SENHA_INVALIDOS = "login.emailSenhaInvalidos";
}
