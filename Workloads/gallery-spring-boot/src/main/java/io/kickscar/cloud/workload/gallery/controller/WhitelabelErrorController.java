package io.kickscar.cloud.workload.gallery.controller;

import io.kickscar.cloud.workload.gallery.dto.JsonResult;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Optional;

@RestController
@RequestMapping("/error")
public class WhitelabelErrorController implements ErrorController {

    /* from GlobalExceptionHandler */
    @RequestMapping("/404")
    public ResponseEntity<JsonResult<?>> _404() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(JsonResult.fail("Unknown URL"));
    }

    @RequestMapping("/500")
    public ResponseEntity<JsonResult<?>> _500(@RequestAttribute String errors) {
        return ResponseEntity
                .internalServerError()
                .body(JsonResult.fail(errors));
    }

    /* from Whitelabel(Embeded Tomcat) */
    @GetMapping
    public ResponseEntity<JsonResult<?>> handlerError(HttpServletRequest request) {
        return ResponseEntity
                .status(Optional
                    .ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                    .map(statusCode -> Integer.parseInt(statusCode.toString()))
                    .orElse(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .body(JsonResult
                        .fail(Optional
                            .ofNullable(request.getAttribute(RequestDispatcher.ERROR_MESSAGE))
                            .map(Object::toString)
                            .orElse("Unexpected Error")));
    }
}
