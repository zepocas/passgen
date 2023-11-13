package passwordcreator.application;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;
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
    @CommandLine.Parameters(index = "0", description = "Input string")
    private String inputString;
    @CommandLine.Parameters(index = "1", description = "desired amount of special characters", arity = "0..1")
    private int amountSpecialChars;
    private static final int MIN_TOTAL_CHARACTERS = 12;
    private static final int MIN_NUMBER_CHARACTERS = 2;
    private static final int MIN_SPECIAL_CHARACTERS = 1;
    private static final int AMOUNT_OF_SUGGESTIONS = 30;
    private static final String EASY_SPECIAL_CHARACTERS = "!\"$%&*()_-+=|\\/?#@[]{}";

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PasswordCreatorMainCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (inputString != null) {
            // Use the provided input string
            List<String> wordsArray = new ArrayList<>(List.of(inputString.split(" ")));
            wordsArray.removeIf(word -> word.length() < 4);

            if (wordsArray.size() >= 3) {
                //System.out.println("Using the input string: " + inputString);
                printList(generate(List.of(inputString.split(" ")), AMOUNT_OF_SUGGESTIONS));
            } else {
                System.out.println("Invalid input string. Give more words");
            }

        } else {
            System.out.println("Invalid input string. Give more words");
        }

    }

    private List<String> generate(List<String> wordBowl, int amountOfSuggestions) {
        List<String> suggestedPasswords = new ArrayList<>();
        List<String> existingSpecialCharsList = wordBowl.stream()
                                                .filter(s -> s.length() == 1 && EASY_SPECIAL_CHARACTERS.contains(s))
                                                .collect(Collectors.toList());

        List<String> validWords = wordBowl.stream()
                                          .filter(this::notContainsDigits)
                                          .collect(Collectors.toList());

        //System.out.println("validWords = " + validWords);

        if (validWords.size() < 3) {
            throw new IllegalArgumentException("Insufficient valid words in the word bowl.");
        }

        List<String> mutableWordBowl = new ArrayList<>(wordBowl);
        List<String> mutableValidWords = new ArrayList<>(validWords);

        int i = 0;
        do {
            List<String> specialCharsList;

            Collections.shuffle(mutableWordBowl);
            Collections.shuffle(mutableValidWords);
            String lowercaseWord = mutableValidWords.get(0).toLowerCase();
            String uppercaseWord = mutableValidWords.get(1).toUpperCase();

            if (existingSpecialCharsList.isEmpty()) {
                specialCharsList = generateSpecialCharacters();
            } else {
                specialCharsList = new ArrayList<>(existingSpecialCharsList);
            }

            String digits = generateDigits(wordBowl);

            // Create a list of the elements and shuffle their order
            List<String> elements = new ArrayList<>();
            elements.add(lowercaseWord);
            elements.add(uppercaseWord);
            elements.addAll(specialCharsList);
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

    private List<String> generateSpecialCharacters() {
        List<String> charList = new ArrayList<>();
        int targetAmount;

        if (amountSpecialChars > MIN_SPECIAL_CHARACTERS) {
            targetAmount = amountSpecialChars;
        } else {
            targetAmount = MIN_SPECIAL_CHARACTERS;
        }

        // Logic to generate special characters (one or two from EASY_SPECIAL_CHARACTERS)
        Random random = new Random();
        for (int i = 0; i < targetAmount; i++) {
            charList.add(String.valueOf(EASY_SPECIAL_CHARACTERS.charAt(random.nextInt(EASY_SPECIAL_CHARACTERS.length() - 1))));
        }

        return charList;
    }

    private boolean notContainsDigits(String input) {
        return !input.matches(".*\\d.*");
    }

    private void printList(List<String> list) {
        if (list != null && !list.isEmpty()) {
            list.sort(Comparator.comparingInt(String::length).reversed());
            list.forEach(string -> System.out.println(string + " - length: " + string.length()));
        }
    }
}
