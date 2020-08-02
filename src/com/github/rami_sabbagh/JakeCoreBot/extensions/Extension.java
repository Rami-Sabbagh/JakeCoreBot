package com.github.rami_sabbagh.JakeCoreBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;

public abstract class Extension implements AbilityExtension {
    protected final AbilityBot bot;
    protected final SilentSender silent;
    protected final DBContext db;

    public Extension(AbilityBot bot, SilentSender silent, DBContext db) {
        this.bot = bot;
        this.silent = silent;
        this.db = db;
    }
}
