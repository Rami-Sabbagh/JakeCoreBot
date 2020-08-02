package com.github.ramilego4game.SpookyBot;

import com.github.ramilego4game.SpookyBot.extensions.*;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class JakeCoreBot extends AbilityBot {

    public JakeCoreBot(String token, String username, DefaultBotOptions options) {
        super(token, username, options);
    }

    @Override
    public int creatorId() {
        return 856875680;
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionCore() {
        return new Core(this, this.silent, this.db);
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionBasic() {
        return new Basic(this, this.silent, this.db);
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionFun() {
        return new Fun(this, this.silent, this.db);
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionReminder() {
        return new Reminder(this, this.silent, this.db);
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionDebug() {
        return new Debug(this, this.silent, this.db);
    }

    @SuppressWarnings("unused")
    public AbilityExtension extensionCreator() {
        return new Creator(this, this.silent, this.db);
    }


}
