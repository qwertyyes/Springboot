package com.example.course_search.controller;

import com.example.course_search.model.CourseDocument;
import com.example.course_search.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final CourseService courseService;

    /**
     * Full course search endpoint with filtering, pagination, and sorting.
     * Returns simplified fields: id, title, category, price, nextSessionDate.
     */
    @GetMapping
    public Map<String, Object> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false, defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        LocalDate parsedStartDate = null;
    if (startDate != null && !startDate.isBlank()) {
        try {
            parsedStartDate = LocalDate.parse(startDate); // Expecting "yyyy-MM-dd"
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid startDate format. Use yyyy-MM-dd.");
        }
    }
        Page<CourseDocument> results = courseService.searchCourses(
                q, minAge, maxAge, minPrice, maxPrice, category, type,
                parsedStartDate, sort, page, size
        );

        List<Map<String, Object>> simplifiedCourses = results.getContent().stream().map(course -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", course.getId());
            map.put("title", course.getTitle());
            map.put("category", course.getCategory());
            map.put("price", course.getPrice());
            map.put("nextSessionDate", course.getNextSessionDate());
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("total", results.getTotalElements());
        response.put("courses", simplifiedCourses);

        return response;
    }

    /**
     * Autocomplete suggestion endpoint.
     * Returns up to 10 matching course titles based on prefix match.
     */
@GetMapping("/suggest")
public List<String> suggestTitles(@RequestParam("q") String prefix) {
    return courseService.getSuggestions(prefix);
}
}
