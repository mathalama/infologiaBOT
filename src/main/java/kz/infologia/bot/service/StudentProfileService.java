package kz.infologia.bot.service;

import kz.infologia.bot.model.StudentProfile;
import kz.infologia.bot.model.User;
import kz.infologia.bot.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    @Transactional(readOnly = true)
    public Optional<StudentProfile> findByTelegramId(Long telegramId) {
        return studentProfileRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public StudentProfile ensureProfile(User user) {
        return studentProfileRepository.findByTelegramId(user.getTelegramId())
                .orElseGet(() -> {
                    StudentProfile profile = StudentProfile.builder()
                            .telegramId(user.getTelegramId())
                            .user(user)
                            .curator(null)
                            .course(null)
                            .cohort(null)
                            .status(null)
                            .notes(null)
                            .build();
                    log.info("Created blank student profile for {}", user.getTelegramId());
                    return studentProfileRepository.save(profile);
                });
    }

    @Transactional
    public StudentProfile save(StudentProfile profile) {
        return studentProfileRepository.save(profile);
    }
}
