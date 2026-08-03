package io.restaurant.platform.modules.table.dto.request;

import io.restaurant.platform.modules.table.enums.TableStatus;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTableRequest {

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private TableStatus status;
}
