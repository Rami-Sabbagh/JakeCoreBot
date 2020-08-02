package com.github.rami_sabbagh.JakeCoreBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;

/**
 * This bot extension is meant to provide basic commands.
 */
public class Basic extends Extension {
    public Basic(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
    }

    @SuppressWarnings("unused")
    public Ability commandPing() {
        return Ability.builder()
                .name("ping")
                .info("Classic ping pong command 🏓")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.send("Pong 🏓", ctx.chatId()))
                .enableStats()
                .build();
    }
}
