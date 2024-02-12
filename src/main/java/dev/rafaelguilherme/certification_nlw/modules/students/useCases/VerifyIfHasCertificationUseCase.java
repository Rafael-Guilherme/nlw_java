package dev.rafaelguilherme.certification_nlw.modules.students.useCases;

import org.springframework.stereotype.Service;

import dev.rafaelguilherme.certification_nlw.modules.students.controllers.dto.VerifyHasCertificationDTO;

@Service
public class VerifyIfHasCertificationUseCase {
    
    public boolean execute( VerifyHasCertificationDTO dto ) {
        if (dto.getEmail().equals("rafael@email.com") && dto.getTechnology().equals("Nodejs")) {
            return true;
        }

        return false;
    }
}
