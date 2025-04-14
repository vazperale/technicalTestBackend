package com.technical_test_backend.technical_test_backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        // Crear un mapa con los detalles que queremos devolver
        Map<String, Object> response = new HashMap<>();
        response.put("status", ex.getStatusCode().value()); 
        response.put("message", ex.getReason());          

        return new ResponseEntity<>(response, ex.getStatusCode());
    }
}