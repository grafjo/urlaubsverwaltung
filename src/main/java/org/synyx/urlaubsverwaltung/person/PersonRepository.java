package org.synyx.urlaubsverwaltung.person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link PersonEntity} entities.
 */
interface PersonRepository extends JpaRepository<PersonEntity, Integer> {

    @Modifying
    void deleteById(Integer id);

    Optional<PersonEntity> findByUsername(String username);

    Optional<PersonEntity> findByEmail(String email);

    int countByPermissionsNotContaining(Role permission);

    int countByPermissionsContainingAndIdNotIn(Role permission, List<Integer> id);

    List<PersonEntity> findByPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permission);

    @Query("select p from person p where :permission not member of p.permissions and (p.firstName like %:query% or p.lastName like %:query%)")
    Page<PersonEntity> findByPermissionsNotContainingAndByNiceNameContainingIgnoreCase(@Param("permission") Role role, @Param("query") String query, Pageable pageable);

    List<PersonEntity> findByPermissionsContainingOrderByFirstNameAscLastNameAsc(Role permission);

    @Query("select p from person p where :permission member of p.permissions and (p.firstName like %:query% or p.lastName like %:query%)")
    Page<PersonEntity> findByPermissionsContainingAndNiceNameContainingIgnoreCase(@Param("permission") Role permission, @Param("query") String nameQuery, Pageable pageable);

    List<PersonEntity> findByPermissionsContainingAndPermissionsNotContainingOrderByFirstNameAscLastNameAsc(Role permissionContaining, Role permissionNotContaining);

    List<PersonEntity> findByPermissionsNotContainingAndNotificationsContainingOrderByFirstNameAscLastNameAsc(Role permissionNotContaining, MailNotification mailNotification);
}
