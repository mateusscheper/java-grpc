package scheper.mateus.grpc.client;

import grpc.FiltroListaUsuarioRequest;
import grpc.ListaUsuarioResponse;
import grpc.LoginRequest;
import grpc.NovoUsuarioRequest;
import grpc.NovoUsuarioResponse;
import grpc.UsuarioServiceGrpc;
import io.github.majusko.grpc.jwt.data.GrpcHeader;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import scheper.mateus.dto.FiltroUsuarioDTO;
import scheper.mateus.dto.UsuarioDTO;
import scheper.mateus.service.LoginService;

import java.util.List;

@Service
public class UsuarioServiceClientImpl {

    private UsuarioServiceGrpc.UsuarioServiceBlockingStub stub;

    private final LoginService loginService;

    @Value("${emailSenhaPadrao}")
    private String emailSenhaPadrao;

    public UsuarioServiceClientImpl(LoginService loginService) {
        this.loginService = loginService;
    }

    public UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO) {
        criarStub();
        try {
            NovoUsuarioRequest request = usuarioDTO.toGrpc();
            NovoUsuarioResponse response = stub.criarUsuario(request);
            usuarioDTO.setId(response.getId());
            usuarioDTO.setSenha(null);
            return usuarioDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatusCode.valueOf(500), e.getMessage());
        }
    }

    public List<UsuarioDTO> listarUsuarios(FiltroUsuarioDTO filtroUsuarioDTO) {
        criarStub();
        FiltroListaUsuarioRequest request = filtroUsuarioDTO.toGrpc();
        ListaUsuarioResponse response = stub.listarUsuarios(request);

        return response
                .getUsuariosList()
                .stream()
                .map(UsuarioDTO::new)
                .toList();
    }

    private void criarStub() {
        ManagedChannel channel = criarCanalGrpc();
        UsuarioServiceGrpc.UsuarioServiceBlockingStub usuarioServiceBlockingStubUnauthenticated = UsuarioServiceGrpc.newBlockingStub(channel);
        Metadata metadata = criarCabecalhoAuthorizationComTokenJwt();

        this.stub = MetadataUtils.attachHeaders(usuarioServiceBlockingStubUnauthenticated, metadata);
    }

    private ManagedChannel criarCanalGrpc() {
        return ManagedChannelBuilder.forTarget("localhost:9090")
                .usePlaintext()
                .build();
    }

    private Metadata criarCabecalhoAuthorizationComTokenJwt() {
        String token = getToken();
        Metadata metadata = new Metadata();
        metadata.put(GrpcHeader.AUTHORIZATION, token);
        return metadata;
    }

    private String getToken() {
        String[] emailSenha = emailSenhaPadrao.split(":");
        LoginRequest loginRequest = LoginRequest.newBuilder()
                .setEmail(emailSenha[0])
                .setSenha(emailSenha[1])
                .build();
        return "Bearer " + loginService.login(loginRequest);
    }
}
