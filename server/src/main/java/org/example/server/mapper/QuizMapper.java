package org.example.server.mapper;

import org.example.server.dto.quiz.ChoiceDTO;
import org.example.server.dto.quiz.QuestionDTO;
import org.example.server.dto.quiz.QuizResponseDTO;
import org.example.server.model.Choice;
import org.example.server.model.Question;
import org.example.server.model.Quiz;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public interface QuizMapper {

    QuizResponseDTO toResponse(Quiz quiz);

    @Component
    class Impl implements QuizMapper {

        @Override
        public QuizResponseDTO toResponse(Quiz quiz) {
            if (quiz == null) return null;

            List<QuestionDTO> questionDTOs = quiz.getQuestions().stream()
                .sorted(Comparator.comparing(Question::getOrderIndex))
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());

            return new QuizResponseDTO(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getPocketMoneyPerQuestion(),
                quiz.getPassPercent(),
                questionDTOs
            );
        }

        private QuestionDTO toQuestionDTO(Question question) {
            List<ChoiceDTO> choiceDTOs = question.getChoices().stream()
                .sorted(Comparator.comparing(Choice::getOrderIndex))
                .map(this::toChoiceDTO)
                .collect(Collectors.toList());

            return new QuestionDTO(
                question.getId(),
                question.getText(),
                choiceDTOs
            );
        }

        private ChoiceDTO toChoiceDTO(Choice choice) {
            return new ChoiceDTO(
                choice.getChoiceId(),
                choice.getText()
            );
        }
    }
}
