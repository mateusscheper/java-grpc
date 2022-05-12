package scheper.mateus.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.annotation.Secured;
import scheper.mateus.service.UsuarioService;

@GrpcService
public class UsuarioServiceImpl extends UsuarioServiceGrpc.UsuarioServiceImplBase {

    private final UsuarioService usuarioService;

    public UsuarioServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    @Secured("ROLE_ADMIN")
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
