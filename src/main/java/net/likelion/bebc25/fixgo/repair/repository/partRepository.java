package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;

public interface partRepository extends JpaRepository<Part, Long> {
    
}
