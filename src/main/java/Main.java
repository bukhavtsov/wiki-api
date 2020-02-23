import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest titlesRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://en.wikipedia.org/w/api.php?action=query&generator=search&format=json&exintro&exsentences=1&exlimit=max&gsrlimit=100&gsrsearch=hastemplate:Birth_date_and_age+Melanie_laurent&pithumbsize=100&pilimit=max&prop=pageimages%7Cextracts"))
                .build();


        HttpResponse<String> titlesResponse =
                client.send(titlesRequest, HttpResponse.BodyHandlers.ofString());


        List<String> titles = getTitles(titlesResponse);
        List<String> extracts = getExtracts(titles, client);
        for (int i = 0; i < extracts.size(); i++) {
            System.out.println(titles.get(i));
            System.out.println(extracts.get(i));
        }
    }

    public static String[] getPages(HttpResponse<String> response) {
        JSONObject json = new JSONObject(response.body());
        Object[] pages = json.getJSONObject("query").getJSONObject("pages").keySet().toArray();
        return Arrays.copyOf(pages, pages.length, String[].class);
    }

    public static String getTitleByPage(String pageID, HttpResponse<String> response) {
        JSONObject json = new JSONObject(response.body());
        return json.getJSONObject("query").getJSONObject("pages").getJSONObject(pageID).get("title").toString().replace(" ", "_");
    }

    public static List<String> getTitles(HttpResponse<String> response) {
        String[] pages = getPages(response);
        List<String> titles = new ArrayList<>();
        for (String page : pages) {
            titles.add(getTitleByPage(page, response));
        }
        return titles;
    }

    public static String getPersonExtractText(String famousPerson, HttpClient client) throws IOException, InterruptedException {
        HttpRequest extractedRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext=&titles=" + famousPerson))
                .build();

        HttpResponse<String> extractedResponse =
                client.send(extractedRequest, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(extractedResponse.body());
        String pageNumber = json.getJSONObject("query").getJSONObject("pages").keys().next();
        return json.getJSONObject("query").getJSONObject("pages").getJSONObject(pageNumber).get("extract").toString();
    }

    public static List<String> getExtracts(List<String> titles, HttpClient client) throws IOException, InterruptedException {
        List<String> extracts = new ArrayList<>();
        for (String title : titles) {
            extracts.add(getPersonExtractText(title, client));
        }
        return extracts;
    }
}