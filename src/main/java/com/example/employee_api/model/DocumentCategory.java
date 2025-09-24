package com.example.employee_api.model;

import com.example.employee_api.model.common.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Entity representing document categories (e.g., HR Documents, Legal Documents, Training Materials, etc.)
 */
@Entity
@Table(name = "document_categories", indexes = {
    @Index(name = "idx_document_category_name", columnList = "name", unique = true),
    @Index(name = "idx_document_category_active", columnList = "active")
})
public class DocumentCategory extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    @Column(name = "color", length = 50)
    private String color; // For UI display (e.g., "#FF5733")

    @Size(max = 50, message = "Icon must not exceed 50 characters")
    @Column(name = "icon", length = 50)
    private String icon; // For UI display (e.g., "file-contract")

    // Constructors
    public DocumentCategory() {}

    public DocumentCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public DocumentCategory(String name, String description, String color, String icon) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentCategory that = (DocumentCategory) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "DocumentCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
                '}';
    }
}