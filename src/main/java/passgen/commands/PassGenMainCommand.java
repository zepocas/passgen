package passgen.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import passgen.tools.Generator;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@CommandLine.Command(name = "passgen", mixinStandardHelpOptions = true, version = "1.0")
public class PassGenMainCommand implements CommandLineRunner {

    private static final int AMOUNT_OF_SUGGESTIONS = 30;
    @CommandLine.Parameters(index = "0", description = "Input string")
    private String inputString;
    @CommandLine.Parameters(index = "1", description = "desired amount of special characters", arity = "0..1")
    private Integer amountSpecialChars;
    private Generator generator;

    // TODO: 13/11/2023 implementar get random word a uma API publica: GET https://api.dicionario-aberto.net/random
    // TODO: 13/11/2023 implementar get palavra próxima de outra a uma API publica: GET https://api.dicionario-aberto.net/near/{word}

    @Override
    public void run(String... args) throws Exception {
        if (inputString != null) {
            // Use the provided input string
            List<String> wordsArray = new ArrayList<>(List.of(inputString.split(" ")));
            wordsArray.removeIf(word -> word.length() < 4);

            if (wordsArray.size() >= 3) {
                //System.out.println("Using the input string: " + inputString);
                printList(generator.generate(List.of(inputString.split(" ")), AMOUNT_OF_SUGGESTIONS, amountSpecialChars));
            } else {
                System.out.println("Invalid input string. Give more words");
            }

        } else {
            System.out.println("Invalid input string. Give more words");
        }
    }

    private void printList(List<String> list) {
        if (list != null && !list.isEmpty()) {
            list.sort(Comparator.comparingInt(String::length).reversed());
            list.forEach(string -> System.out.println(string + " - length: " + string.length()));
        }
    }
}
