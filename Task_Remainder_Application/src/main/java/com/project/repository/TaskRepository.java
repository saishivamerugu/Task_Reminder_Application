package com.project.repository;

import com.project.entity.Task;
import com.project.enums.TaskPriority;
import com.project.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (
                    :keyword IS NULL OR
                    LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                    LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
           """)
    Page<Task> findTasksWithFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
