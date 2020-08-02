package com.github.rami_sabbagh.SpookyBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This bot extension contains commands meant to be used by the bot's creator only.
 */
public class Creator extends Extension {

    public Creator(AbilityBot bot, SilentSender silent, DBContext db) {
        super(bot, silent, db);
    }

    @SuppressWarnings("unused")
    public Ability commandUpdateCommands() {
        return Ability.builder()
                .name("updatecommands")
                .info("Updates the commands definition of the bot ⚙")
                .locality(Locality.USER)
                .privacy(Privacy.CREATOR)
                .action(ctx -> {
                    List<BotCommand> commands = new ArrayList<>();
                    for (Map.Entry<String, Ability> entry : bot.abilities().entrySet()) {
                        Ability ability = entry.getValue();
                        if (ability.locality() == Locality.ALL && ability.privacy() == Privacy.PUBLIC && ability.info() != null) {
                            commands.add(new BotCommand().setCommand(entry.getKey()).setDescription(ability.info()));
                        }
                    }

                    try {
                        bot.execute(new SetMyCommands().setCommands(commands));
                    } catch (Exception e) {
                        silent.send("Failed to update commands definition!", ctx.chatId());
                        System.out.println("Failed to update commands definition...");
                        e.printStackTrace();
                        return;
                    }

                    silent.send("Updated commands definition successfully ✅", ctx.chatId());
                })
                .build();
    }
}
