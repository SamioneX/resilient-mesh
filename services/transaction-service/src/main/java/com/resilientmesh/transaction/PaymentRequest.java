package com.resilientmesh.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

record PaymentRequest(@NotBlank String userId, @DecimalMin("0.01") BigDecimal amount) {
}
