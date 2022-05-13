package scheper.mateus.grpc;

import grpc.LoginRequest;
import grpc.LoginResponse;
import grpc.LoginServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import scheper.mateus.exception.BusinessException;
import scheper.mateus.service.LoginService;

@GRpcService
public class LoginServiceImpl extends LoginServiceGrpc.LoginServiceImplBase {

    private final LoginService loginService;

    public LoginServiceImpl(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            String token = loginService.login(request);
            LoginResponse response = LoginResponse.newBuilder().setMessage(token).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (BusinessException e) {
            StatusRuntimeException statusRuntimeException = Status.INVALID_ARGUMENT.withCause(e).
                    withDescription(e.getMessage())
                    .augmentDescription(e.getMessage())
                    .asRuntimeException();
            responseObserver.onError(statusRuntimeException);
            throw statusRuntimeException;
        }
    }
}
