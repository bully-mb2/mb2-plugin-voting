package com.templars_server.voting;

import com.templars_server.model.Context;
import com.templars_server.util.rcon.RconClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Vote implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Vote.class);
    public static final int MAX_CHOICES = 6;
    public static final String DONT_CHANGE = "Don't change";
    private static final int[] VOTE_DURATION = new int[]{60, 30, 30};
    private static final int[] REVOTE_DURATION = new int[]{30, 30, 30};
    private static final int REVOTE_THRESHOLD = 0;


    private final String prefix;
    private final List<String> choices;
    private final Map<Integer, Integer> votes;
    private final Context context;
    private final RconClient rcon;
    private final VoteCallback callback;
    private final Thread thread;
    private volatile boolean canceled;
    private volatile boolean skipPhase;

    public Vote(String prefix, List<String> choices, Context context, VoteCallback callback) {
        this.prefix = prefix;
        this.choices = choices;
        this.votes = new ConcurrentHashMap<>();
        this.context = context;
        this.rcon = context.getRconClient();
        this.callback = callback;
        this.thread = new Thread(this, "Vote-Thread");
        this.canceled = false;
        this.skipPhase = false;
    }

    public void start() {
        thread.start();
        LOG.info(getName() + " started");
    }

    public boolean isAlive() {
        return thread.isAlive() && !thread.isInterrupted();
    }

    public synchronized void cancel() {
        canceled = true;
        thread.interrupt();
        LOG.info(getName() + " cancelled");
    }

    @Override
    public void run() {
        int attempt = 1;
        do {
            LOG.info(getName() + " attempt " + attempt);
            runVote(attempt++);
        } while(!isVoteFinished());
    }

    public synchronized void vote(int slot, Integer vote) {
        if (vote == null) {
            votes.remove(slot);
            return;
        }

        if (vote < 0 || vote >= choices.size()) {
            rcon.print(slot, prefix + "Invalid choice");
            return;
        }

        votes.put(slot, vote);
        String choice = choices.get(vote);
        if (choice == null) {
            rcon.print(slot, prefix + "Invalid choice");
        } else {
            rcon.print(slot, prefix + "Vote cast for " + choice + ", now " + makeVoteString(tallyVotes(vote)));
            if (votes.size() >= context.getPlayers().size()) {
                LOG.info(votes.size() + " / " + context.getPlayers().size() + " voted, interrupting vote thread and finishing the vote");
                rcon.printAll(prefix + "Everyone has voted!");
                skipPhase = true;
                thread.interrupt();
            }
        }
    }

    private void runVote(int attempt) {
        int[] duration = VOTE_DURATION;
        if (attempt > 1) {
            duration = REVOTE_DURATION;
        }

        List<String> choices = this.choices.stream()
                .limit(Vote.MAX_CHOICES)
                .distinct()
                .collect(Collectors.toList());
        if (attempt > 1) {
            rcon.printAll(prefix + "Voting round ^3" + attempt + "^7 begun. Type !number to vote. Voting will complete in " + makeTimeString(timeLeft(0, duration)));
        } else {
            rcon.printAll(prefix + "Voting has begun. Type !number to vote. Voting will complete in " + makeTimeString(timeLeft(0, duration)));
        }

        rcon.printAll(prefix + makeChoicesString(choices, null));
        for (int step=0; step<duration.length; step++){
            try {
                Thread.sleep(duration[step] * 1000L);
            } catch (InterruptedException e) {
                if (skipPhase) {
                    //Ignore result, reset the interrupted flag if we want to skip this voting phase
                    //noinspection ResultOfMethodCallIgnored
                    Thread.interrupted();
                    skipPhase = false;
                }

                return;
            }

            if (step+1 < duration.length) {
                rcon.printAll(prefix + "Type !number to vote. Voting will complete in " + makeTimeString(timeLeft(step+1, duration)));
                rcon.printAll(prefix + makeChoicesString(choices, votes));
            }
        }
    }

    private int timeLeft(int step, int[] duration) {
       return IntStream.of(duration)
               .skip(step)
               .sum();
    }

    private boolean isVoteFinished() {
        if (canceled || !isAlive()) {
            return true;
        }

        Map<String, Integer> resultMap = new LinkedHashMap<>();
        for (Integer vote : votes.values()) {
            resultMap.computeIfAbsent(choices.get(vote), k -> tallyVotes(vote));
        }

        List<Map.Entry<String, Integer>> result = resultMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        if (result.size() < 1) {
            completeVote("", 0);
            return true;
        }

        Map.Entry<String, Integer> first = result.get(0);
        if (result.size() <= 2) {
            completeVote(first.getKey(), first.getValue());
            return true;
        }

        Map.Entry<String, Integer> second = result.get(1);
        Map.Entry<String, Integer> third = result.get(2);
        if (
                first.getValue() > 1
                        && second.getValue() > 0
                        && third.getValue() > REVOTE_THRESHOLD
        ) {
            votes.clear();
            choices.clear();
            choices.add(first.getKey());
            choices.add(second.getKey());
            LOG.info("Starting second round of voting between " + first.getKey() + " and " + second.getKey());
            rcon.printAll(String.format("%sA second round of voting between %s and %s has begun", prefix, first.getKey(), second.getKey()));
            return false;
        }

        completeVote(first.getKey(), first.getValue());
        return true;
    }

    private void completeVote(String winner, int votes) {
        LOG.info(getName() + " completing, " + winner + " won with " + votes + " votes");
        if (canceled) {
            LOG.info("Vote was cancelled");
            return;
        }

        if (votes < 1) {
            rcon.printAll(prefix + "No votes have been cast");
        } else {
            rcon.printAll(prefix + makeChoicesString(choices, this.votes));
            rcon.printAll(prefix + winner + " wins with " + makeVoteString(votes));
        }

        callback.onVoteComplete(context, winner);
    }

    private int tallyVotes(int choice) {
        return (int) votes.values().stream()
                .filter((vote) -> vote == choice)
                .count();
    }

    private String makeVoteString(int votes) {
        if (votes > 1) {
            return "^3" + votes + "^7 votes";
        }

        return "^3" + votes + "^7 vote";
    }

    private String makeChoicesString(List<String> choices, Map<Integer, Integer> votes) {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<choices.size(); i++) {
            String choice = choices.get(i);
            builder.append(i+1);
            if (votes != null) {
                builder.append("(");
                builder.append(tallyVotes(i));
                builder.append(")");
            }
            builder.append(": ");
            builder.append(choice);
            if (i < choices.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    private String makeTimeString(int durationSeconds) {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        StringBuilder builder = new StringBuilder();
        if (minutes > 0) {
            builder.append("^2");
            builder.append(minutes);
            builder.append(" ^7minute");
            if (minutes > 1) {
                builder.append("s");
            }
            if (seconds > 0) {
                builder.append(" ");
            }
        }

        if (seconds > 0) {
            builder.append("^2");
            builder.append(seconds);
            builder.append(" ^7second");
            if (seconds > 1) {
                builder.append("s");
            }
        }
        return builder.toString();
    }

    private String getName() {
        return thread.getName() + "-" + thread.getId();
    }

}
