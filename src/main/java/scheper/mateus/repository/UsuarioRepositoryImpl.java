package scheper.mateus.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import scheper.mateus.entity.Usuario;
import grpc.ListaUsuarioResponse;

import java.util.List;

@SuppressWarnings("unchecked")
@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Usuario usuario) {
        entityManager.persist(usuario);
    }

    @Override
    public Usuario findByEmail(String email) {
        try {
            return entityManager.createQuery("SELECT u " +
                            "FROM Usuario u " +
                            "WHERE u.email = :email", Usuario.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public ListaUsuarioResponse listarUsuarios(grpc.Usuario filtro) {
        try {
            String sql = gerarSqlListarUsuarios(filtro);
            Query query = entityManager.createNativeQuery(sql);
            popularParametrosListarUsuarios(filtro, query);
            List<Object[]> dadosUsuarios = query.getResultList();

            ListaUsuarioResponse.Builder responseBuilder = ListaUsuarioResponse.newBuilder();
            popularUsuariosResponse(dadosUsuarios, responseBuilder);

            return responseBuilder.build();
        } catch (NoResultException e) {
            return ListaUsuarioResponse.newBuilder().build();
        }
    }

    private void popularUsuariosResponse(List<Object[]> dadosUsuarios, ListaUsuarioResponse.Builder responseBuilder) {
        for (Object[] dadosUsuario : dadosUsuarios) {
            String idUsuario = (String) dadosUsuario[0];
            String nome = (String) dadosUsuario[1];
            String cpf = (String) dadosUsuario[2];
            String email = (String) dadosUsuario[3];

            var usuario = grpc.Usuario.newBuilder()
                    .setId(idUsuario)
                    .setNome(nome)
                    .setCpf(cpf)
                    .setEmail(email)
                    .build();

            responseBuilder.addUsuarios(usuario);
        }
    }

    private void popularParametrosListarUsuarios(grpc.Usuario filtro, Query query) {
        if (!StringUtils.isBlank(filtro.getId())) {
            query.setParameter("idUsuario", filtro.getId());
        }

        if (!StringUtils.isBlank(filtro.getNome())) {
            String nome = filtro.getNome().trim().toLowerCase();
            query.setParameter("nome", nome);
        }

        if (!StringUtils.isBlank(filtro.getCpf())) {
            query.setParameter("cpf", filtro.getCpf());
        }

        if (!StringUtils.isBlank(filtro.getEmail())) {
            query.setParameter("email", filtro.getEmail());
        }
    }

    private String gerarSqlListarUsuarios(grpc.Usuario filtro) {
        StringBuilder sql = new StringBuilder();
        String andOrWhere = "WHERE ";
        sql.append("SELECT CAST(u.id_usuario AS varchar), u.nome, u.cpf, u.email FROM public.usuario u ");

        if (!StringUtils.isBlank(filtro.getId())) {
            sql.append(andOrWhere).append("TO_CHAR(u.idUsuario) = :idUsuario ");
            andOrWhere = "AND ";
        }

        if (!StringUtils.isBlank(filtro.getNome())) {
            sql.append(andOrWhere).append("LOWER(u.nome) LIKE :nome ");
            andOrWhere = "AND ";
        }

        if (!StringUtils.isBlank(filtro.getCpf())) {
            sql.append(andOrWhere).append("u.cpf = :cpf ");
            andOrWhere = "AND ";
        }

        if (!StringUtils.isBlank(filtro.getEmail())) {
            sql.append(andOrWhere).append("u.email = :email ");
        }

        sql.append("ORDER BY u.id_usuario");

        return sql.toString();
    }
}
