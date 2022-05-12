package scheper.mateus.config;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(GrpcInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String methodMsg = String.format("Chamada ao serviço %s; %s; %s",
                serverCall.getMethodDescriptor().getFullMethodName(),
                metadata,
                serverCall.getAttributes());

        log.info(methodMsg);

        return serverCallHandler.startCall(serverCall, metadata);
    }
}
