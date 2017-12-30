package gifford;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        byte[] key = {-63, -40, 121, 51, -38, 89, -116, 54};
        Gifford generator = new Gifford(key);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input file: ");
        File input = new File(scanner.nextLine());
        System.out.println("Output file: ");
        File output = new File(scanner.nextLine());
        generator.processFile(input, output);
        ArrayList<Byte> list = generator.getSequence();
        System.out.println("Ones: " + generator.getCountOnes(list));
        System.out.println("Zeroes: " + generator.getCountZeros(list));
        generator.frequencyTest(list);
        generator.sequenceTest(list);
        generator.seriesTest(list);
        generator.autoCorrelationTest(list);
        generator.universalTest(list);
    }
}
