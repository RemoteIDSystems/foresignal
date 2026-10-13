package ai.foresignal.testsupport.chatops;

import ai.foresignal.chatops.ChannelConfig;
import ai.foresignal.chatops.ChatConnector;
import ai.foresignal.chatops.ChatRef;
import ai.foresignal.chatops.ChatReply;
import ai.foresignal.chatops.CommandSink;
import ai.foresignal.chatops.ConnectorState;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ChatConnector} that records what the router sends (prompt 061 tests): replies and callback answers,
 * with or without button support.
 */
public final class RecordingConnector implements ChatConnector {

    /** One recorded reply. */
    public record Sent(ChatRef chat, ChatReply reply) {}

    /** One recorded callback answer. */
    public record Answer(String callbackRef, String text) {}

    private final boolean buttons;
    private final ConnectorState state = new ConnectorState();
    private final List<Sent> replies = new ArrayList<>();
    private final List<Answer> answers = new ArrayList<>();

    /** A connector with ({@code true}, like Telegram) or without buttons (like Matrix). */
    public RecordingConnector(boolean buttons) {
        this.buttons = buttons;
    }

    @Override
    public void start(ChannelConfig config, CommandSink sink) {}

    @Override
    public int poll() {
        return 0;
    }

    @Override
    public void stop() {}

    @Override
    public synchronized void reply(ChatRef chat, ChatReply reply) {
        replies.add(new Sent(chat, reply));
    }

    @Override
    public synchronized void answerCallback(String callbackRef, String text) {
        answers.add(new Answer(callbackRef, text));
    }

    @Override
    public boolean supportsButtons() {
        return buttons;
    }

    @Override
    public ConnectorState state() {
        return state;
    }

    /** The replies so far. */
    public synchronized List<Sent> replies() {
        return List.copyOf(replies);
    }

    /** The callback answers so far. */
    public synchronized List<Answer> answers() {
        return List.copyOf(answers);
    }

    /** The text of the last reply. */
    public synchronized String lastText() {
        return replies.isEmpty() ? null : replies.getLast().reply().markdown();
    }
}
