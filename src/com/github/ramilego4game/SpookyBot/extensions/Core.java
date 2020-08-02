package com.github.ramilego4game.SpookyBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.Map;

/**
 * This bot extension is meant to provide core bot commands.
 */
public class Core extends Extension {

    public Core(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
    }

    /*@SuppressWarnings("unused")
    public Ability commandStart() {
        return Ability.builder()
                .name("start")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> silent.send("ًWelcome to Rami's Telegram Bot!", ctx.chatId()))
                .enableStats()
                .build();
    }*/

    @SuppressWarnings("unused")
    public Ability commandCancel() {
        Map<String, Integer> interactive = db.getMap("INTERACTIVE");

        return Ability.builder()
                .name("cancel")
                .info("Cancel the current operation ⏹")
                .locality(Locality.ALL)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    String chatID = ctx.chatId().toString();
                    if (interactive.containsKey(chatID)) {
                        interactive.remove(chatID);
                        silent.send("Cancelled last operation successfully ✅", ctx.chatId());
                    } else {
                        silent.send("No active command to cancel ⚠", ctx.chatId());
                    }
                })
                .enableStats()
                .build();
    }
}
