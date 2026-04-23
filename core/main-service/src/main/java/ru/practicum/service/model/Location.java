package ru.practicum.service.model;

import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private Double lat;
    private Double lon;
}
