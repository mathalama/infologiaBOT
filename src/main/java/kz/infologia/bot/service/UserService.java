package kz.infologia.bot.service;

import kz.infologia.bot.model.Role;
import kz.infologia.bot.model.User;
import kz.infologia.bot.repository.UserRepository;
import kz.infologia.bot.service.dto.AuthResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentProfileService studentProfileService;

    @Transactional
    public User saveOrUpdateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();

        User persistedUser = userRepository.findByTelegramId(telegramId)
                .map(existing -> updateExistingUser(existing, telegramUser))
                .orElseGet(() -> createNewUser(telegramUser));

        studentProfileService.ensureProfile(persistedUser);
        return persistedUser;
    }

    private User updateExistingUser(User user, org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        user.setUsername(telegramUser.getUserName());
        user.setFirstName(telegramUser.getFirstName());
        user.setLastName(telegramUser.getLastName());
        user.setLanguageCode(telegramUser.getLanguageCode());
        user.setIsBot(telegramUser.getIsBot());
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        if (user.getAuthorized() == null) {
            user.setAuthorized(Boolean.FALSE);
        }
        log.debug("Updated user profile for {} ({})", user.getFirstName(), user.getTelegramId());
        return userRepository.save(user);
    }

    private User createNewUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        User newUser = new User();
        newUser.setTelegramId(telegramUser.getId());
        newUser.setUsername(telegramUser.getUserName());
        newUser.setFirstName(telegramUser.getFirstName());
        newUser.setLastName(telegramUser.getLastName());
        newUser.setLanguageCode(telegramUser.getLanguageCode());
        newUser.setIsBot(telegramUser.getIsBot());
        newUser.setRole(Role.STUDENT);
        newUser.setAuthorized(Boolean.FALSE);
        log.info("Registered new Telegram user {} ({})", newUser.getFirstName(), newUser.getTelegramId());
        return userRepository.save(newUser);
    }

    @Transactional
    public AuthResult registerUser(Long telegramId, String rawPassword) {
        if (!StringUtils.hasText(rawPassword) || rawPassword.trim().length() < MIN_PASSWORD_LENGTH) {
            return new AuthResult(false, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }

        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return new AuthResult(false, "User profile not found. Send any message first to initialize your account.");
        }

        User user = userOpt.get();
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        if (StringUtils.hasText(user.getPasswordHash())) {
            return new AuthResult(false, "Account already registered. Use /login to authenticate.");
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
        user.setAuthorized(Boolean.TRUE);
        userRepository.save(user);

        return new AuthResult(true, "Registration successful. You are now logged in.");
    }

    @Transactional
    public AuthResult authenticateUser(Long telegramId, String rawPassword) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return new AuthResult(false, "User profile not found. Send any message first to initialize your account.");
        }

        User user = userOpt.get();
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        if (!StringUtils.hasText(user.getPasswordHash())) {
            return new AuthResult(false, "Account is not registered yet. Use /register <password> first.");
        }

        if (!StringUtils.hasText(rawPassword)) {
            return new AuthResult(false, "Provide your password: /login <password>.");
        }

        if (!passwordEncoder.matches(rawPassword.trim(), user.getPasswordHash())) {
            return new AuthResult(false, "Password is incorrect.");
        }

        user.setAuthorized(Boolean.TRUE);
        userRepository.save(user);

        return new AuthResult(true, "Authentication successful. Welcome back!");
    }

    @Transactional
    public AuthResult logout(Long telegramId) {
        Optional<User> userOpt = userRepository.findByTelegramId(telegramId);
        if (userOpt.isEmpty()) {
            return new AuthResult(false, "User profile not found.");
        }

        User user = userOpt.get();
        if (user.getRole() == null) {
            user.setRole(Role.STUDENT);
        }
        if (!Boolean.TRUE.equals(user.getAuthorized())) {
            return new AuthResult(false, "You are not logged in.");
        }

        user.setAuthorized(Boolean.FALSE);
        userRepository.save(user);

        return new AuthResult(true, "You have been logged out.");
    }

    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public boolean userExists(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    public boolean isAuthorized(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(user -> Boolean.TRUE.equals(user.getAuthorized()))
                .orElse(false);
    }

    public Role getRole(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(User::getRole)
                .orElse(Role.STUDENT);
    }
}
// FIXME
