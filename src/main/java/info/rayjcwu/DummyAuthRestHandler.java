package info.rayjcwu;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.*;

import java.io.IOException;
import java.util.Map;

// Reference
// http://david.pilato.fr/blog/2016/10/19/adding-a-new-rest-endpoint-to-elasticsearch-updated-for-ga/
public class DummyAuthRestHandler extends BaseRestHandler {

    @Inject
    public DummyAuthRestHandler(Settings settings, RestController controller) {
        super(settings);
        // didn't register handler, only filter
        controller.registerFilter(new RestFilter() {
            @Override
            public void process(RestRequest restRequest, RestChannel channel, NodeClient nodeClient, RestFilterChain restFilterChain) throws Exception {
                boolean foundKey = false;
                for (Map.Entry<String, String> e: restRequest.headers()) {
                    if (e.getKey().equalsIgnoreCase("Dummy-Auth") && e.getValue().equalsIgnoreCase("somePassword")) {
                        foundKey = true;
                        break;
                    }
                }
                if (foundKey) {
                    restFilterChain.continueProcessing(restRequest, channel, nodeClient);
                } else {
                    final ElasticsearchException exception = new ElasticsearchException("Did not find authentication");
                    channel.sendResponse(new BytesRestResponse(channel, RestStatus.FORBIDDEN, exception));
                }
            }
        });
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest restRequest, NodeClient client) throws IOException {
        // Actually this method should not be invoked, since there is no corresponding PATH for this handler
        // Return {} in case.
        return channel -> {
            XContentBuilder builder = channel.newBuilder();
            builder.startObject();
            builder.endObject();
            channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
        };
    }
}
