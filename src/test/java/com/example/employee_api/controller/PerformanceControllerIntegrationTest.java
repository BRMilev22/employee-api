package com.example.employee_api.controller;

import com.example.employee_api.model.*;
import com.example.employee_api.model.enums.*;
import com.example.employee_api.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PerformanceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;



    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PerformanceReviewRepository performanceReviewRepository;

    @Autowired
    private GoalRepository goalRepository;

    private Department testDepartment;
    private Employee testEmployee;
    private Employee testReviewer;
    private PerformanceReview testReview;
    private Goal testGoal;

    @BeforeEach
    void setUp() {
        // Set up MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Clear repositories
        goalRepository.deleteAll();
        performanceReviewRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        
        // Create test department
        testDepartment = new Department();
        testDepartment.setName("Performance Test Department");
        testDepartment.setDepartmentCode("PERF001");
        testDepartment.setDescription("Department for performance testing");
        testDepartment = departmentRepository.save(testDepartment);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@company.com");
        testEmployee.setPhone("123-456-7890");
        testEmployee.setJobTitle("Software Engineer");
        testEmployee.setGender("MALE");
        testEmployee.setHireDate(LocalDate.of(2020, 1, 15));
        testEmployee.setSalary(BigDecimal.valueOf(75000));
        testEmployee.setStatus(EmployeeStatus.ACTIVE);
        testEmployee.setDepartment(testDepartment);
        testEmployee = employeeRepository.save(testEmployee);

        // Create test reviewer
        testReviewer = new Employee();
        testReviewer.setEmployeeId("EMP002");
        testReviewer.setFirstName("Jane");
        testReviewer.setLastName("Smith");
        testReviewer.setEmail("jane.smith@company.com");
        testReviewer.setPhone("987-654-3210");
        testReviewer.setJobTitle("Senior Manager");
        testReviewer.setGender("FEMALE");
        testReviewer.setHireDate(LocalDate.of(2018, 6, 1));
        testReviewer.setSalary(BigDecimal.valueOf(95000));
        testReviewer.setStatus(EmployeeStatus.ACTIVE);
        testReviewer.setDepartment(testDepartment);
        testReviewer = employeeRepository.save(testReviewer);

        // Create test performance review
        testReview = new PerformanceReview();
        testReview.setEmployee(testEmployee);
        testReview.setReviewer(testReviewer);
        testReview.setTitle("Q1 2024 Performance Review");
        testReview.setDescription("Quarterly performance evaluation");
        testReview.setReviewPeriodStart(LocalDate.of(2024, 1, 1));
        testReview.setReviewPeriodEnd(LocalDate.of(2024, 3, 31));
        testReview.setDueDate(LocalDate.of(2024, 4, 15));
        testReview.setStatus(ReviewStatus.DRAFT);
        testReview = performanceReviewRepository.save(testReview);

        // Create test goal
        testGoal = new Goal();
        testGoal.setEmployee(testEmployee);
        testGoal.setTitle("Complete Spring Boot Certification");
        testGoal.setDescription("Obtain Spring Boot professional certification");
        testGoal.setStartDate(LocalDate.of(2024, 1, 1));
        testGoal.setDueDate(LocalDate.of(2024, 6, 30));
        testGoal.setProgressPercentage(BigDecimal.valueOf(25));
        testGoal.setPriority(GoalPriority.HIGH);
        testGoal.setStatus(GoalStatus.IN_PROGRESS);
        testGoal.setSuccessCriteria("Pass certification exam with score > 80%");
        testGoal = goalRepository.save(testGoal);
    }

    // ===== PERFORMANCE REVIEW TESTS =====

    @Test
    void getAllPerformanceReviews_ShouldReturnAllReviews() throws Exception {
        mockMvc.perform(get("/api/performance/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.title == 'Q1 2024 Performance Review')]", hasSize(1)));
    }

    @Test
    void getAllPerformanceReviewsPaginated_ShouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/paginated")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", isA(List.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void getPerformanceReviewById_ShouldReturnSpecificReview() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/{id}", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testReview.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Q1 2024 Performance Review")))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    void createPerformanceReview_ShouldCreateNewReview() throws Exception {
        PerformanceReview newReview = new PerformanceReview();
        newReview.setEmployee(testEmployee);
        newReview.setReviewer(testReviewer);
        newReview.setTitle("Q2 2024 Performance Review");
        newReview.setDescription("Second quarter performance evaluation");
        newReview.setReviewPeriodStart(LocalDate.of(2024, 4, 1));
        newReview.setReviewPeriodEnd(LocalDate.of(2024, 6, 30));
        newReview.setDueDate(LocalDate.of(2024, 7, 15));
        newReview.setStatus(ReviewStatus.DRAFT);

        String reviewJson = """
            {
                "employee": {"id": %d},
                "reviewer": {"id": %d},
                "title": "Q2 2024 Performance Review",
                "description": "Second quarter performance evaluation",
                "reviewPeriodStart": "2024-04-01",
                "reviewPeriodEnd": "2024-06-30",
                "dueDate": "2024-07-15",
                "status": "DRAFT"
            }
            """.formatted(testEmployee.getId(), testReviewer.getId());

        mockMvc.perform(post("/api/performance/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Q2 2024 Performance Review")))
                .andExpect(jsonPath("$.status", is("DRAFT")));
    }

    @Test
    void updatePerformanceReview_ShouldUpdateExistingReview() throws Exception {
        String reviewJson = """
            {
                "employee": {"id": %d},
                "reviewer": {"id": %d},
                "title": "Updated Q1 2024 Performance Review",
                "description": "Updated quarterly performance evaluation",
                "reviewPeriodStart": "2024-01-01",
                "reviewPeriodEnd": "2024-03-31",
                "dueDate": "2024-04-15",
                "status": "PENDING",
                "overallRating": "MEETS_EXPECTATIONS",
                "employeeComments": "Great progress this quarter"
            }
            """.formatted(testEmployee.getId(), testReviewer.getId());

        mockMvc.perform(put("/api/performance/reviews/{id}", testReview.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Updated Q1 2024 Performance Review")))
                .andExpect(jsonPath("$.overallRating", is("MEETS_EXPECTATIONS")));
    }

    @Test
    void deletePerformanceReview_ShouldRemoveReview() throws Exception {
        mockMvc.perform(delete("/api/performance/reviews/{id}", testReview.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/performance/reviews/{id}", testReview.getId()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getPerformanceReviewsByEmployee_ShouldReturnEmployeeReviews() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/employee/{employeeId}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Q1 2024 Performance Review")));
    }

    @Test
    void getPerformanceReviewsByReviewer_ShouldReturnReviewerReviews() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/reviewer/{reviewerId}", testReviewer.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void getPerformanceReviewsByStatus_ShouldReturnReviewsWithStatus() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/status/{status}", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", is("DRAFT")));
    }

    @Test
    void getOverduePerformanceReviews_ShouldReturnOverdueReviews() throws Exception {
        // Create overdue review
        PerformanceReview overdueReview = new PerformanceReview();
        overdueReview.setEmployee(testEmployee);
        overdueReview.setReviewer(testReviewer);
        overdueReview.setTitle("Overdue Review");
        overdueReview.setDescription("Past due review");
        overdueReview.setReviewPeriodStart(LocalDate.of(2023, 10, 1));
        overdueReview.setReviewPeriodEnd(LocalDate.of(2023, 12, 31));
        overdueReview.setDueDate(LocalDate.of(2024, 1, 15)); // Past due
        overdueReview.setStatus(ReviewStatus.IN_PROGRESS);
        performanceReviewRepository.save(overdueReview);

        mockMvc.perform(get("/api/performance/reviews/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void submitReview_ShouldChangeStatusToPending() throws Exception {
        mockMvc.perform(put("/api/performance/reviews/{id}/submit", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void approveReview_ShouldChangeStatusToApproved() throws Exception {
        // First submit the review
        testReview.setStatus(ReviewStatus.PENDING);
        performanceReviewRepository.save(testReview);

        mockMvc.perform(put("/api/performance/reviews/{id}/approve", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void startReview_ShouldChangeStatusToInProgress() throws Exception {
        mockMvc.perform(put("/api/performance/reviews/{id}/start", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void completeReview_ShouldChangeStatusToCompleted() throws Exception {
        // First start the review
        testReview.setStatus(ReviewStatus.IN_PROGRESS);
        performanceReviewRepository.save(testReview);

        mockMvc.perform(put("/api/performance/reviews/{id}/complete", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.completedDate", notNullValue()));
    }

    // ===== GOAL TESTS =====

    @Test
    void getAllGoals_ShouldReturnAllGoals() throws Exception {
        mockMvc.perform(get("/api/performance/goals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.title == 'Complete Spring Boot Certification')]", hasSize(1)));
    }

    @Test
    void getAllGoalsPaginated_ShouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/performance/goals/paginated")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", isA(List.class)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.size", is(5)));
    }

    @Test
    void getGoalById_ShouldReturnSpecificGoal() throws Exception {
        mockMvc.perform(get("/api/performance/goals/{id}", testGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testGoal.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Complete Spring Boot Certification")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void createGoal_ShouldCreateNewGoal() throws Exception {
        String goalJson = """
            {
                "employee": {"id": %d},
                "title": "Learn Docker",
                "description": "Master containerization with Docker",
                "startDate": "2024-02-01",
                "dueDate": "2024-08-31",
                "progressPercentage": 0,
                "priority": "MEDIUM",
                "status": "NOT_STARTED",
                "successCriteria": "Complete Docker certification course"
            }
            """.formatted(testEmployee.getId());

        mockMvc.perform(post("/api/performance/goals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(goalJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Learn Docker")))
                .andExpect(jsonPath("$.status", is("NOT_STARTED")));
    }

    @Test
    void updateGoal_ShouldUpdateExistingGoal() throws Exception {
        String goalJson = """
            {
                "employee": {"id": %d},
                "title": "Complete Advanced Spring Boot Certification",
                "description": "Master advanced Spring Boot features",
                "startDate": "2024-01-01",
                "dueDate": "2024-12-31",
                "progressPercentage": 50,
                "priority": "HIGH",
                "status": "IN_PROGRESS",
                "successCriteria": "Complete certification with score above 80%%"
            }
            """.formatted(testEmployee.getId());

        mockMvc.perform(put("/api/performance/goals/{id}", testGoal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(goalJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Complete Advanced Spring Boot Certification")))
                .andExpect(jsonPath("$.progressPercentage", is(50)));
    }

    @Test
    void deleteGoal_ShouldRemoveGoal() throws Exception {
        mockMvc.perform(delete("/api/performance/goals/{id}", testGoal.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/performance/goals/{id}", testGoal.getId()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getGoalsByEmployee_ShouldReturnEmployeeGoals() throws Exception {
        mockMvc.perform(get("/api/performance/goals/employee/{employeeId}", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title", is("Complete Spring Boot Certification")));
    }

    @Test
    void getActiveGoalsByEmployee_ShouldReturnActiveGoals() throws Exception {
        mockMvc.perform(get("/api/performance/goals/employee/{employeeId}/active", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", anyOf(is("NOT_STARTED"), is("IN_PROGRESS"))));
    }

    @Test
    void getGoalsByStatus_ShouldReturnGoalsWithStatus() throws Exception {
        mockMvc.perform(get("/api/performance/goals/status/{status}", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")));
    }

    @Test
    void getOverdueGoals_ShouldReturnOverdueGoals() throws Exception {
        // Create overdue goal
        Goal overdueGoal = new Goal();
        overdueGoal.setEmployee(testEmployee);
        overdueGoal.setTitle("Overdue Goal");
        overdueGoal.setDescription("Past due goal");
        overdueGoal.setStartDate(LocalDate.of(2023, 6, 1));
        overdueGoal.setDueDate(LocalDate.of(2023, 12, 31)); // Past due
        overdueGoal.setProgressPercentage(BigDecimal.valueOf(30));
        overdueGoal.setPriority(GoalPriority.HIGH);
        overdueGoal.setStatus(GoalStatus.IN_PROGRESS);
        goalRepository.save(overdueGoal);

        mockMvc.perform(get("/api/performance/goals/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void updateGoalProgress_ShouldUpdateProgress() throws Exception {
        mockMvc.perform(put("/api/performance/goals/{id}/progress", testGoal.getId())
                .param("progressPercentage", "75"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.progressPercentage", is(75)));
    }

    @Test
    void completeGoal_ShouldMarkGoalAsCompleted() throws Exception {
        mockMvc.perform(put("/api/performance/goals/{id}/complete", testGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.progressPercentage", is(100)))
                .andExpect(jsonPath("$.completedDate", notNullValue()));
    }

    @Test
    void startGoal_ShouldChangeStatusToInProgress() throws Exception {
        // Create a goal with NOT_STARTED status
        Goal newGoal = new Goal();
        newGoal.setEmployee(testEmployee);
        newGoal.setTitle("New Goal to Start");
        newGoal.setDescription("A goal to be started");
        newGoal.setStartDate(LocalDate.now());
        newGoal.setDueDate(LocalDate.now().plusMonths(3));
        newGoal.setProgressPercentage(BigDecimal.ZERO);
        newGoal.setPriority(GoalPriority.MEDIUM);
        newGoal.setStatus(GoalStatus.NOT_STARTED);
        Goal savedGoal = goalRepository.save(newGoal);

        mockMvc.perform(put("/api/performance/goals/{id}/start", savedGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void pauseGoal_ShouldChangeStatusToOnHold() throws Exception {
        mockMvc.perform(put("/api/performance/goals/{id}/pause", testGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("ON_HOLD")));
    }

    @Test
    void cancelGoal_ShouldChangeStatusToCancelled() throws Exception {
        mockMvc.perform(put("/api/performance/goals/{id}/cancel", testGoal.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    // ===== STATISTICS AND VALIDATION TESTS =====

    @Test
    void getAverageProgressByEmployee_ShouldReturnAverage() throws Exception {
        mockMvc.perform(get("/api/performance/goals/employee/{employeeId}/average-progress", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void canEmployeeCreateGoal_ShouldReturnBoolean() throws Exception {
        mockMvc.perform(get("/api/performance/goals/employee/{employeeId}/can-create", testEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(Boolean.class)));
    }

    @Test
    void canStartReview_ShouldReturnBoolean() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/{reviewId}/can-start", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(Boolean.class)));
    }

    @Test
    void canCompleteReview_ShouldReturnBoolean() throws Exception {
        mockMvc.perform(get("/api/performance/reviews/{reviewId}/can-complete", testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(Boolean.class)));
    }
}