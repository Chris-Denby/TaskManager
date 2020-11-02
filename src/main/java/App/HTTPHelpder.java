package App;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HTTPHelpder
{
    static HttpURLConnection connection = null;


    public static void sendRestMessage() throws IOException
    {
        //https://docs.microsoft.com/en-us/sharepoint/dev/sp-add-ins/working-with-folders-and-files-with-rest?redirectedfrom=MSDN#working-with-folders-by-using-rest
        /**
         * POST https://{site_url}/_api/web/folders
         * Authorization: "Bearer " + accessToken
         * Accept: "application/json;odata=verbose"
         * Content-Type: "application/json"
         * Content-Length: {length of request body as integer}
         * X-RequestDigest: "{form_digest_value}"
         *
         * {
         *   "__metadata": {
         *     "type": "SP.Folder"
         *   },
         *   "ServerRelativeUrl": "/document library relative url/folder name"
         * }
         */

        URL serverURL = new URL("http//" + "add('LibraryName/FolderName')");
        int requestLength = 0;

        connection = (HttpURLConnection) serverURL.openConnection();
        connection.setRequestMethod("POST");

        //request headers
        connection.setRequestProperty("Authorization", "Bearer");
        connection.setRequestProperty("Accept","application/json;odata=verbose");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", requestLength + "");

        //request timeouts (5 sec)
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        //disable redirects
        connection.setInstanceFollowRedirects(false);

        //request parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put("parameter1","val");
        parameters.put("parameter2","val");
        parameters.put("parameter3","val");

        //request JSON
        String jsonInputString = "{name: Upendra, job: Programmer}";

        //output stream for request
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(ParameterStringBuilder.getParameterString(parameters));
        outputStream.flush();
        outputStream.close();

        //read response
        int status = connection.getResponseCode();

        if(status >299)
        {
            System.out.println("HTTP Request error 299");
        }
        else
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String input;
            StringBuffer content = new StringBuffer();
            while((input = in.readLine()) != null)
            {
                content.append(input);
            }
        }

        //close connection
        connection.disconnect();
    }


    public static class ParameterStringBuilder
    {
        public static String getParameterString(Map<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                result.append("&");
            }

            String resultString = result.toString();
            return resultString.length() > 0
                    ? resultString.substring(0, resultString.length() - 1)
                    : resultString;
        }
    }


}
