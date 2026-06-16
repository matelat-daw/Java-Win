package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByNick(String nick);

    @EntityGraph(attributePaths = "roles")
    Optional<Staff> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByNick(String nick);

    @Query(
            value = """
                    select u
                    from Staff u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    """,
            countQuery = """
                    select count(u)
                    from Staff u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    """
    )
    @EntityGraph(attributePaths = "roles")
    Page<Staff> findAllExcludingRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query(
            value = """
                    select u
                    from Staff u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    and (
                        lower(u.surname1) like lower(concat('%', :surname, '%'))
                        or lower(coalesce(u.surname2, '')) like lower(concat('%', :surname, '%'))
                    )
                    """,
            countQuery = """
                    select count(u)
                    from Staff u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    and (
                        lower(u.surname1) like lower(concat('%', :surname, '%'))
                        or lower(coalesce(u.surname2, '')) like lower(concat('%', :surname, '%'))
                    )
                    """
    )
    @EntityGraph(attributePaths = "roles")
    Page<Staff> findAllExcludingRoleNameBySurname(
            @Param("roleName") String roleName,
            @Param("surname") String surname,
            Pageable pageable
    );
}
