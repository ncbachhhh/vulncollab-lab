package com.vulncollab.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    Optional<Workspace> findByPublicId(String publicId);

    boolean existsByPublicId(String publicId);

    @Query("""
            select w
            from Workspace w
            join WorkspaceMember wm on wm.workspace = w
            where wm.user.id = :userId
            order by w.createdAt desc
            """)
    List<Workspace> findAllForMember(@Param("userId") Long userId);
}
