package com.company;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author _KaTarin_
 * 3/30/2023
 */

public class TextService {

    private ThreadPoolExecutor poolExecutor = null;
    private int numberOfAvailableProcessors;
    private int separatorLineLength;


    public TextService() {
        this.numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
        this.separatorLineLength = System.lineSeparator().length();
    }


    public Map<String, Long> splitTaskAndCountWords(Path pathToFile) {
        String line = "";

        long totalCharInFile = 0;    //total number of chars in file

        long s = 0;
        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {
            s = System.currentTimeMillis();
            while ((line = sourceReader.readLine()) != null) {
                totalCharInFile += line.length() + this.separatorLineLength;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Read with time: " + (System.currentTimeMillis() - s));

        return apportionTextToThreads(pathToFile, totalCharInFile, 2);
    }

    private Map<String, Long> apportionTextToThreads(Path pathToFile, long totalCharInFile, int scaleOfLoad) {

        Map<String, Long> resultMap = new ConcurrentHashMap<>();

        long chunkSize = 0;             //chunks for process in multiple threads
        long skipCharCount = 0;         //first line for processing in certain thread
        long endChar = 0;               //last line fir processing in certain thread
        long numOfSkipped = 0;          //actual number of skipped chars

        BlockingQueue<Runnable> processThreadQueue = new ArrayBlockingQueue<>(numberOfAvailableProcessors * scaleOfLoad);
        this.poolExecutor = new ThreadPoolExecutor(numberOfAvailableProcessors, numberOfAvailableProcessors,
                0, TimeUnit.MICROSECONDS, processThreadQueue);

        numberOfAvailableProcessors *= scaleOfLoad;

        endChar = chunkSize = totalCharInFile / numberOfAvailableProcessors;

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {
            numOfSkipped = sourceReader.skip(endChar);
            endChar = this.skipLetter(sourceReader, numOfSkipped);

            for (int i = 0; i < this.numberOfAvailableProcessors - 1; ++i) {
                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, skipCharCount, endChar));

                if (sourceReader.skip(chunkSize) < chunkSize) break;

                skipCharCount = endChar;
                endChar = this.skipLetter(sourceReader, skipCharCount + chunkSize);
            }
            if (skipCharCount < totalCharInFile) {
                skipCharCount = endChar;
                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, skipCharCount, totalCharInFile));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.poolExecutor.prestartAllCoreThreads();
        this.poolExecutor.shutdown();
        try {
            this.poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private long skipLetter(BufferedReader sourceReader, long fromChar) throws IOException {
        int letter = 0;
        if ((letter = sourceReader.read()) != -1) {
            while ((letter) != 10 && letter != -1) {
                fromChar += 1;
                letter = sourceReader.read();
            }
            if (this.separatorLineLength == 2) fromChar += 1;
        }
        return fromChar;
    }

    public void countWords(Path path, Map<String, Long> resultMap, long startWord, long endWord) {
        long s = System.currentTimeMillis();
        try (BufferedReader sourceReader = new BufferedReader(new FileReader(path.toFile()))) {
            sourceReader.skip(startWord);
            int charCount = 0;

            StringBuilder word = new StringBuilder();
            int charLetter = 0;

            while ((charLetter = sourceReader.read()) != -1 && charCount < endWord - startWord) {       // FIXME: 4/25/2023 remake regex to recognize "*.*" for example java.Math
                if (charLetter == '\n' || charLetter == '\r' || charLetter == ' ' || charLetter == ',' || charLetter == '.' ||
                        charLetter == '!' || charLetter == '?' || charLetter == '\"' || charLetter == ':'|| charLetter == '-'||
                        charLetter == '_' || charLetter == ';' || charLetter == '&' || charLetter == '*' || charLetter == '(' ||
                        charLetter == ')' || charLetter == '{' || charLetter == '}' || charLetter == '“'|| charLetter == '”'||
                        charLetter == '‘' || charLetter == '/'|| charLetter == '\\') {
                    if (word.length() != 0) {
                        resultMap.merge(word.toString(), 1L, Long::sum);
                        word.setLength(0);
                    }
                } else {
                    word.append((char) charLetter);
                }
                ++charCount;
            }
            if (word.length() != 0) resultMap.merge(word.toString(), 1L, Long::sum);

            System.out.println(Thread.currentThread() + " read time: " + (System.currentTimeMillis() - s));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
