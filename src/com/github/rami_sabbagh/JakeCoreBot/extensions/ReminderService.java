package com.github.rami_sabbagh.JakeCoreBot.extensions;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * <u>Not an extension</u>, but the thread responsible for dispatching timers update messages.
 */
public class ReminderService {

    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);

    protected final AbilityBot bot;
    protected final SilentSender silent;
    protected final DBContext db;

    protected final Map<Long, Timer> timers = new HashMap<>();

    public ReminderService(AbilityBot bot, SilentSender silent, DBContext db) {
        this.bot = bot;
        this.silent = silent;
        this.db = db;
    }

    /**
     * Calculates the remaining time until a timer is up. (thread-safe)
     *
     * @param instant The timer's instant.
     * @return The remaining time.
     */
    public static Duration calculateRemainingTime(Instant instant) {
        return Duration.between(Instant.now(), instant);
    }

    /**
     * Calculates the time to wait before dispatching a new timer update message. (thread-safe)
     *
     * @param remaining The time remaining for the timer.
     * @return The time remaining to dispatch the timer update message.
     */
    public static Duration calculateDispatchTime(Duration remaining) {
        if (remaining.toDays() > 0) //Days remaining
            return remaining.minusDays(remaining.toDays());
        else if (remaining.toHours() >= 6) //6 hours remaining
            return remaining.minusHours(6);
        else if (remaining.toHours() >= 3) //3 hours remaining
            return remaining.minusHours(3);
        else if (remaining.toHours() >= 1) //1 hour remaining
            return remaining.minusHours(1);
        else if (remaining.toMinutes() >= 30) //30 minutes remaining
            return remaining.minusMinutes(30);
        else if (remaining.toMinutes() >= 15) //15 minutes remaining
            return remaining.minusMinutes(15);
        else if (remaining.toMinutes() >= 5) //5 minutes remaining
            return remaining.minusMinutes(5);
        else if (remaining.toMinutes() >= 1) //1 minute remaining
            return remaining.minusMinutes(1);
        else if (remaining.toSeconds() >= 30) //30 seconds remaining
            return remaining.minusSeconds(30);
        else if (remaining.toSeconds() >= 10) //10 seconds remaining
            return remaining.minusSeconds(10);
        else if (remaining.toSeconds() >= 5) //5 seconds remaining
            return remaining.minusSeconds(5);
        else
            return remaining; //anything left
    }

    /**
     * Formats the remaining time into a message to display for the user. (thread-safe)
     * The method expects to receive the remaining time <u>at the time of</u> sending the message.
     * That has to be calculated by subtracting the <i>remaining time until timer dispatch</i> from the <i>remaining time for the timer</i>.
     *
     * @param remaining The remaining time at the point of sending the message.
     * @return The formatted message.
     */
    public static String formatDispatchMessage(Duration remaining) {
        if (remaining.toDays() > 0)
            return String.format("`%d day%s` remaining ⏳", remaining.toDays(), (remaining.toDays() > 1) ? "s" : "");
        else if (remaining.toMinutes() > 0)
            return String.format("`%d minute%s` remaining ⏳", remaining.toMinutes(), (remaining.toMinutes() > 1) ? "s" : "");
        else if (remaining.toSeconds() > 2)
            return String.format("`%d seconds` remaining ⏳", remaining.toSeconds()); //No need for conditional 's' because we're not dispatching with 1 second remaining.
        else
            return "Time's up! ⏲";
    }

    /**
     * Schedules a timer for execution.
     *
     * @param chatID  The chat which the timer belongs to.
     * @param instant The instant of the timer's end.
     * @return The current scheduled future for the timer, null if the timer is expired on arrival.
     */
    @SuppressWarnings("UnusedReturnValue")
    public ScheduledFuture<?> scheduleTimer(long chatID, Instant instant) {
        return new Timer(chatID, instant).scheduledFuture;
    }

    /**
     * Attempts to cancel a timer.
     *
     * @param chatID The chat which the timer belongs to.
     * @return Whether it has been cancelled successfully or not.
     */
    public boolean cancelTimer(long chatID) {
        Timer timer;
        synchronized (timers) {
            timer = timers.get(chatID);
        }
        return timer.cancel();
    }


    /**
     * Represents a scheduled timer.
     */
    protected class Timer implements Runnable {
        /**
         * The chat which the timer belongs to.
         */
        final long chatID;

        /**
         * The instant of the timer's end.
         */
        final Instant instant;

        /**
         * The next timer update dispatch message.
         */
        volatile String dispatchMessage;

        /**
         * The latest scheduled future of the timer.
         */
        volatile ScheduledFuture<?> scheduledFuture;

        /**
         * Creates a new timer instance and schedules it instantly. (thread-safe)
         * Automatically adds the timer to the timers list.
         *
         * @param chatID  The chat which the timer belongs to.
         * @param instant The instant of the timer's end.
         */
        protected Timer(long chatID, Instant instant) {
            this.chatID = chatID;
            this.instant = instant;
            this.schedule();

            synchronized (timers) {
                timers.put(chatID, this);
            }
        }

        /**
         * Attempts to cancel the timer and unregister it.
         *
         * @return true when cancelled successfully, false otherwise.
         */
        boolean cancel() {
            if (this.scheduledFuture == null) return true; //Already cancelled.
            if (this.scheduledFuture.cancel(false)) {
                this.dispatchMessage = null;
                this.scheduledFuture = null;
                db.getMap("TIMERS").remove(String.valueOf(chatID));
                synchronized (timers) {
                    timers.remove(this.chatID, this);
                }
                return true;
            }
            return false;
        }

        /**
         * Reschedules the timer for the next dispatch.
         *
         * @return The scheduled future for the timer, null when if the timer has expired.
         */
        @SuppressWarnings("UnusedReturnValue")
        ScheduledFuture<?> schedule() {
            Duration untilTimersEnd = calculateRemainingTime(instant);
            if (untilTimersEnd.isNegative()) {
                this.dispatchMessage = null;
                this.scheduledFuture = null;
                db.getMap("TIMERS").remove(String.valueOf(chatID)); //Remove the expired timer from the database.
                synchronized (timers) {
                    timers.remove(this.chatID, this);
                }
                return null; //Timer expired, don't re-schedule.
            }

            Duration untilNextDispatch = calculateDispatchTime(untilTimersEnd);
            Duration remainderAtDispatch = untilTimersEnd.minus(untilNextDispatch);
            this.dispatchMessage = formatDispatchMessage(remainderAtDispatch);
            this.scheduledFuture = executor.schedule(this, untilNextDispatch.toSeconds() + 1, TimeUnit.SECONDS);
            return this.scheduledFuture;
        }

        /**
         * Dispatches the timer update message, and re-schedules the timer if needed.
         */
        @Override
        public void run() {
            System.out.println("Dispatching timer!");
            silent.sendMd(dispatchMessage, chatID);
            schedule(); //Reschedule the timer.
        }
    }
}
