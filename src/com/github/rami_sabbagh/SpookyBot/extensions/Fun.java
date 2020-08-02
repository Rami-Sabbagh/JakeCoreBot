package com.github.rami_sabbagh.SpookyBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;

/**
 * This bot extension contains fun commands.
 */
public class Fun extends Extension {
    public Fun(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
    }

    @SuppressWarnings("unused")
    public Ability commandDice() {
        return Ability.builder()
                .name("dice")
                .info("Roll a dice 🎲")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.execute(new SendDice().setChatId(ctx.chatId()).setEmoji("🎲")))
                .enableStats()
                .build();
    }

    @SuppressWarnings("unused")
    public Ability commandDart() {
        return Ability.builder()
                .name("dart")
                .info("Throw a dart 🎯")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.execute(new SendDice().setChatId(ctx.chatId()).setEmoji("🎯")))
                .enableStats()
                .build();
    }

    @SuppressWarnings("unused")
    public Ability commandBasket() {
        return Ability.builder()
                .name("basket")
                .info("Throw a basketball 🏀")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.execute(new SendDice().setChatId(ctx.chatId()).setEmoji("🏀")))
                .enableStats()
                .build();
    }
}
