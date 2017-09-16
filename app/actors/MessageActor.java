package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.FeedResponse;
import data.Message;
import services.AgentService;
import services.FeedService;

import java.util.Objects;
import java.util.UUID;

public class MessageActor extends UntypedActor {
    public MessageActor(ActorRef out) {
        this.out = out;
    }

    public static Props props(ActorRef out) {
        return Props.create(MessageActor.class, out);
    }

    private final ActorRef out;
    private FeedService feedService = new FeedService();
    private AgentService agentService = new AgentService();

    @Override
    public void onReceive(Object message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Message messageObject = new Message();
        if (message instanceof String) {
            messageObject.text = (String) message;
            messageObject.sender = Message.Sender.USER;
            out.tell(objectMapper.writeValueAsString(messageObject),
                    self());
            String keyword = agentService
                    .getAgentResponse((String) message).keyword;
            if(!Objects.equals(keyword, "NOT_FOUND")){
                FeedResponse feedResponse = feedService.getFeedResponse(keyword);
                messageObject.text = (feedResponse.title == null) ? "No results found" : "Showing results for: " + keyword;
                messageObject.feedResponse = feedResponse;
                messageObject.sender = Message.Sender.BOT;
                out.tell(objectMapper.writeValueAsString(messageObject), self());
            }
        }
    }
}
