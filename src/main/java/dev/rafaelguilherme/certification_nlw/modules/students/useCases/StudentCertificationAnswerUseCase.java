package dev.rafaelguilherme.certification_nlw.modules.students.useCases;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.rafaelguilherme.certification_nlw.modules.questions.entities.QuestionEntity;
import dev.rafaelguilherme.certification_nlw.modules.questions.repositories.QuestionRepository;
import dev.rafaelguilherme.certification_nlw.modules.students.controllers.dto.StudentCertificationAnswerDTO;
import dev.rafaelguilherme.certification_nlw.modules.students.controllers.dto.VerifyHasCertificationDTO;
import dev.rafaelguilherme.certification_nlw.modules.students.entities.AnswerCertificationEntity;
import dev.rafaelguilherme.certification_nlw.modules.students.entities.CertificationStudentEntity;
import dev.rafaelguilherme.certification_nlw.modules.students.entities.StudentEntity;
import dev.rafaelguilherme.certification_nlw.modules.students.repositories.CertificationStudentRepository;
import dev.rafaelguilherme.certification_nlw.modules.students.repositories.StudentRepository;

@Service
public class StudentCertificationAnswerUseCase {
    
    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CertificationStudentRepository certificationStudentRepository;

    @Autowired
    private VerifyIfHasCertificationUseCase verifyIfHasCertificationUseCase;

    public CertificationStudentEntity execute(StudentCertificationAnswerDTO dto) throws Exception {
        
        var hasCertification = this.verifyIfHasCertificationUseCase
                .execute(new VerifyHasCertificationDTO(dto.getEmail(), dto.getTechnology()));

        if (hasCertification) {
            throw new Exception("Você já tirou sua certificação!");
        }

        List<QuestionEntity> questionsEntity =  questionRepository.findByTechnology(dto.getTechnology());
        List<AnswerCertificationEntity> answersCertification = new ArrayList<>();

        AtomicInteger correctAnswers = new AtomicInteger(0);

        dto.getQuestionsAnswers()
            .stream().forEach(questionAnswer -> {
                var question = questionsEntity.stream().filter(q -> q.getId().equals(questionAnswer.getQuestionID()))
                .findFirst().get();

                var findCorrectAlternative = question.getAlternatives().stream()
                .filter(alternative -> alternative.isCorrect()).findFirst().get();

                if(findCorrectAlternative.getId().equals(questionAnswer.getAlternativeID())) {
                    questionAnswer.setCorrect(true);
                    correctAnswers.incrementAndGet();
                } else {
                    questionAnswer.setCorrect(false);
                }

                AnswerCertificationEntity answerCertificationEntity = AnswerCertificationEntity.builder()
                    .answerID(questionAnswer.getAlternativeID())
                    .questionID(questionAnswer.getQuestionID())
                    .isCorrect(questionAnswer.isCorrect()).build();

                answersCertification.add(answerCertificationEntity);
            });

        var student = studentRepository.findByEmail(dto.getEmail());
        UUID studentID;
        if(!student.isEmpty()) {
            var studentCreated = StudentEntity.builder().email(dto.getEmail()).build();
            studentCreated = studentRepository.save(studentCreated);
            studentID = studentCreated.getId();
        } else {
            studentID = student.get().getId();
        }
    
        CertificationStudentEntity certificationStudentEntity = CertificationStudentEntity.builder()
            .technology(dto.getTechnology())
            .studentID(studentID)
            .grade(correctAnswers.get())
            .build();

        var certificationStudentCreated =  certificationStudentRepository.save(certificationStudentEntity);

        answersCertification.stream().forEach(answerCertification -> {
            answerCertification.setCertificationID(certificationStudentEntity.getId());
            answerCertification.setCertificationStudentEntity(certificationStudentEntity);
        });
        
        certificationStudentEntity.setAnswersCertificationsEntities((answersCertification));

        certificationStudentRepository.save(certificationStudentEntity);
        
        return certificationStudentCreated;
    }
}