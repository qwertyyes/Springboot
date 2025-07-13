package com.example.course_search.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.example.course_search.model.CourseDocument;

public interface CourseService {
    Page<CourseDocument> searchCourses(String query, Integer minAge, Integer maxAge,
                                       Double minPrice, Double maxPrice,
                                       String category, String type,
                                       LocalDate nextSessionDate,
                                       String sort, int page, int size);
                                       
    List<String> getSuggestions(String prefix);
}
