package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.BeneficiaryUser;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiaryUserRepository extends JpaRepository<BeneficiaryUser, Long> {
    @Query(value = """
            select distinct u
            from BeneficiaryUser u
            join u.projects p
            where p.id = :projectId
            order by u.surname1 asc, u.surname2 asc, u.name asc
            """,
            countQuery = """
            select count(distinct u)
            from BeneficiaryUser u
            join u.projects p
            where p.id = :projectId
            """)
    Page<BeneficiaryUser> findByProjectIdOrderBySurname1AscSurname2AscNameAsc(@Param("projectId") Long projectId, Pageable pageable);

    Optional<BeneficiaryUser> findByDniIgnoreCase(String dni);

    Optional<BeneficiaryUser> findByEmailIgnoreCase(String email);

    Optional<BeneficiaryUser> findByPhone(Long phone);
}
