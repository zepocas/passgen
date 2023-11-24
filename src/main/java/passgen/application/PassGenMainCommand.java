package passgen.application;

import com.jayway.jsonpath.JsonPath;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Command(
    name = "passgen",
    description = "calls main function"
)
public class PassGenMainCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "Input string", arity = "0..1")
    private String inputString;
    @CommandLine.Parameters(index = "1", description = "desired amount of special characters", arity = "0..1")
    private Integer amountSpecialChars;
    @CommandLine.Option(names = {"-r", "-random"}, description = "gets random portuguese words")
    private Boolean random;
    private static final String API_URL = "https://api.dicionario-aberto.net/random";
    private static final int AMOUNT_OF_SUGGESTIONS = 30;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PassGenMainCommand()).execute(args);
        System.exit(exitCode);
    }

    // TODO: 24/11/2023 implementar get frases from notícias API publica: GET https://www.publico.pt/api/list/ultimas

    @Override
    public void run() {
        Generator generator = new Generator();
        if (inputString != null) {
            // Use the provided input string
            List<String> wordsArray = new ArrayList<>(List.of(inputString.split(" ")));
            wordsArray.removeIf(word -> word.length() < 4);

            if (wordsArray.size() >= 3) {
                printList(generator.generate(inputString, AMOUNT_OF_SUGGESTIONS, amountSpecialChars));
            } else {
                System.out.println("Invalid input string. Give more words");
            }
        } else if (random != null && random) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(URI.create(API_URL))
                                             .build();
            StringBuilder randomInputString = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String randomWord = getRandomWordFromAPI(client, request);
                if (randomWord == null || randomWord.isEmpty()) {
                    System.out.println("Can't get random word");
                }
                randomInputString.append(randomWord).append(" ");
            }
            printList(generator.generate(randomInputString.toString(), AMOUNT_OF_SUGGESTIONS, amountSpecialChars));

        } else {
            System.out.println("Invalid input string. Give me more words");
        }
    }

    private String getRandomWordFromAPI(HttpClient client, HttpRequest request) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Check if the request was successful (status code 200)
            if (response.statusCode() == 200) {
                return JsonPath.read(response.body(), "$.word");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void printList(List<String> list) {
        if (list != null && !list.isEmpty()) {
            list.sort(Comparator.comparingInt(String::length).reversed());
            list.forEach(string -> System.out.println(string + " - length: " + string.length()));
        }
    }
}
