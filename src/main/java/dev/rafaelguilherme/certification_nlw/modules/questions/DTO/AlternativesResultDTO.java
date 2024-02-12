package dev.rafaelguilherme.certification_nlw.modules.questions.DTO;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativesResultDTO {
    
    private UUID id;
    private String description;
}
