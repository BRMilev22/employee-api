package com.example.employee_api.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for automatic audit logging
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * Action type for the audit log
     */
    String action() default "";
    
    /**
     * Entity type being audited
     */
    String entityType() default "";
    
    /**
     * Description template for the audit log
     */
    String description() default "";
    
    /**
     * Whether this is a security event
     */
    boolean securityEvent() default false;
    
    /**
     * Whether to capture request/response data
     */
    boolean captureData() default false;
}