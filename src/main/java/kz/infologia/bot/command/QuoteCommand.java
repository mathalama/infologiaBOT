package kz.infologia.bot.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class QuoteCommand extends BaseCommand {

    private final List<String> quotes = Arrays.asList("examplesOfQuote");

    private final Random random = new Random();

    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChatId();
        
        String quote = quotes.get(random.nextInt(quotes.size()));
        
        String message = "FIXME";
        
        sendMessage(absSender, chatId, message, true);
    }
}
