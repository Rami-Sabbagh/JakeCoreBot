# JakeCoreBot
A Telegram bot I'm writing in Java, with multiple functionalities as requested by my friends.

## Building

You need:
- Intellj IDEA Communtiy 2020.01
- Java JDK 13

Open the repository as a project in Intellj IDEA, and build it.

## Configuring

- Create a new bot using `@BotFather` on Telegram, copy the token
and paste it into a new file at `resources/token.txt` __without a new line or any whitespace__ in it.
- Feel free to change the bot's picture, description and about text using `@BotFather`.
- Build the project and run it.
- Send a message to the bot from your account, look for the chatID, it should be your user ID.
- Edit `com.github.rami-sabbagh.SpookyBot.JakeCoreBot`, replace the creatorId in the overriden method.
- Restart the bot, and send `/updatecommands` to update the commands definition of the bot.
