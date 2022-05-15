package scheper.mateus.dto;

import grpc.FiltroListaUsuarioRequest;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.br.CPF;

import java.io.Serial;
import java.io.Serializable;

public class FiltroUsuarioDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String nome;

    @CPF(message = "CPF no formato inválido.")
    private String cpf;

    @Email(message = "E-mail no formato inválido.")
    private String email;

    public FiltroListaUsuarioRequest toGrpc() {
        FiltroListaUsuarioRequest.Builder builder = FiltroListaUsuarioRequest.newBuilder();

        if (this.id != null) {
            builder.setId(this.id.toString());
        }

        if (this.nome != null) {
            builder.setNome(this.nome);
        }

        if (this.cpf != null) {
            builder.setCpf(this.cpf);
        }

        if (this.email != null) {
            builder.setEmail(this.email);
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
}
