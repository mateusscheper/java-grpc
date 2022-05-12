package scheper.mateus.builder;

import scheper.mateus.entity.Usuario;
import scheper.mateus.grpc.NovoUsuarioRequest;

public final class UsuarioBuilder {
    private Usuario usuario;

    private UsuarioBuilder() {
        usuario = new Usuario();
    }

    public static Usuario fromGrpc(NovoUsuarioRequest novoUsuarioDTO, String senha) {
        return anUsuario()
                .comNome(novoUsuarioDTO.getNome().trim())
                .comCpf(novoUsuarioDTO.getCpf().trim())
                .comEmail(novoUsuarioDTO.getEmail().trim())
                .comSenha(senha)
                .build();
    }

    public static UsuarioBuilder anUsuario() {
        return new UsuarioBuilder();
    }

    public UsuarioBuilder comIdUsuario(Long idUsuario) {
        usuario.setIdUsuario(idUsuario);
        return this;
    }

    public UsuarioBuilder comNome(String nome) {
        usuario.setNome(nome);
        return this;
    }

    public UsuarioBuilder comCpf(String cpf) {
        usuario.setCpf(cpf);
        return this;
    }

    public UsuarioBuilder comEmail(String email) {
        usuario.setEmail(email);
        return this;
    }

    public UsuarioBuilder comSenha(String senha) {
        usuario.setSenha(senha);
        return this;
    }

    public Usuario build() {
        return usuario;
    }
}
