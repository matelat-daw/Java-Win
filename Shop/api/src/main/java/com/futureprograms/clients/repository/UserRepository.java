package com.futureprograms.clients.repository;

import com.futureprograms.clients.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
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
}
