package kz.infologia.bot.bot;

import kz.infologia.bot.command.AboutCommand;
import kz.infologia.bot.command.ProfileCommand;
import kz.infologia.bot.command.QuoteCommand;
import kz.infologia.bot.config.BotConfig;
import kz.infologia.bot.service.UserService;
import kz.infologia.bot.service.dto.AuthResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InfologiaBot extends TelegramLongPollingBot {

    private static final int LOG_PREVIEW_LENGTH = 80;

    private final BotConfig botConfig;
    private final UserService userService;
    private final AboutCommand aboutCommand;
    private final QuoteCommand quoteCommand;
    private final ProfileCommand profileCommand;

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();
        long telegramId = update.getMessage().getFrom().getId();
        String firstName = update.getMessage().getFrom().getFirstName();

        userService.saveOrUpdateUser(update.getMessage().getFrom());

        log.info("Incoming message from {} ({}): {}", firstName, telegramId, preview(messageText));

        if (messageText.startsWith("/")) {
            handleCommand(chatId, telegramId, messageText, update);
        } else {
            handlePlainText(chatId, telegramId);
        }
    }

    private void handlePlainText(long chatId, long telegramId) {
        if (userService.isAuthorized(telegramId)) {
            sendMessage(chatId, "Your message has been received. Use /help to see available commands.");
        } else {
            sendMessage(chatId, unauthorizedMessage(), true);
        }
    }

    private void handleCommand(long chatId, long telegramId, String messageText, Update update) {
        String[] parts = messageText.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "/start":
                sendMessage(chatId, startMessage(), true);
                break;
            case "/help":
                sendMessage(chatId, helpMessage(), true);
                break;
            case "/register":
                handleRegister(chatId, telegramId, parts);
                break;
            case "/login":
                handleLogin(chatId, telegramId, parts);
                break;
            case "/logout":
                handleLogout(chatId, telegramId);
                break;
            case "/status":
                sendMessage(chatId, statusMessage(telegramId), true);
                break;
            case "/about":
                aboutCommand.execute(this, update);
                break;
            case "/quote":
                if (ensureAuthorized(chatId, telegramId)) {
                    quoteCommand.execute(this, update);
                }
                break;
            case "/info":
                if (ensureAuthorized(chatId, telegramId)) {
                    sendMessage(chatId, infoMessage(telegramId), true);
                }
                break;
            case "/profile":
                if (ensureAuthorized(chatId, telegramId)) {
                    profileCommand.execute(this, update);
                }
                break;
            default:
                if (ensureAuthorized(chatId, telegramId)) {
                    sendMessage(chatId, "Unknown command. Use /help to see the list of available commands.");
                }
                break;
        }
    }

    private void handleRegister(long chatId, long telegramId, String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            sendMessage(chatId, "Usage: /register <password>");
            return;
        }

        AuthResult result = userService.registerUser(telegramId, parts[1].trim());
        sendMessage(chatId, result.message(), !result.success());
    }

    private void handleLogin(long chatId, long telegramId, String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            sendMessage(chatId, "Usage: /login <password>");
            return;
        }

        AuthResult result = userService.authenticateUser(telegramId, parts[1].trim());
        sendMessage(chatId, result.message(), !result.success());
    }

    private void handleLogout(long chatId, long telegramId) {
        AuthResult result = userService.logout(telegramId);
        sendMessage(chatId, result.message(), !result.success());
    }

    private boolean ensureAuthorized(long chatId, long telegramId) {
        if (userService.isAuthorized(telegramId)) {
            return true;
        }

        sendMessage(chatId, unauthorizedMessage(), true);
        return false;
    }

    private String unauthorizedMessage() {
        return "*Authorization required*\n" +
                "Register with `/register <password>` or login with `/login <password>` to continue.";
    }

    private String startMessage() {
        return "*Welcome to Infologia Bot!*\n" +
                "Use `/register <password>` to create an account or `/login <password>` if you already have one.";
    }

    private String helpMessage() {
        return "*Available commands*\n" +
                "/start - introduction\n" +
                "/register <password> - register and log in\n" +
                "/login <password> - log in\n" +
                "/logout - log out\n" +
                "/status - show your authentication status\n" +
                "/about - learn about the bot\n" +
                "/quote - get a protected quote\n" +
                "/profile [telegram_id] - show your student profile (curators/admins can pass ID)\n" +
                "/info - show stored profile information";
    }

    private String statusMessage(long telegramId) {
        boolean authorized = userService.isAuthorized(telegramId);
        String role = userService.getRole(telegramId).name();
        return "*Status:* " + (authorized ? "authorized" : "guest") + "\n" +
                "*Role:* " + role;
    }

    private String infoMessage(long telegramId) {
        return userService.findByTelegramId(telegramId)
                .map(user -> "*Telegram ID:* " + user.getTelegramId() + "\n" +
                        "*Username:* " + valueOrPlaceholder(user.getUsername()) + "\n" +
                        "*First name:* " + valueOrPlaceholder(user.getFirstName()) + "\n" +
                        "*Last name:* " + valueOrPlaceholder(user.getLastName()) + "\n" +
                        "*Language:* " + valueOrPlaceholder(user.getLanguageCode()) + "\n" +
                        "*Role:* " + user.getRole())
                .orElse("User profile not found.");
    }

    private String valueOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

    private void sendMessage(long chatId, String text) {
        sendMessage(chatId, text, false);
    }

    private void sendMessage(long chatId, String text, boolean markdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        if (markdown) {
            message.setParseMode("Markdown");
        }

        try {
            execute(message);
            log.info("Sent message to chat {}: {}", chatId, preview(text));
        } catch (TelegramApiException e) {
            log.error("Failed to send message: {}", e.getMessage());
        }
    }

    private String preview(String text) {
        if (text.length() <= LOG_PREVIEW_LENGTH) {
            return text;
        }
        return text.substring(0, LOG_PREVIEW_LENGTH) + "...";
    }
}
