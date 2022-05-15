package scheper.mateus.grpc;

import grpc.FiltroListaUsuarioRequest;
import grpc.ListaUsuarioResponse;
import grpc.NovoUsuarioRequest;
import grpc.NovoUsuarioResponse;
import grpc.UsuarioServiceGrpc;
import io.github.majusko.grpc.jwt.annotation.Allow;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.dao.DataIntegrityViolationException;
import scheper.mateus.service.UsuarioService;

@GRpcService
public class UsuarioServiceImpl extends UsuarioServiceGrpc.UsuarioServiceImplBase {

    private final UsuarioService usuarioService;

    public UsuarioServiceImpl(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @Override
    @Allow(roles = "ADMIN")
    public void criarUsuario(NovoUsuarioRequest request, StreamObserver<NovoUsuarioResponse> responseObserver) {
        try {
            NovoUsuarioResponse response = usuarioService.criarUsuario(request);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            String description = popularDetalhesDescricaoRetorno(e);

            StatusRuntimeException statusRuntimeException = Status.INVALID_ARGUMENT
                    .withCause(e)
                    .withDescription(description)
                    .augmentDescription(e.getMessage())
                    .asRuntimeException();

            responseObserver.onError(statusRuntimeException);

            throw statusRuntimeException;
        }
    }

    private String popularDetalhesDescricaoRetorno(Exception e) {
        String description = "";
        if (e instanceof DataIntegrityViolationException dataintegrityviolationexception) {
            description = dataintegrityviolationexception.getMostSpecificCause().getMessage();
        } else {
            description = "Erro ao salvar o usuário.";
        }
        return description;
    }

    @Override
    @Allow(roles = "ADMIN")
    public void listarUsuarios(FiltroListaUsuarioRequest request, StreamObserver<ListaUsuarioResponse> responseObserver) {
        ListaUsuarioResponse usuarios = usuarioService.listarUsuarios(request);

        responseObserver.onNext(usuarios);
        responseObserver.onCompleted();
    }
}
