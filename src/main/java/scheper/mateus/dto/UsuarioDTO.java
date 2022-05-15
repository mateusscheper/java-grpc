package scheper.mateus.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import grpc.FiltroListaUsuarioRequest;
import grpc.NovoUsuarioRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serial;
import java.io.Serializable;

import static scheper.mateus.utils.StringUtils.limparCpf;

public class UsuarioDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    @NotBlank(message = "Nome não informado.")
    private String nome;

    @CPF(message = "CPF no formato inválido.")
    @NotBlank(message = "CPF não informado.")
    private String cpf;

    @Email(message = "E-mail no formato inválido.")
    @NotBlank(message = "E-mail não informado.")
    private String email;

    @NotBlank(message = "Senha não informada.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String senha;

    @NotNull(message = "ID da função não informado.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long idRole;

    public UsuarioDTO() {
    }

    public UsuarioDTO(FiltroListaUsuarioRequest usuarioResponse) {
        this.id = Long.valueOf(usuarioResponse.getId());
        this.nome = usuarioResponse.getNome();
        this.cpf = usuarioResponse.getCpf();
        this.email = usuarioResponse.getEmail();
    }

    public NovoUsuarioRequest toGrpc() {
        NovoUsuarioRequest.Builder builder = NovoUsuarioRequest.newBuilder();
        if (this.nome != null) {
            builder.setNome(this.nome.trim());
        }

        if (this.cpf != null) {
            String cpfSomenteNumeros = limparCpf(this.cpf);
            builder.setCpf(cpfSomenteNumeros);
        }

        if (this.email != null) {
            builder.setEmail(this.email.trim());
        }

        if (this.senha != null) {
            builder.setSenha(this.senha);
        }

        if (this.idRole != null) {
            builder.setIdRole(this.idRole);
        }

        return builder.build();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }
}
