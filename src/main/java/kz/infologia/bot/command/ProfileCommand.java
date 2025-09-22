package kz.infologia.bot.command;

import kz.infologia.bot.model.Role;
import kz.infologia.bot.model.StudentProfile;
import kz.infologia.bot.model.User;
import kz.infologia.bot.service.StudentProfileService;
import kz.infologia.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class ProfileCommand extends BaseCommand {

    private final UserService userService;
    private final StudentProfileService studentProfileService;

    public void execute(AbsSender absSender, Update update) {
        Long chatId = update.getMessage().getChatId();
        Long requesterId = update.getMessage().getFrom().getId();
        String[] parts = update.getMessage().getText().trim().split("\\s+", 2);

        Long targetId = requesterId;

        if (parts.length > 1 && StringUtils.hasText(parts[1])) {
            if (canInspectForeignProfiles(requesterId)) {
                try {
                    targetId = Long.parseLong(parts[1].trim());
                } catch (NumberFormatException ex) {
                    sendMessage(absSender, chatId, "Provide the telegram_id as digits: /profile 123456789");
                    return;
                }
            } else {
                sendMessage(absSender, chatId, "Access denied. Only curators and administrators can inspect another profile.");
                return;
            }
        }

        User targetUser = userService.findByTelegramId(targetId).orElse(null);
        if (targetUser == null) {
            sendMessage(absSender, chatId, "User with such Telegram ID is not registered.");
            return;
        }

        StudentProfile profile = studentProfileService.ensureProfile(targetUser);

        String message = buildProfileMessage(targetUser, profile);
        sendMessage(absSender, chatId, message, true);
    }

    private boolean canInspectForeignProfiles(Long requesterId) {
        Role role = userService.getRole(requesterId);
        return role == Role.CURATOR || role == Role.ADMIN;
    }

    private String buildProfileMessage(User user, StudentProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("*Student profile*\n");
        sb.append("*Telegram ID:* ").append(user.getTelegramId()).append("\n");
        sb.append("*Name:* ").append(valueOrPlaceholder(user.getFirstName())).append(" ")
                .append(valueOrPlaceholder(user.getLastName())).append("\n");
        sb.append("*Username:* ").append(valueOrPlaceholder(user.getUsername())).append("\n");
        sb.append("*Role:* ").append(user.getRole()).append("\n\n");

        sb.append("*Course:* ").append(valueOrPlaceholder(profile.getCourse())).append("\n");
        sb.append("*Cohort:* ").append(valueOrPlaceholder(profile.getCohort())).append("\n");
        sb.append("*Status:* ").append(valueOrPlaceholder(profile.getStatus())).append("\n");
        sb.append("*Curator:* ").append(valueOrPlaceholder(profile.getCurator())).append("\n");
        sb.append("*Notes:* ").append(valueOrPlaceholder(profile.getNotes())).append("\n");

        return sb.toString();
    }

    private String valueOrPlaceholder(String value) {
        return StringUtils.hasText(value) ? value : "n/a";
    }
}
