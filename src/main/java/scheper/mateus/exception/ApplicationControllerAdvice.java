package scheper.mateus.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationErros(BindException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<String> messages = bindingResult.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();
        return new ApiErrors(messages);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrors> handleResponseStatusException(ResponseStatusException ex) {
        String mensagemErro = ex.getReason();
        HttpStatusCode statusCode = ex.getStatusCode();
        ApiErrors apiErrors = new ApiErrors(mensagemErro);
        return new ResponseEntity<>(apiErrors, statusCode);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleException(BusinessException e) {
        String message = e.getMessage();

        if (message.startsWith("{") && message.endsWith("}")) {
            String messageNoBraces = message.replace("{", "").replace("}", "");
            try {
                message = ResourceBundle.getBundle("messages").getString(messageNoBraces);
            } catch (MissingResourceException ignored) {
                // ignored
            }
        }

        logger.info(message);

        return new ApiErrors(message);
    }
}
