import java.io.*;
import java.util.*;

// Author: Jakob Millen

/*
 * Title: Boyer Moore Algorithm | Good Suffix Heuristic
 * Author: Geeksforgeeks
 * Link: https://www.geeksforgeeks.org/boyer-moore-algorithm-good-suffix-heuristic/
 * Usage: Some logic and structure was used as inspiration.
 */

public class BMSearch {

    public static void main(String[] args) throws IOException {
        // Checks if the correct number of arguments are passed
        if (args.length < 2) {
            System.out.println("Usage: java BMSearch <skip table> <text file>");
            return;
        }

        String tableFile = args[0];
        String textFile = args[1];

        // Load the pattern and skip table data from the specified file
        Map<String, Object> loadedData = loadSkipTableAndPattern(tableFile);
        String pattern = (String) loadedData.get("pattern"); // Extract the pattern used in the skip table
        @SuppressWarnings("unchecked") // Suppress as safe cast after checking type
        HashMap<Character, List<Integer>> skipTable = (HashMap<Character, List<Integer>>) loadedData.get("skipTable");

        //System.out.println(loadedData);
        //System.out.println(pattern);

        int[] suffix = new int[pattern.length()]; // Array to store the suffix lengths
        int[] shift = new int[pattern.length() + 1]; // Array to store the shift amounts
        preprocessGoodSuffix(pattern, suffix, shift); // Preprocess the pattern to fill suffix and shift arrays

        // Search the text file and print lines containing the pattern
        searchAndPrintMatches(textFile, pattern, skipTable, shift);
    }

    // Preprocess the pattern to determine the good suffix shifts
    private static void preprocessGoodSuffix(String pattern, int[] suffix, int[] shift) {
        int m = pattern.length(); // Length of the pattern
        int g = m - 1; // Rightmost position of the pattern
        int f = m; // Position of the rightmost suffix

        // Initialize arrays
        for (int i = m - 1; i >= 0; i--) {
            // Adjust the suffix array based on the previously computed position
            if (i > g && suffix[i + m - 1 - f] < i - g) {
                suffix[i] = suffix[i + m - 1 - f];
            } else {
                // Reset g and f to the current position when a mismatch is found
                if (i < g) {
                    g = 1;
                }
                f = i;
                // Update suffix lengths that match the prefix of the pattern
                while (g >= 0 && pattern.charAt(g) == pattern.charAt(g + m - 1 - f)) {
                    g--;
                }
                suffix[i] = f - g;
            }
        }

        // Set the shift values for all positions in the pattern
        for (int i = 0; i <= m; i++){
            shift[i] = m;
        }
        int j = 0;
        // Adjust shift values based on suffix array calculations
        for (int i = m - 1; i >= -1; i--){
            if (i == -1 || suffix[i] == i + 1){
                for (; j < m - 1 - i; j++){
                    if (shift[j] == m){
                        shift[j] = m -1 - i;
                    }
                }
            }
        }
        // Apply the final shift settings for the suffixes that match prefixes
        for (int i = 0; i <= m - 2; i++){
            shift[m - 1 - suffix[i]] = m - 1 - i;
        }
    }

    // Search through the text file and print lines containing the pattern
    private static void searchAndPrintMatches(String textFile, String pattern, HashMap<Character, List<Integer>> skipTable, int[] shift) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(textFile));
        String line;
        while((line = reader.readLine()) != null){
            if (searchLine(line, pattern, skipTable, shift)){
                System.out.println("Pattern Match Found: " + line); // Print line the pattern was found on
            }
        }
        reader.close();
    }

    // Search a single line for the pattern and determine if it should be printed
    private static boolean searchLine(String line, String pattern, HashMap<Character, List<Integer>> skipTable, int[] shift){
        int m = pattern.length(); // Length of the pattern
        int n = line.length(); // Length of the line
        int i = 0; // Starting position in line

        while (i <= n - m){ // Check theres enough text left to match the pattern
            int j = m - 1; // Start checking from the end of the pattern

            // Compare while characters match
            while (j >= 0 && pattern.charAt(j) == line.charAt(i + j)) {
                j--; // Move to the next character in the pattern
            } 
            
            if (j < 0) {
                return true; // Pattern found in the line
            } else {
                // Mismatch found so calculate skip
                char mismatchChar = line.charAt(i + j);
                int skip = j;

                if (skipTable.containsKey(mismatchChar) && j < skipTable.get(mismatchChar).size()) {
                    // Check to see if we have a skip value for this character at this position
                    skip = skipTable.get(mismatchChar).get(j);
                } else {
                    // If no specific skip found use default logic
                    skip = m;
                }
                //System.out.println("Mismatch at i: " + i + ", j: " + j + ", char: " + mismatchChar + ", skip: " + skip);
                // Move the pattern forward by the calculated skip amount
                i += skip;
            }
        }
        return false; // Pattern not found in the line;
    }

    // Load the skip table and pattern from the file
    private static Map<String, Object> loadSkipTableAndPattern(String filename) throws IOException {
        Map<String, Object> result = new HashMap<>();
        HashMap<Character, List<Integer>> skipTable = new HashMap<>();
        String pattern = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {

            // Read the first line to extract the pattern
            String firstLine = reader.readLine();
            if (firstLine != null) {
                pattern = firstLine.replaceAll("[^a-zA-Z0-9]", ""); // Removes all non-alphanumeric characters
            }

            String line;
            while((line = reader.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split(",");
                // Parse shift values for characters
                if (parts.length > 1) { 
                    char ch = parts[0].charAt(0);
                    List<Integer> skips = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++){
                        skips.add(Integer.parseInt(parts[i]));
                    }
                    skipTable.put(ch, skips);
                }
            }
        }

        result.put("pattern", pattern);
        result.put("skipTable", skipTable);
        return result;
    }
}