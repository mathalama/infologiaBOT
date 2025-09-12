package kz.infologia.bot.command;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class BaseCommand {
    
    protected void sendMessage(AbsSender absSender, long chatId, String text) {
        sendMessage(absSender, chatId, text, false);
    }
    
    protected void sendMessage(AbsSender absSender, long chatId, String text, boolean markdown) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        
        if (markdown) {
            message.setParseMode("Markdown");
        }

        try {
            absSender.execute(message);
            log.info("Message sent to chat {}: {}", chatId, text.substring(0, Math.min(50, text.length())));
        } catch (TelegramApiException e) {
            log.error("Error sending message: {}", e.getMessage());
        }
    }
}
