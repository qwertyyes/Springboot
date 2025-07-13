package com.example.course_search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import com.example.course_search.model.CourseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface CourseRepository extends ElasticsearchRepository<CourseDocument, String> {
    // Optional search helpers
Page<CourseDocument> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<CourseDocument> findByCategory(String category, Pageable pageable);
}
