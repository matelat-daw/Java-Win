package com.asociaciondomitila.projects.repository;

import com.asociaciondomitila.projects.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNick(String nick);

    Optional<User> findByVerificationToken(String token);

    boolean existsByEmail(String email);

    boolean existsByNick(String nick);

    @Query(
            value = """
                    select u
                    from User u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    """,
            countQuery = """
                    select count(u)
                    from User u
                    where not exists (
                        select 1
                        from u.roles r
                        where lower(r.name) = lower(:roleName)
                    )
                    """
    )
    Page<User> findAllExcludingRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query(
            value = """
                    select u
                    from User u
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
                    from User u
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
    Page<User> findAllExcludingRoleNameBySurname(
            @Param("roleName") String roleName,
            @Param("surname") String surname,
            Pageable pageable
    );
}
