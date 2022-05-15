package scheper.mateus.rest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import scheper.mateus.dto.FiltroUsuarioDTO;
import scheper.mateus.dto.UsuarioDTO;
import scheper.mateus.grpc.client.UsuarioServiceClientImpl;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioServiceClientImpl usuarioService;

    public UsuarioController(UsuarioServiceClientImpl usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody @Valid UsuarioDTO usuarioDTO) {
        UsuarioDTO usuarioCriadoDTO = usuarioService.criarUsuario(usuarioDTO);
        return ResponseEntity.ok(usuarioCriadoDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios(@Valid FiltroUsuarioDTO filtroUsuarioDTO) {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuarios(filtroUsuarioDTO);
        return !usuarios.isEmpty() ? ResponseEntity.ok(usuarios) : ResponseEntity.noContent().build();
    }
}
