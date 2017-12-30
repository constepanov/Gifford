package gifford;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

class Gifford {

    private byte[] key = new byte[8];

    private ArrayList<Byte> sequence = new ArrayList<>();

    Gifford(byte[] k) {
        System.arraycopy(k, 0, key, 0, key.length);
    }

    private byte[] encrypt(byte[] data) {
        int index = 0;
        byte temp;
        for(int i = 0; i < data.length; i++) {
            temp = (byte) (key[(index) & 7] ^ key[(index + 1) & 7] >> 1 ^ ((key[(index + 7) & 7]) << 1) & 255);
            sequence.add(temp);
            index--;
            index &= 7;
            key[index] = temp;
            short a = (short) ((key[(index) & 7] << 8) | key[(index + 2) & 7]);
            short b = (short) ((key[(index + 4) & 7] << 8) | key[(index + 7) & 7]);
            temp = (byte) ((a * b) >> 8);
            data[i] ^= temp;
        }
        return data;
    }

    void processFile(File input, File output) {
        try(FileInputStream inputStream = new FileInputStream(input);
            FileOutputStream outputStream = new FileOutputStream(output)) {
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            byte[] outData = encrypt(data);
            outputStream.write(outData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ArrayList<Byte> getSequence() {
        return sequence;
    }

    private String byteToString(byte b) {
        return Integer.toBinaryString(b & 255 | 256).substring(1);
    }

    int getCountOnes(ArrayList<Byte> data) {
        int result = 0;
        String byteString;
        for (byte b : data) {
            byteString = byteToString(b);
            for (int i = 0; i < byteString.length(); i++) {
                if (Integer.parseInt(String.valueOf(byteString.charAt(i))) == 1){
                    result++;
                }
            }
        }
        return result;
    }

    int getCountZeros(ArrayList<Byte> data) {
        return (data.size() * 8) - getCountOnes(data);
    }

    private double frequencyTest(int countOnes, int N) {
        int totalSize = N * 8;
        return (Math.pow((totalSize - countOnes) - countOnes, 2)) / totalSize;
    }

    void frequencyTest(ArrayList<Byte> data) {
        int length = data.size();
        int countOnes = getCountOnes(data);
        double result = frequencyTest(countOnes, length);
        if(result >= -2.7055 && result <= 2.7055)
            System.out.println("Frequency test passed: " + result);
        else
            System.out.println("Frequency test not passed: " + result);
    }

    void sequenceTest(ArrayList<Byte> data) {
        int[] occurrences = new int[256];
        int L = 8;
        long N = data.size() * 8;
        double result = 0;
        double k = N / L;
        for(byte b : data) {
            occurrences[b & 0xFF]++;
        }
        for(int i : occurrences) {
            result += Math.pow(i, 2);
        }
        result *= (256 / k);
        result -= k;
        if(result <= 284.3359)
            System.out.println("Sequence test passed: " + result);
        else
            System.out.println("Sequence test not passed: " + result);
    }

    void seriesTest(ArrayList<Byte> data) {
        int k = 8;
        long n = data.size() * 8;
        int[] B = new int[k + 1];
        int[] G = new int[k + 1];
        int countOnes = 0;
        int countZeroes = 0;
        for(byte b : data) {
            String tmp = byteToString(b);
            for(int i = 0; i < tmp.length(); i++) {
                if (Integer.parseInt(String.valueOf(tmp.charAt(i))) == 1) {
                    countOnes++;
                    if(countZeroes <= k)
                        B[countZeroes]++;
                    countZeroes = 0;
                } else {
                    countZeroes++;
                    if(countOnes <= k)
                        G[countOnes]++;
                    countOnes = 0;
                }
            }
        }

        double ones = 0;
        double zeroes = 0;
        for(int i = 1; i < k + 1; i++) {
            double e = (n - i + 3) / (Math.pow(2, i + 2));
            ones += Math.pow(B[i] - e, 2) / e;
            zeroes += Math.pow(G[i] - e, 2) / e;
        }

        double result = ones + zeroes;
        if(result <= 21.0641)
            System.out.println("Series test passed: " + result);
        else
            System.out.println("Series test not passed: " + result);
    }

    void autoCorrelationTest(ArrayList<Byte> data) {
        System.out.println("Autocorrelation test:");
        for(int tau = 10; tau < 30; tau += 5) {
            int countOnes = 0;
            for(int i = 0; i < data.size() - tau; i++) {
                String tmp = byteToString(data.get(i));
                for(int k = 0; k < tmp.length(); k++) {
                    if (Integer.parseInt(String.valueOf(tmp.charAt(k))) == 1) {
                        countOnes++;
                    }
                }
            }
            double result = frequencyTest(countOnes, data.size() - tau);
            if (result >= -3 && result <= 3)
                System.out.println("Frequency test passed:" + result + "; Tau: " + tau);
            else
                System.out.println("Frequency test not passed:" + result + "; Tau: " + tau);
        }
    }

    void universalTest(ArrayList<Byte> data) {
        ArrayList<Integer> parts = new ArrayList<>();
        int L = 8;
        int V = (int) Math.pow(2, L);
        int Q = 2000;
        double K = data.size() - Q;
        int[] table = new int[V];
        int bit;
        int tmp = 0;
        int counter = 0;
        for (Byte aData : data) {
            String byteString = byteToString(aData);
            for (int k = 0; k < byteString.length(); k++) {
                bit = Integer.parseInt(String.valueOf(byteString.charAt(k)));
                tmp += bit * Math.pow(2, counter);
                counter++;
                if (counter == L) {
                    parts.add(tmp);
                    counter = 0;
                    tmp = 0;
                }
            }
        }

        int i;
        for(i = 0; i < Q; i++) {
            int b = parts.get(i);
            table[b] = i;
        }

        double sum = 0;

        for(int k = i; k < Q + K; k++) {
            int b = parts.get(k);
            sum += (Math.log10(k - table[b]) / Math.log10(2));
            table[b] = k;
        }

        sum = sum / K;

        double e = 7.1836656;
        double d = 3.238;
        double C = 0.7 - 0.8 / L + ((4 + 32 / L) * Math.pow(K, -(double) 3 / L)) / 15;

        double result = (sum - e) / (C * Math.sqrt(d));
        if(result >= -1.2816 && result <= 1.2816)
            System.out.println("Universal test passed: " + result);
        else
            System.out.println("Universal test not passed: " + result);
    }
}
