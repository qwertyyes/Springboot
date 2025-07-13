Course Search API – Testing & Verification
Endpoint
GET /api/search

Supports search with multiple filters, pagination, and sorting.
First add these dependencies that are compatible with each other 

<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data Elasticsearch -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

then add 4 files for controllers,service,models and repository create 1 file for each folder and then make sure your json data file is present in the resources tab.

then after completing write code and run spirng boot first do mvn clean install and then spring booot 

Example Queries
1. Basic Full-Text Search
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?q=Art"
Expected:

Returns courses with "Art" in the title or description

Sorted by nextSessionDate ascending (default)

Response:

json
Copy
Edit
{
  "total": 4,
  "courses": [ { "id": "...", "title": "...", ... }, ... ]
}





 2. Filter by Category & Type
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?category=Art&type=COURSE"
Expected:

Only courses where:

"category": "Art"

"type": "COURSE"









3. Filter by Age and Price Range
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?minAge=8&maxAge=10&minPrice=20&maxPrice=50"
Expected:

Courses where:

minAge <= course.minAge && maxAge >= course.maxAge

price is between 20 and 50







4. Filter by Start Date (nextSessionDate)
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?startDate=2025-08-01"
Expected:

Courses with nextSessionDate >= 2025-08-01










5. Sort by Price Ascending
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?sort=priceAsc"
Expected:

Sorted by price in ascending order










6. Sort by Upcoming Session
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?sort=upcoming"
Expected:

Sorted by nextSessionDate ascending (earliest first)












7. Pagination Example
bash
Copy
Edit
curl -X GET "http://localhost:8080/api/search?page=1&size=2"
Expected:

Returns page 1 (second page) with 2 items

Total count and total pages included

Optional: Integration Tests with Elasticsearch (Testcontainers)
Required Dependencies (Maven)
xml
Copy
Edit
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>elasticsearch</artifactId>
  <version>1.19.3</version>
  <scope>test</scope>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>








Sample Integration Test Class
java
Copy
Edit
@SpringBootTest
@Testcontainers
public class CourseSearchIntegrationTest {

    @Container
    static ElasticsearchContainer container =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.10");

    @Autowired
    private CourseRepository courseRepository;

    @BeforeAll
    static void startContainer() {
        container.start();
        System.setProperty("spring.elasticsearch.rest.uris", container.getHttpHostAddress());
    }

    @BeforeEach
    void setup() {
        courseRepository.deleteAll();

        courseRepository.save(new CourseDocument(
                "1", "Art Adventure", "Explore creative painting", "Art", "COURSE",
                "3rd–5th", 8, 10, 30.0, Instant.parse("2025-08-01T10:00:00Z")
        ));

        courseRepository.save(new CourseDocument(
                "2", "Math Magic", "Fun with numbers", "Math", "ONE_TIME",
                "2nd–4th", 7, 9, 20.0, Instant.parse("2025-09-01T10:00:00Z")
        ));
    }

    @Test
    void testSearchByCategory() {
        Page<CourseDocument> result = courseRepository.search(
            "Art", null, null, null, null, "Art", null, null, "priceAsc", PageRequest.of(0, 10)
        );
        assertEquals(1, result.getTotalElements());
        assertEquals("Art Adventure", result.getContent().get(0).getTitle());
    }
}




