import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    private final String queueName;
    private final Connection queueConnection;
    private final boolean IS_DURABLE = false;

    public ChannelFactory(String queueName, Connection queueConnection) {
        super();
        this.queueName = queueName;
        this.queueConnection = queueConnection;
    }

    @Override
    public Channel create() throws Exception {
        Channel channel = queueConnection.createChannel();
        channel.queueDeclare(queueName, IS_DURABLE, false, false, null);
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        return new DefaultPooledObject<>(channel);
    }
}
