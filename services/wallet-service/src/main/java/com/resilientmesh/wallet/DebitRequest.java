package com.resilientmesh.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record DebitRequest(@NotNull @DecimalMin("0.01") BigDecimal amount) {
}
