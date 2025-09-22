package kz.infologia.bot.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
@Component
public class AboutCommand extends BaseCommand {

    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChatId();
        
        String aboutMessage = "SALEM ALEM";
        
        sendMessage(absSender, chatId, aboutMessage, true);
    }
}
