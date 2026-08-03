package io.restaurant.platform.modules.table.dto.response;

import io.restaurant.platform.modules.table.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTableResponse {
    private Long id;
    private Long restaurantId;
    private Integer number;
    private Integer capacity;
    private TableStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
