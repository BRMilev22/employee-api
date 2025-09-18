package com.example.employee_api.repository.specification;

import com.example.employee_api.model.Employee;
import com.example.employee_api.model.enums.EmployeeStatus;
import com.example.employee_api.model.enums.EmploymentType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification class for building dynamic queries for Employee entity
 * This allows for flexible, type-safe query construction
 */
public class EmployeeSpecifications {

    public static Specification<Employee> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) -> {
            if (firstName == null || firstName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("firstName")),
                "%" + firstName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("lastName")),
                "%" + lastName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String searchTerm = "%" + fullName.toLowerCase() + "%";
            return criteriaBuilder.like(
                criteriaBuilder.lower(
                    criteriaBuilder.concat(
                        criteriaBuilder.concat(root.get("firstName"), " "),
                        root.get("lastName")
                    )
                ),
                searchTerm
            );
        };
    }

    public static Specification<Employee> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")),
                "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasEmployeeId(String employeeId) {
        return (root, query, criteriaBuilder) -> {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("employeeId")),
                "%" + employeeId.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasJobTitle(String jobTitle) {
        return (root, query, criteriaBuilder) -> {
            if (jobTitle == null || jobTitle.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("jobTitle")),
                "%" + jobTitle.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasStatus(EmployeeStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Employee> hasEmploymentType(EmploymentType employmentType) {
        return (root, query, criteriaBuilder) -> {
            if (employmentType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("employmentType"), employmentType);
        };
    }

    public static Specification<Employee> belongsToDepartment(Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            if (departmentId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
            return criteriaBuilder.equal(departmentJoin.get("id"), departmentId);
        };
    }

    public static Specification<Employee> belongsToDepartmentName(String departmentName) {
        return (root, query, criteriaBuilder) -> {
            if (departmentName == null || departmentName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> departmentJoin = root.join("department", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(departmentJoin.get("name")),
                "%" + departmentName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasCurrentPosition(Long positionId) {
        return (root, query, criteriaBuilder) -> {
            if (positionId == null) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> positionJoin = root.join("currentPosition", JoinType.LEFT);
            return criteriaBuilder.equal(positionJoin.get("id"), positionId);
        };
    }

    public static Specification<Employee> hasCurrentPositionTitle(String positionTitle) {
        return (root, query, criteriaBuilder) -> {
            if (positionTitle == null || positionTitle.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> positionJoin = root.join("currentPosition", JoinType.LEFT);
            return criteriaBuilder.like(
                criteriaBuilder.lower(positionJoin.get("title")),
                "%" + positionTitle.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasSalaryBetween(BigDecimal minSalary, BigDecimal maxSalary) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minSalary != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            }
            
            if (maxSalary != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> hasHireDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hireDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), endDate));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> hasBirthDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), endDate));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Employee> hasManager(Long managerId) {
        return (root, query, criteriaBuilder) -> {
            if (managerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("managerId"), managerId);
        };
    }

    public static Specification<Employee> hasNoManager() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("managerId"));
    }

    public static Specification<Employee> livesInCity(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("city")),
                "%" + city.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> livesInState(String state) {
        return (root, query, criteriaBuilder) -> {
            if (state == null || state.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("state")),
                "%" + state.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasPostalCode(String postalCode) {
        return (root, query, criteriaBuilder) -> {
            if (postalCode == null || postalCode.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("postalCode"), postalCode);
        };
    }

    public static Specification<Employee> hasPhone(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("phone")),
                "%" + phone.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Employee> hasGender(String gender) {
        return (root, query, criteriaBuilder) -> {
            if (gender == null || gender.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("gender")),
                "%" + gender.toLowerCase() + "%"
            );
        };
    }

    // Full-text search across multiple fields
    public static Specification<Employee> globalSearch(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            String likePattern = "%" + searchTerm.toLowerCase() + "%";
            
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeId")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("jobTitle")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likePattern)
            );
        };
    }

    // Age range calculation
    public static Specification<Employee> hasAgeBetween(Integer minAge, Integer maxAge) {
        return (root, query, criteriaBuilder) -> {
            if (minAge == null && maxAge == null) {
                return criteriaBuilder.conjunction();
            }
            
            LocalDate now = LocalDate.now();
            List<Predicate> predicates = new ArrayList<>();
            
            if (maxAge != null) {
                LocalDate minBirthDate = now.minusYears(maxAge + 1);
                predicates.add(criteriaBuilder.greaterThan(root.get("birthDate"), minBirthDate));
            }
            
            if (minAge != null) {
                LocalDate maxBirthDate = now.minusYears(minAge);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), maxBirthDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Years of service
    public static Specification<Employee> hasYearsOfServiceBetween(Integer minYears, Integer maxYears) {
        return (root, query, criteriaBuilder) -> {
            if (minYears == null && maxYears == null) {
                return criteriaBuilder.conjunction();
            }
            
            LocalDate now = LocalDate.now();
            List<Predicate> predicates = new ArrayList<>();
            
            if (maxYears != null) {
                LocalDate minHireDate = now.minusYears(maxYears + 1);
                predicates.add(criteriaBuilder.greaterThan(root.get("hireDate"), minHireDate));
            }
            
            if (minYears != null) {
                LocalDate maxHireDate = now.minusYears(minYears);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hireDate"), maxHireDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}