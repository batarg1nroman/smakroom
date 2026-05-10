package com.smakroom.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles exceptions from MVC controllers — returns HTML error pages.
 * REST controllers have their own handler: RestExceptionHandler.
 */
@ControllerAdvice(basePackages = {
    "com.smakroom.auth",
    "com.smakroom.home",
    "com.smakroom.user",
    "com.smakroom.product.controller",
    "com.smakroom.order.controller",
    "com.smakroom.admin"
})
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Not found: {} — {}", ex.getMessage(), request.getDescription(false));
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Business error: {} — {}", ex.getMessage(), request.getDescription(false));
        ModelAndView mav = new ModelAndView("error/business");
        mav.addObject("message", ex.getMessage());
        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ModelAndView("error/403");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneral(Exception ex, WebRequest request) {
        log.error("Unexpected error: {} — {}", ex.getMessage(), request.getDescription(false), ex);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("message", "Внутренняя ошибка сервера");
        return mav;
    }
}
