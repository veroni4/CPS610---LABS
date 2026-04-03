import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapReduce {
    // read a file and return an array of strings, where each string is a line from the file
    public static String[] split(String fileName) {
        try {
            // Reads all lines into a List<String>
            List<String> lines = Files.readAllLines(Paths.get(fileName));

            // Converts the List to a String array
            return lines.toArray(new String[0]);
        } catch (IOException e) {
            // Log the error or handle it based on your needs
            System.err.println("Error reading file: " + e.getMessage());
            return new String[0]; // Return an empty array if something goes wrong
        }
    }
    // I. MAPPING PHASE: -------------------------------------------
    // key: movie name, value: list of strings that contain the movie name in lower case
    public static Map<String, List<String>> map(String[] lines, String[] movieNames) {
        Map<String, List<String>> result = new HashMap<>();

        for (String line : lines) {
            String lowerLine = line.toLowerCase();
            for (String movieName : movieNames) {
                if (lowerLine.contains(movieName.toLowerCase())) {
                    result.computeIfAbsent(movieName, k -> new ArrayList<>()).add(line);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // list of input files
        String [] inputFiles = {
                "file1.txt",
                "file2.txt",
                "file3.txt",
                "file4.txt"};

        String [] movieNames = {
                "Oppenheimer",
                "Killers of the Flower Moon",
                "Poor Things",
                "The Zone of Interest",
                "Anatomy of a Fall",
                "Past Lives",
                "The Holdovers",
                "Barbie",
                "American Fiction",
                "Maestro"
        };
        // cast to small letters for case-insensitive comparison
        for (int i = 0; i < movieNames.length; i++) {
            movieNames[i] = movieNames[i].toLowerCase();
        }

        // process each file (key: movie name, value: list of lines that contain the movie name)
        Map<String, List<String>> hashTable1 = map(split(inputFiles[0]), movieNames);
        Map<String, List<String>> hashTable2 = map(split(inputFiles[1]), movieNames);
        Map<String, List<String>> hashTable3 = map(split(inputFiles[2]), movieNames);
        Map<String, List<String>> hashTable4 = map(split(inputFiles[3]), movieNames);

        //Prints the hash tables before shuffle and reduce
        System.out.println("Before shuffle and reduce:\n Mapping results for each file:");
        
        Map<String, List<String>>[] hashTables = new Map[]{hashTable1, hashTable2, hashTable3, hashTable4};
        
        for (int fileNum = 1; fileNum <= 4; fileNum++) {
            System.out.println("File " + fileNum + ":");
            Map<String, List<String>> currentHashTable = hashTables[fileNum - 1];
            for (String movieName : movieNames) {
                System.out.println("  " + movieName + ": " + currentHashTable.getOrDefault(movieName, new ArrayList<>()).size());
            }
        }

        // II. SHUFFLE PHASE: -------------------------------------------
        // shuffle by combining all of the 4 hash tables into one hash table using hash table 1
        // as the base and adding the values of the other hash tables to it
        for (String movieName : movieNames) {
            hashTable1.computeIfAbsent(movieName, k -> new ArrayList<>()).addAll(hashTable2.getOrDefault(movieName, new ArrayList<>()));
            hashTable1.computeIfAbsent(movieName, k -> new ArrayList<>()).addAll(hashTable3.getOrDefault(movieName, new ArrayList<>()));
            hashTable1.computeIfAbsent(movieName, k -> new ArrayList<>()).addAll(hashTable4.getOrDefault(movieName, new ArrayList<>()));
        }
        //print the combined hash table - shuffled hash table
        System.out.println("------------------------------");
        System.out.println("Combined hash table (after shuffle):");

        for (String movieName : movieNames) {
            System.out.println(movieName + ": " + hashTable1.getOrDefault(movieName, new ArrayList<>()).size());
        }

        // III. REDUCE PHASE: -------------------------------------------
        // Reduce master hash table by creating a new hash table that contains the movie name 
        // and the number of occurrences of that movie(size) in the combined hash table
        Map<String, Integer> reducedHashTable = new HashMap<>();
        for (String movieName : movieNames) {
            reducedHashTable.put(movieName, hashTable1.getOrDefault(movieName, new ArrayList<>()).size());
        }

        // print the results - after reduce
        System.out.println("------------------------------");
        System.out.println("Reduced hash table (final results):");
        for (String movieName : movieNames) {
            System.out.println(movieName + ": " + reducedHashTable.get(movieName));
        }
    }
}
