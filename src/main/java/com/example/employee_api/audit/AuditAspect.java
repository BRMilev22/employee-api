package com.example.employee_api.audit;

import com.example.employee_api.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * AOP Aspect for automatic audit logging
 */
@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * Around advice for methods annotated with @Auditable
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            logAuditEvent(joinPoint, auditable, result, exception, duration);
        }
    }
    
    /**
     * After advice for all controller methods to log API access
     */
    @AfterReturning(
        pointcut = "execution(* com.example.employee_api.controller.*.*(..))",
        returning = "result"
    )
    public void logApiAccess(JoinPoint joinPoint, Object result) {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String methodName = joinPoint.getSignature().getName();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                
                // Only log non-audit endpoints to avoid infinite loops
                if (!"AuditLogController".equals(className)) {
                    auditLogService.logUserAction(
                        AuditLogService.ACTION_ACCESS,
                        "API",
                        null,
                        String.format("Accessed %s.%s via %s %s", 
                            className, methodName, request.getMethod(), request.getRequestURI())
                    );
                }
            }
        } catch (Exception e) {
            // Log errors but don't fail the main operation
            System.err.println("Failed to log API access: " + e.getMessage());
        }
    }
    
    /**
     * After throwing advice for all controller methods to log errors
     */
    @AfterThrowing(
        pointcut = "execution(* com.example.employee_api.controller.*.*(..))",
        throwing = "exception"
    )
    public void logApiError(JoinPoint joinPoint, Throwable exception) {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String methodName = joinPoint.getSignature().getName();
                String className = joinPoint.getTarget().getClass().getSimpleName();
                
                // Only log non-audit endpoints to avoid infinite loops
                if (!"AuditLogController".equals(className)) {
                    auditLogService.logUserAction(
                        AuditLogService.ACTION_ACCESS,
                        "API",
                        null,
                        String.format("Failed to access %s.%s via %s %s", 
                            className, methodName, request.getMethod(), request.getRequestURI()),
                        false,
                        exception.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            // Log errors but don't fail the main operation
            System.err.println("Failed to log API error: " + e.getMessage());
        }
    }
    
    /**
     * Around advice for service layer CRUD operations
     */
    @Around("execution(* com.example.employee_api.service.*.create*(..)) || " +
            "execution(* com.example.employee_api.service.*.update*(..)) || " +
            "execution(* com.example.employee_api.service.*.delete*(..))")
    public Object auditCrudOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String serviceName = joinPoint.getTarget().getClass().getSimpleName();
        
        // Skip audit service to avoid infinite loops
        if ("AuditLogService".equals(serviceName)) {
            return joinPoint.proceed();
        }
        
        String actionType = determineActionType(methodName);
        String entityType = determineEntityType(serviceName);
        
        Object result = null;
        
        try {
            result = joinPoint.proceed();
            
            // Try to extract entity ID from result
            Long entityId = extractEntityId(result);
            
            auditLogService.logUserAction(
                actionType,
                entityType,
                entityId,
                String.format("%s operation on %s", actionType, entityType)
            );
            
            return result;
        } catch (Throwable e) {
            
            auditLogService.logUserAction(
                actionType,
                entityType,
                null,
                String.format("Failed %s operation on %s", actionType, entityType),
                false,
                e.getMessage()
            );
            
            throw e;
        }
    }
    
    private void logAuditEvent(ProceedingJoinPoint joinPoint, Auditable auditable, 
                              Object result, Throwable exception, long duration) {
        try {
            String actionType = auditable.action().isEmpty() ? 
                determineActionType(joinPoint.getSignature().getName()) : auditable.action();
            
            String entityType = auditable.entityType().isEmpty() ? 
                determineEntityType(joinPoint.getTarget().getClass().getSimpleName()) : auditable.entityType();
            
            String description = auditable.description().isEmpty() ? 
                String.format("Executed %s on %s", actionType, entityType) : auditable.description();
            
            boolean success = exception == null;
            String errorMessage = exception != null ? exception.getMessage() : null;
            
            Long entityId = extractEntityId(result);
            
            Object oldValues = null;
            Object newValues = null;
            
            if (auditable.captureData() && result != null) {
                newValues = result;
            }
            
            auditLogService.logUserAction(
                actionType,
                entityType,
                entityId,
                description,
                success,
                errorMessage,
                oldValues,
                newValues
            );
            
        } catch (Exception e) {
            // Log errors but don't fail the main operation
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }
    
    private String determineActionType(String methodName) {
        methodName = methodName.toLowerCase();
        
        if (methodName.contains("create") || methodName.contains("add") || methodName.contains("save")) {
            return AuditLogService.ACTION_CREATE;
        } else if (methodName.contains("update") || methodName.contains("modify") || methodName.contains("edit")) {
            return AuditLogService.ACTION_UPDATE;
        } else if (methodName.contains("delete") || methodName.contains("remove")) {
            return AuditLogService.ACTION_DELETE;
        } else if (methodName.contains("get") || methodName.contains("find") || methodName.contains("search")) {
            return AuditLogService.ACTION_READ;
        } else {
            return AuditLogService.ACTION_ACCESS;
        }
    }
    
    private String determineEntityType(String className) {
        // Remove "Service" or "Controller" suffix and convert to entity name
        String entityType = className.replace("Service", "").replace("Controller", "");
        
        // Handle special cases
        if (entityType.equals("Employee")) return "Employee";
        if (entityType.equals("Department")) return "Department";
        if (entityType.equals("Position")) return "Position";
        if (entityType.equals("LeaveRequest")) return "LeaveRequest";
        if (entityType.equals("LeaveBalance")) return "LeaveBalance";
        if (entityType.equals("LeaveType")) return "LeaveType";
        if (entityType.equals("LeaveDocument")) return "LeaveDocument";
        if (entityType.equals("User")) return "User";
        if (entityType.equals("Role")) return "Role";
        if (entityType.equals("PerformanceReview")) return "PerformanceReview";
        if (entityType.equals("Goal")) return "Goal";
        if (entityType.equals("Payroll")) return "Payroll";
        if (entityType.equals("Document")) return "Document";
        if (entityType.equals("Report")) return "Report";
        if (entityType.equals("Notification")) return "Notification";
        
        return entityType;
    }
    
    private Long extractEntityId(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            // Try to get ID using reflection
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception e) {
            // Ignore exceptions during ID extraction
        }
        
        return null;
    }
}