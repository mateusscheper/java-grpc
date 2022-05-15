package scheper.mateus.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
@SequenceGenerator(name = "role_seq", sequenceName = "role_sequence", allocationSize = 1)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "role_seq")
    private Long idRole;

    private String nome;

    public Long getIdRole() {
        return idRole;
    }

    public void setIdRole(Long idRole) {
        this.idRole = idRole;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
