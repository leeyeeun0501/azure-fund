package io.kickscar.cloud.workload.gallery.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public void handlerNoHandlerFoundException(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        return request.getHeader("accept").matches(".*application/json.*") ? "forward:/error/404" : "index";
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/pain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print("No Handler Found");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handlerNoResourceFoundException(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("text/pain");
        response.setCharacterEncoding("utf-8");
        response.getWriter().print("No Resource Found");
    }

    @ExceptionHandler(Exception.class)
    public String handler(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        log.error(errors.toString());

        request.setAttribute("errors", errors);
        return "forward:/error/500";
    }
}
