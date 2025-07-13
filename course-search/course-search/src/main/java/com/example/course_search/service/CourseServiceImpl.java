package com.example.course_search.service;

import com.example.course_search.model.CourseDocument;
import com.example.course_search.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public List<String> getSuggestions(String prefix) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchPhrasePrefixQuery("title", prefix))
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<CourseDocument> hits = elasticsearchRestTemplate.search(searchQuery, CourseDocument.class);

        return hits.stream()
                .map(hit -> hit.getContent().getTitle())
                .collect(Collectors.toList());
    }


    //  Full Search with Filtering, Sorting, Fuzzy Match
    @Override
    public Page<CourseDocument> searchCourses(String query, Integer minAge, Integer maxAge,
                                              Double minPrice, Double maxPrice,
                                              String category, String type,
                                              LocalDate nextSessionDate,
                                              String sort, int page, int size) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //  Fuzzy match on title + full-text search on description
        if (query != null && !query.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(query, "title", "description")
                .fuzziness("AUTO")); // fuzzy match
        }

        //  Age filter
        if (minAge != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("maxAge").gte(minAge));
        }
        if (maxAge != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("minAge").lte(maxAge));
        }

        //  Price filter
        if (minPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice));
        }
        if (maxPrice != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(maxPrice));
        }

        //  Category filter
        if (category != null && !category.trim().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("category", category));
        }

        //  Type filter
        if (type != null && !type.trim().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("type", type));
        }

        //  Date filter
        if (nextSessionDate != null) {
        String isoDate = nextSessionDate.atStartOfDay().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    boolQuery.filter(QueryBuilders.rangeQuery("nextSessionDate").gte(isoDate));

        }
        //  Sorting
        SortBuilder<?> sortBuilder;
        if ("priceAsc".equalsIgnoreCase(sort)) {
            sortBuilder = SortBuilders.fieldSort("price").order(SortOrder.ASC);
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            sortBuilder = SortBuilders.fieldSort("price").order(SortOrder.DESC);
        } else {
            sortBuilder = SortBuilders.fieldSort("nextSessionDate").order(SortOrder.ASC); // Default
        }

        Pageable pageable = PageRequest.of(page, size);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(boolQuery)
            .withPageable(pageable)
            .withSorts(sortBuilder);

        NativeSearchQuery searchQuery = queryBuilder.build();

        SearchHits<CourseDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, CourseDocument.class);

        List<CourseDocument> results = searchHits.getSearchHits().stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }
}
