package scheper.mateus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scheper.mateus.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
