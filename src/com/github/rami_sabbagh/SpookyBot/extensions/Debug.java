package com.github.rami_sabbagh.SpookyBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.List;

/**
 * This bot extension contains debug commands.
 */
public class Debug extends Extension {
    public Debug(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
    }

    @SuppressWarnings("unused")
    public Ability commandWhoAmI() {
        return Ability.builder()
                .name("whoami")
                .info("Who are you? 🤔")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    List<String> information = new ArrayList<>();
                    User from = ctx.user();

                    if (from.getFirstName() != null)
                        information.add("Your first name is `" + from.getFirstName() + "`");
                    if (from.getLastName() != null) information.add("Your last name is `" + from.getLastName() + "`");
                    if (from.getUserName() != null) information.add("Your username is @" + from.getUserName());
                    if (from.getLanguageCode() != null)
                        information.add("Your language code is `" + from.getLanguageCode() + "`");
                    if (from.getBot()) information.add("You belong to the family 🤖");

                    silent.sendMd(String.join("\n", information), ctx.chatId());
                })
                .enableStats()
                .build();
    }

    @SuppressWarnings("unused")
    public Ability commandWhoIsIt() {
        return Ability.builder()
                .name("whoisit")
                .info("Who is he/she/it? 🤔")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    if (!ctx.update().getMessage().isReply()) {
                        silent.send("This command has to be sent as a reply to a message.", ctx.chatId());
                        return;
                    }

                    List<String> information = new ArrayList<>();
                    User from = ctx.update().getMessage().getReplyToMessage().getFrom();

                    if (from.getFirstName() != null)
                        information.add("His/Her first name is `" + from.getFirstName() + "`");
                    if (from.getLastName() != null)
                        information.add("His/Her last name is `" + from.getLastName() + "`");
                    if (from.getUserName() != null) information.add("His/Her username is @" + from.getUserName());
                    if (from.getLanguageCode() != null)
                        information.add("His/Her language code is `" + from.getLanguageCode() + "`");
                    if (from.getBot()) information.add("He/She belongs to the family 🤖");

                    silent.sendMd(String.join("\n", information), ctx.chatId());
                })
                .enableStats()
                .build();
    }

    @SuppressWarnings("unused")
    public Ability commandTestFile() {
        return Ability.builder()
                .name("testfile")
                .info("GET THE TESTING FILE 📄")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    try {
                        bot.execute(new SendDocument().setChatId(ctx.chatId())
                                .setDocument("TEST.txt",
                                        Debug.class.getClassLoader().getResourceAsStream("TEST.txt"))
                        );
                    } catch (Exception e) {
                        silent.send("Failed to send testing file ⚠", ctx.chatId());
                        e.printStackTrace();
                    }
                })
                .enableStats()
                .build();
    }
}
