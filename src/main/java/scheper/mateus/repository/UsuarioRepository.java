package scheper.mateus.repository;

import scheper.mateus.entity.Usuario;
import scheper.mateus.grpc.ListaUsuarioResponse;

public interface UsuarioRepository {

    ListaUsuarioResponse listarUsuarios(scheper.mateus.grpc.Usuario filtro);

    void save(Usuario usuario);

    Usuario findByEmail(String email);
}
