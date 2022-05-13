package scheper.mateus.repository;

import grpc.ListaUsuarioResponse;
import scheper.mateus.entity.Usuario;

public interface UsuarioRepository {

    ListaUsuarioResponse listarUsuarios(grpc.Usuario filtro);

    void save(Usuario usuario);

    Usuario findByEmail(String email);
}
