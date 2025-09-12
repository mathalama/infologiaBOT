package kz.infologia.bot.service;

import kz.infologia.bot.model.User;
import kz.infologia.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public User saveOrUpdateUser(org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        Long telegramId = telegramUser.getId();
        
        Optional<User> existingUser = userRepository.findByTelegramId(telegramId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(telegramUser.getUserName());
            user.setFirstName(telegramUser.getFirstName());
            user.setLastName(telegramUser.getLastName());
            user.setLanguageCode(telegramUser.getLanguageCode());
            user.setIsBot(telegramUser.getIsBot());
            
            log.info("Обновлен пользователь: {}", user.getFirstName());
            return userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setUsername(telegramUser.getUserName());
            newUser.setFirstName(telegramUser.getFirstName());
            newUser.setLastName(telegramUser.getLastName());
            newUser.setLanguageCode(telegramUser.getLanguageCode());
            newUser.setIsBot(telegramUser.getIsBot());
            
            log.info("Создан новый пользователь: {}", newUser.getFirstName());
            return userRepository.save(newUser);
        }
    }
    
    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
    
    public boolean userExists(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }
}
// FIXME