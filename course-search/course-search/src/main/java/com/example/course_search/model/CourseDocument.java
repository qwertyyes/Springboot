package com.example.course_search.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "courses")

@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseDocument {

    @Id
    private String id;

    private String title;
    private String description;

    private int minAge;
    private int maxAge;

    private double price;

    private String category; // e.g. Science, Art, etc.
    private String type;     // e.g. Online, Offline

    @Field(type = FieldType.Date, format = DateFormat.date_time)
private OffsetDateTime nextSessionDate;

    @CompletionField
    private Completion suggest;

    public void setSuggestFromTitle() {
        this.suggest = new Completion(new String[]{this.title});
    }
    private String gradeRange;

public String getGradeRange() {
    return gradeRange;
}

public void setGradeRange(String gradeRange) {
    this.gradeRange = gradeRange;
}
}
