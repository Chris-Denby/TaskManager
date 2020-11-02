package Graph;

import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.MailFolder;
import com.microsoft.graph.models.extensions.Message;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IMailFolderCollectionPage;
import com.microsoft.graph.requests.extensions.IMessageCollectionPage;

import java.util.List;

/**
 * Graph
 */
public class GraphHelper {

    private static SimpleAuthProvider authProvider = null;




    public static IGraphServiceClient getGraphClient(String accessToken)
    {
        IGraphServiceClient graphClient = null;
        if (graphClient == null) {
            // Create the auth provider
            authProvider = new SimpleAuthProvider(accessToken);
            // Create default logger to only log errors
            DefaultLogger logger = new DefaultLogger();
            logger.setLoggingLevel(LoggerLevel.ERROR);

            // Build a Graph client
            graphClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .logger(logger)
                    .buildClient();
        }
        System.out.println("Graph Client created - GraphHelper");
        return graphClient;
    }

    public static User getUser(IGraphServiceClient graphClient)
    {
        // GET /me to get authenticated user
        User me = graphClient
                .me()
                .buildRequest()
                .get();
        return me;
    }

    public static List<Message> getEmail(String mailbox, IGraphServiceClient graphClient)
    {
        IMessageCollectionPage messages = graphClient
                .me()
                .mailFolders(mailbox)
                .messages()
                .buildRequest()
                .get();

        return messages.getCurrentPage();
    }

    public static void getEmailFolders(IGraphServiceClient graphClient)
    {
        IMailFolderCollectionPage folders = graphClient
                .me()
                .mailFolders()
                .buildRequest()
                .get();

                List<MailFolder> foldersList = folders.getCurrentPage();
                for(MailFolder f:foldersList)
                {
                    System.out.println(f.displayName);
                }

    }
}