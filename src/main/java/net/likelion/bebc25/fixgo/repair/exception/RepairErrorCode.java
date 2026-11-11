package net.likelion.bebc25.fixgo.repair.exception;

import net.likelion.bebc25.fixgo.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum RepairErrorCode implements ErrorCode {

    // 수리 기본 예외
    REPAIR_NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_SAME_REPAIR_STATUS(HttpStatus.CONFLICT),
    INVALID_REPAIR_STATUS_TRANSITION(HttpStatus.CONFLICT),
    CHANGED_BY_NOT_FOUND(HttpStatus.NOT_FOUND),

    // 수리비 계산 예외
    PRODUCT_MODEL_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_REPAIR_DECISION(HttpStatus.BAD_REQUEST),
    WARRANTY_RESULT_UNDECIDED(HttpStatus.CONFLICT),
    WARRANTY_FREE_NOT_ALLOWED(HttpStatus.CONFLICT),
    LABOR_RATE_NOT_FOUND(HttpStatus.NOT_FOUND),

    // 수리비 확정 예외
    REPAIR_COST_TECHNICIAN_ONLY(HttpStatus.FORBIDDEN),
    NOT_ASSIGNED_TECHNICIAN(HttpStatus.FORBIDDEN),
    REPAIR_COST_NOT_DIAGNOSING(HttpStatus.CONFLICT),
    REPAIR_COST_ALREADY_CONFIRMED(HttpStatus.CONFLICT),
    REPAIR_COST_NOT_CONFIRMED(HttpStatus.CONFLICT),

    // 고객 견적 승인/거부 예외
    QUOTE_NOT_PENDING(HttpStatus.CONFLICT),

    // A/S 접수 예외
    AS_RECEPTION_NOT_FOUND(HttpStatus.NOT_FOUND);

    private final HttpStatus httpStatus;

    RepairErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }
}
