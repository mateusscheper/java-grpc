package scheper.mateus.grpc;

import grpc.ListaUsuarioResponse;
import grpc.NovoUsuarioRequest;
import grpc.NovoUsuarioResponse;
import grpc.Usuario;
import grpc.UsuarioServiceGrpc;
import io.github.majusko.grpc.jwt.annotation.Allow;
import io.github.majusko.grpc.jwt.annotation.Exposed;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.security.access.annotation.Secured;
import scheper.mateus.service.UsuarioService;

@GRpcService
public class UsuarioServiceImpl extends UsuarioServiceGrpc.UsuarioServiceImplBase {

    private final UsuarioService usuarioService;

    public UsuarioServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @Override
    @Secured({})
    @Exposed(environments={"default","qa"})
    public void criarUsuario(NovoUsuarioRequest request, StreamObserver<NovoUsuarioResponse> responseObserver) {
        try {
            usuarioService.criarUsuario(request);

            NovoUsuarioResponse response = NovoUsuarioResponse.newBuilder()
                    .setMessage("OK")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            StatusRuntimeException statusRuntimeException = Status.INVALID_ARGUMENT.withCause(e).
                    withDescription("Erro ao salvar o usuário.")
                    .augmentDescription(e.getMessage())
                    .asRuntimeException();

            responseObserver.onError(statusRuntimeException);

            throw statusRuntimeException;
        }
    }

    @Override
    public void listarUsuarios(Usuario request, StreamObserver<ListaUsuarioResponse> responseObserver) {
        ListaUsuarioResponse usuarios = usuarioService.listarUsuarios(request);

        responseObserver.onNext(usuarios);
        responseObserver.onCompleted();
    }
}
