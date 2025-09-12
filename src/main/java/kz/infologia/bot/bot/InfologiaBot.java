package kz.infologia.bot.bot;

import kz.infologia.bot.command.*;
import kz.infologia.bot.config.BotConfig;
import kz.infologia.bot.model.User;
import kz.infologia.bot.service.UserService;
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

    private final BotConfig botConfig;
    private final UserService userService;
    private final AboutCommand aboutCommand;
    private final QuoteCommand quoteCommand;

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();

            // Сохраняем или обновляем информацию о пользователе
            User user = userService.saveOrUpdateUser(update.getMessage().getFrom());

            log.info("Получено сообщение от {} (ID: {}): {}", firstName, user.getTelegramId(), messageText);

            // Обработка команд
            if (messageText.startsWith("/")) {
                handleCommand(chatId, messageText, update);
            } else {
                // Обработка обычных сообщений
                sendMessage(chatId, "Привет! 👋\n\n" +
                        "Я получил твое сообщение: \"" + messageText + "\"\n\n" +
                        "Используй /help для просмотра всех доступных команд!\n" +
                        "Удачи в подготовке к ЕНТ! 🎓");
            }
        }
    }

    private void handleCommand(long chatId, String messageText, Update update) {
        String command = messageText.split(" ")[0].toLowerCase();
        
        switch (command) {
            case "/start":
                sendMessage(chatId, "startFIXME", true);
                break;
                
            case "/help":
                sendMessage(chatId, "helpFIXME", true);
                break;
                
            case "/about":
                aboutCommand.execute(this, update);
                break;
                
            case "/quote":
                quoteCommand.execute(this, update);
                break;
                
            case "/info":
                sendMessage(chatId, "infoFIXME", true);
                break;
                
            default:
                sendMessage(chatId, "defaultFIXME", true);
                break;
        }
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
            log.info("Отправлено сообщение в чат {}: {}", chatId, text.substring(0, Math.min(50, text.length())));
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage());
        }
    }
}
