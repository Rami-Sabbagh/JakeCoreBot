package com.github.rami_sabbagh.SpookyBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Contains reminder features like <i>timers</i>, which are set by the users.
 */
public class Reminder extends Extension {

    /**
     * The regex pattern used for validating the duration response from the users.
     * Example valid responses: 12:34:56, 1:2:3, 1:22:3, 0:0:0.
     */
    public static final Pattern timerPattern = Pattern.compile("^\\d{1,2}:\\d{1,2}:\\d{1,2}$");

    private static final String cancelCallbackData = "CANCEL_TIMER";

    public final ReminderService reminderService;

    public Reminder(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
        reminderService = new ReminderService(bot, silent, db);

        Map<String, Instant> timers = db.getMap("TIMERS");

        //Reschedule the timers.
        for (Map.Entry<String, Instant> entry : timers.entrySet())
            reminderService.scheduleTimer(Long.parseLong(entry.getKey()), entry.getValue());
    }

    @SuppressWarnings("unused")
    public Ability commandTimer() {
        Map<String, Integer> interactive = db.getMap("INTERACTIVE");
        Map<String, Instant> timers = db.getMap("TIMERS");

        InlineKeyboardButton cancelButton = new InlineKeyboardButton().setText("Cancel Timer ⌛").setCallbackData(cancelCallbackData);
        InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup();
        {
            ArrayList<InlineKeyboardButton> cancelRow = new ArrayList<>();
            cancelRow.add(cancelButton);
            cancelKeyboard.getKeyboard().add(cancelRow);
        }

        return Ability.builder()
                .name("timer")
                .info("Set a timer ⏲")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    //Check if a timer was already set.
                    String chatID = ctx.chatId().toString();
                    if (timers.containsKey(chatID)) {
                        Duration remaining = Duration.between(Instant.now(), timers.get(chatID));
                        //Check if it has expired
                        if (remaining.isNegative() || remaining.isZero() || remaining.toSeconds() == 0)
                            timers.remove(chatID); //Remove it and continue with the timer creation flow.
                        else {
                            //The timer is already set and still not met, display the remaining time.
                            ArrayList<String> measures = new ArrayList<>(3);

                            long days = remaining.toDays();
                            int hours = remaining.toHoursPart();
                            int minutes = remaining.toMinutesPart();
                            int seconds = remaining.toSecondsPart();

                            if (days != 0) measures.add(days + " days");
                            if (hours != 0) measures.add(hours + " hours");
                            if (minutes != 0) measures.add(minutes + " minutes");
                            if (seconds != 0) measures.add(seconds + " seconds");

                            try {
                                bot.execute(new SendMessage()
                                        .setChatId(ctx.chatId())
                                        .setText(String.format("Remaining time: `%s`", String.join("`, `", measures)))
                                        .enableMarkdown(true)
                                        .setReplyMarkup(cancelKeyboard)
                                );
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }

                            //Don't follow the creation flow, abort command execution.
                            return;
                        }
                    }

                    //Creation stage 1: prompt to input the duration.
                    try {
                        Message message = bot.execute(new SendMessage()
                                .setChatId(ctx.chatId())
                                .setReplyToMessageId(ctx.update().getMessage().getMessageId())
                                .setReplyMarkup(new ForceReplyKeyboard().setSelective(true))
                                .setText("Please send the duration in `HH:MM:SS` format ⏰")
                                .enableMarkdown(true)
                        );

                        //Set the sent message ID as the filter for reply messages.
                        interactive.put(ctx.chatId().toString(), message.getMessageId());
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                })
                .reply(upd -> {
                            Message message = upd.getMessage();
                            String text = message.getText();

                            //Check if it's a valid time.
                            if (timerPattern.matcher(text).matches()) {
                                //If so, decompose it,
                                String[] fields = text.split(":");
                                int hours = Integer.parseInt(fields[0]);
                                int minutes = Integer.parseInt(fields[1]);
                                int seconds = Integer.parseInt(fields[2]);

                                //Create a duration then an Instant,
                                Duration duration = Duration.ofSeconds(seconds + minutes * 60 + hours * 3600);
                                Instant trigger = Instant.now().plus(duration);

                                //Register the timer,
                                timers.put(message.getChatId().toString(), trigger);

                                //Schedule the timer updates.
                                reminderService.scheduleTimer(message.getChatId(), trigger);

                                //And confirm the timer to the user.
                                silent.send("The timer has been set successfully ✅", message.getChatId());

                                //Clear the interactive message ID cause we're done.
                                interactive.remove(message.getChatId().toString());
                            } else {
                                //It's an invalid duration, complain to the user and ask him to input again.
                                try {
                                    Message response = bot.execute(new SendMessage()
                                            .setChatId(message.getChatId())
                                            .setReplyToMessageId(message.getMessageId())
                                            .setReplyMarkup(new ForceReplyKeyboard().setSelective(true))
                                            .setText("Invalid time ⚠\nPlease send the time in format `HH:MM:SS`❕")
                                            .enableMarkdown(true)
                                    );

                                    //Update the last message ID.
                                    interactive.replace(message.getChatId().toString(), response.getMessageId());
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        isInteractive()) //Validates that the message is reply to the last message by the bot.
                .reply(upd -> {
                    CallbackQuery query = upd.getCallbackQuery();
                    Message message = query.getMessage();
                    EditMessageReplyMarkup clearMarkup = new EditMessageReplyMarkup()
                            .setChatId(message.getChatId())
                            .setMessageId(message.getMessageId());


                    if (!timers.containsKey(String.valueOf(message.getChatId()))) {
                        //Clear the inline keyboard markup from the message.
                        silent.execute(clearMarkup);
                        //Reply that the timer has already expired.
                        silent.execute(new AnswerCallbackQuery()
                            .setCallbackQueryId(query.getId())
                            .setText("The timer has already expired ⚠"));
                    } else {
                        //Attempt to cancel the timer
                        if (reminderService.cancelTimer(message.getChatId())) {
                            //Clear the inline keyboard markup form the message.
                            silent.execute(clearMarkup);
                            //Reply that the timer has been cancelled successfully.
                            silent.execute(new AnswerCallbackQuery()
                                .setCallbackQueryId(query.getId())
                                .setText("Cancelled the timer successfully ✅"));
                            //Send a message so the other group users know.
                            silent.send("Cancelled the timer successfully ✅", message.getChatId());
                        } else {
                            //Reply that we failed, and he has to try again.
                            silent.execute(new AnswerCallbackQuery()
                                .setCallbackQueryId(query.getId())
                                .setText("Failed to cancel the timer while it's dispatching an update ⚠\nPlease try again ℹ"));
                        }
                    }


                },
                isCancelTimer()) //Validates that the callback query is for canceling a timer.
                .enableStats()
                .build();
    }

    /**
     * Checks if the update is a message, which is a reply to the last interactive message sent by the bot in the chat.
     *
     * @return the filter predicate, use it with the AbilityBuilder replies.
     */
    private Predicate<Update> isInteractive() {
        Map<String, Integer> interactive = db.getMap("INTERACTIVE");

        return upd -> {
            if (!upd.hasMessage()) return false; //It's a message,
            if (!upd.getMessage().isReply()) return false; //That's an reply,
            String chatID = upd.getMessage().getChatId().toString(); //To which chat,
            Integer replyingToMessageID = upd.getMessage().getReplyToMessage().getMessageId(); //And which message,
            if (!interactive.containsKey(chatID)) return false; //Check if the bot has send an interactive message,
            return interactive.get(chatID).equals(replyingToMessageID); //Match the reply with the interactive message.
        };
    }

    /**
     * Checks if the update is the callback query of the cancel timer button.
     *
     * @return the filter predicate, use it with the AbilityBuilder replies.
     */
    private Predicate<Update> isCancelTimer() {
        return upd -> {
            if (!upd.hasCallbackQuery()) return false; //We want a callback query.
            CallbackQuery query = upd.getCallbackQuery();
            if (query.getData() == null) return false; //It has to contain query data.
            if (query.getMessage() == null) return false; //Not a callback query for a message.
            return query.getData().equals(cancelCallbackData); //Matches the data of the cancel timer button.
        };
    }
}
