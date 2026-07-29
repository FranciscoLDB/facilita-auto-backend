package com.fldb.facilita.auto.api.exception;

import com.fldb.facilita.auto.api.exception.model.ApiResponseError;
import com.fldb.facilita.auto.api.exception.model.GeneralErrorItem;
import com.fldb.facilita.auto.api.exception.model.ValidationErrorItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Corpo da requisição ausente ou inválido.")
                .detailedMessage("O corpo da requisição (JSON) não foi fornecido ou contém erros de sintaxe.")
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseError> handleResourceNotFoundException(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        GeneralErrorItem errorItem = null;
        if (ex.getErrorCode() != null) {
            errorItem = GeneralErrorItem.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .build();
        }

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Recurso não encontrado.")
                .detailedMessage(ex.getMessage())
                .errors(errorItem != null ? List.of(errorItem) : null)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseError> handleBusinessException(BusinessException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        GeneralErrorItem errorItem = null;
        if (ex.getErrorCode() != null) {
            errorItem = GeneralErrorItem.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .build();
        }

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Regra de negócio violada.")
                .detailedMessage(ex.getMessage())
                .errors(errorItem != null ? List.of(errorItem) : null)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<ValidationErrorItem> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ValidationErrorItem.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Erro de validação.")
                .detailedMessage("Um ou mais campos informados na requisição são inválidos.")
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponseError> handleSystemException(SystemException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        GeneralErrorItem errorItem = null;
        if (ex.getErrorCode() != null) {
            errorItem = GeneralErrorItem.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .build();
        }

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Erro interno no servidor.")
                .detailedMessage(ex.getMessage())
                .errors(errorItem != null ? List.of(errorItem) : null)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseError> handleDataAccessException(DataAccessException ex) {
        // Log do erro real para auditoria no console/arquivos de log
        log.error("Erro de acesso ao banco de dados: ", ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Falha na comunicação com o banco de dados.")
                .detailedMessage("Não foi possível concluir a operação devido a uma indisponibilidade ou falha temporária no banco de dados.")
                .timestamp(OffsetDateTime.now())
                .errors(List.of(
                        GeneralErrorItem.builder()
                                .code(ErrorCode.DATABASE_ERROR) // SYS-501
                                .message(ErrorCode.DATABASE_ERROR.getDefaultMessage())
                                .build()
                ))
                .build();

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseError> handleGenericException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        GeneralErrorItem errorItem = GeneralErrorItem.builder()
                .code(ErrorCode.INTERNAL_ERROR)
                .message("Ocorreu um erro inesperado no sistema.")
                .build();

        ApiResponseError response = ApiResponseError.builder()
                .statusCode(status.value())
                .statusMessage(status.name())
                .message("Erro não tratado.")
                .detailedMessage(ex.getMessage())
                .errors(List.of(errorItem))
                .build();

        return ResponseEntity.status(status).body(response);
    }
}