package com.example.employee_api.specifications;

import com.example.employee_api.dto.UserSearchCriteria;
import com.example.employee_api.model.Role;
import com.example.employee_api.model.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic User queries
 */
public class UserSpecifications {

    /**
     * Create specification based on search criteria
     */
    public static Specification<User> withCriteria(UserSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Username filter (partial match, case-insensitive)
            if (criteria.getUsername() != null && !criteria.getUsername().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")),
                    "%" + criteria.getUsername().toLowerCase() + "%"
                ));
            }

            // Email filter (partial match, case-insensitive)
            if (criteria.getEmail() != null && !criteria.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }

            // First name filter (partial match, case-insensitive)
            if (criteria.getFirstName() != null && !criteria.getFirstName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + criteria.getFirstName().toLowerCase() + "%"
                ));
            }

            // Last name filter (partial match, case-insensitive)
            if (criteria.getLastName() != null && !criteria.getLastName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%" + criteria.getLastName().toLowerCase() + "%"
                ));
            }

            // Enabled status filter
            if (criteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), criteria.getEnabled()));
            }

            // Email verified status filter
            if (criteria.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), criteria.getEmailVerified()));
            }

            // Role filter
            if (criteria.getRole() != null && !criteria.getRole().trim().isEmpty()) {
                Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(roleJoin.get("name")),
                    "%" + criteria.getRole().toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}