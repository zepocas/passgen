package passwordcreator.application;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Command(
    name = "passgen",
    description = "calls main function"
)
public class PasswordCreatorMainCommand implements Runnable {

    private File inputFile;
    private final String relativePath = System.getProperty("user.home") + File.separator + ".passgen-wordbowl.txt";
    @Option(names = {"-s", "--string"}, description = "Input string")
    private String inputString;
    private final int MIN_TOTAL_CHARACTERS = 12;
    private final int MIN_NUMBER_CHARACTERS = 2;
    private final int MIN_SPECIAL_CHARACTERS = 1;
    private final int AMOUNT_OF_SUGGESTIONS = 30;
    private final String DEFAULT_FILE_CONTENT = "o meu nome\na minha zona\nrecordacoes de infancia\nescola primaria\nobjectos de que gosto\no que "
        + "estou a ver neste momento\nque números ou datas me dizem algo";
    private final String EASY_SPECIAL_CHARACTERS = "!\"£$%&*()_-+=|\\/?#@[]{}";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PasswordCreatorMainCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (inputString != null) {
            // Use the provided input string
            List<String> wordsArray = new ArrayList<>(List.of(inputString.split(" ")));
            System.out.println(wordsArray);
            wordsArray.removeIf(word -> word.length() < 4);
            System.out.println(wordsArray);

            if (wordsArray.size() >= 3) {
                System.out.println("Using the input string: " + inputString);
                printList(generate(List.of(inputString.split(" ")), AMOUNT_OF_SUGGESTIONS));
            } else {
                System.out.println("Invalid input string. Give more words");
            }

        } else {
            // Default behavior: read from a file
            String userHome = System.getProperty("user.home");
            inputFile = new File(relativePath);

            if (inputFile.exists()) {
                try (FileReader reader = new FileReader(inputFile)) {
                    int data;
                    while ((data = reader.read()) != -1) {
                        System.out.print((char) data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (inputFile.createNewFile()) {
                        FileWriter fileWriter = new FileWriter(inputFile);
                        fileWriter.append(DEFAULT_FILE_CONTENT);
                        fileWriter.close();
                        System.out.println("No input provided, default file was created with default content.");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            try {
                generate(processTextFromFile(), AMOUNT_OF_SUGGESTIONS);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private List<String> generate(List<String> wordBowl, int amountOfSuggestions) {
        List<String> suggestedPasswords = new ArrayList<>();

        List<String> validWords = wordBowl.stream()
                                          .filter(this::notContainsDigits)
                                          .collect(Collectors.toList());

        System.out.println("validWords = " + validWords);

        if (validWords.size() < 3) {
            throw new IllegalArgumentException("Insufficient valid words in the word bowl.");
        }

        List<String> mutableWordBowl = new ArrayList<>(wordBowl);
        List<String> mutableValidWords = new ArrayList<>(validWords);

        int i = 0;
        do {
            Collections.shuffle(mutableWordBowl);
            Collections.shuffle(mutableValidWords);

            String lowercaseWord = mutableValidWords.get(0).toLowerCase();
            String uppercaseWord = mutableValidWords.get(1).toUpperCase();
            String specialChars = generateSpecialCharacters();
            String digits = generateDigits(wordBowl);

            // Create a list of the elements and shuffle their order
            List<String> elements = new ArrayList<>();
            elements.add(lowercaseWord);
            elements.add(uppercaseWord);
            elements.add(specialChars);
            elements.add(digits);
            Collections.shuffle(elements);

            // Construct the password by combining the elements in random order
            String password = String.join("", elements);

            // Ensure the password has a minimum length of MIN_TOTAL_CHARACTERS
            if (password.length() >= MIN_TOTAL_CHARACTERS) {
                suggestedPasswords.add(password);
                i++;
            }

        } while (i < amountOfSuggestions);

        return suggestedPasswords;
    }

    private String generateDigits(List<String> wordBowl) {
        // Logic to generate two digits (either from the input or randomize between 00 and 99)
        StringBuilder digits = new StringBuilder();
        List<String> inputDigits = new ArrayList<>();

        for (String word : wordBowl) {
            inputDigits.addAll(extractDigits(word));
        }
        // Randomize digits between 00 and 99 if not enough from the input
        Random random = new Random();
        while (inputDigits.size() + digits.length() < MIN_NUMBER_CHARACTERS) {
            int randomNumber = random.nextInt(100);
            digits.append(String.format("%02d", randomNumber)); // Ensure two-di
            inputDigits.add(digits.toString());// git format
        }

        List<String> mutableInputDigits = new ArrayList<>(inputDigits);
        Collections.shuffle(mutableInputDigits);
        return mutableInputDigits.get(0);
    }

    private List<String> extractDigits(String input) {
        List<String> digits = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(input);

        while (matcher.find()) {
            digits.add(matcher.group());
        }

        return digits;
    }

    private String generateSpecialCharacters() {
        // Logic to generate special characters (one or two from EASY_SPECIAL_CHARACTERS)
        StringBuilder specialChars = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < MIN_SPECIAL_CHARACTERS; i++) {
            specialChars.append(EASY_SPECIAL_CHARACTERS.charAt(random.nextInt(EASY_SPECIAL_CHARACTERS.length() - 1)));
        }

        return specialChars.toString();
    }

    private List<String> processTextFromFile() throws FileNotFoundException {
        List<String> wordsFromFile = new ArrayList<>();
        Scanner reader = new Scanner(inputFile);

        while (reader.hasNext()) {
            String line = reader.nextLine();
            // Remove accents and convert to UTF-8
            String normalized = Normalizer.normalize(line, Normalizer.Form.NFD);
            String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            byte[] utf8Bytes = withoutAccents.getBytes(Charset.forName("UTF-8"));

            String result = new String(utf8Bytes, Charset.forName("UTF-8"));
            wordsFromFile.addAll(List.of(result.split(" ")));
        }

        return wordsFromFile;
    }

    private boolean notContainsDigits(String input) {
        return !input.matches(".*\\d.*");
    }

    private void printList(List<String> list) {
        if (list != null && !list.isEmpty()) {
            for (String string : list) {
                System.out.println(string);
            }
        }
    }
}
