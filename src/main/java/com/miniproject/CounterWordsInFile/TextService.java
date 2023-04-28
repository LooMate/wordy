package com.miniproject.CounterWordsInFile;


import com.miniproject.CounterWordsInFile.enums.ProgramMode;

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

    private ThreadPoolExecutor poolExecutor;
    private final int numberOfAvailableProcessors;
    private final int separatorLineLength;


    public TextService() {
        this.numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
        this.separatorLineLength = System.lineSeparator().length();
    }



    public Map<String, Long> splitTaskAndCountWords(Path pathToFile, ProgramMode programMode) {

        String line = "";
        long totalCharInFile = 0;    //total number of chars in file
        long timeStart = 0;

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {
            timeStart = System.currentTimeMillis();
            while ((line = sourceReader.readLine()) != null) {
                totalCharInFile += line.length() + this.separatorLineLength;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Please check FILE path");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if(programMode == ProgramMode.EXPLAIN || programMode == ProgramMode.EXPLAIN_IN_SINGLE_THREAD)
            System.out.println("Read with time: " + (System.currentTimeMillis() - timeStart));

        return apportionTextToThreads(pathToFile, totalCharInFile, programMode);
    }


    private Map<String, Long> apportionTextToThreads(Path pathToFile, long totalCharInFile, ProgramMode programMode) {

        Map<String, Long> resultMap = new ConcurrentHashMap<>();

        long chunkSize = 0;             //chunks     to be processed in multiple threads
        long skipCharCount = 0;         //first line to be processed in a certain thread
        long endChar = 0;               //last line  to be processes in a certain thread
        long numOfSkipped = 0;          //actual number of skipped chars
        int numOfAvProc = 0;            //number of cores used in a calculation
        boolean showTime = false;       //display the time to executed process

        switch (programMode) {
            case EXPLAIN_IN_SINGLE_THREAD:
                showTime = true;
            case SINGLE_THREAD:
                numOfAvProc = 1;
                break;
            case EXPLAIN:
                showTime = true;
            case DEFAULT:
                numOfAvProc = this.numberOfAvailableProcessors;
                break;
            default:
                System.out.println("SOMETHING WENT WRONG \n TextService apportionTextToThreads");
        }

        BlockingQueue<Runnable> processThreadQueue = new ArrayBlockingQueue<>(numOfAvProc);
        this.poolExecutor = new ThreadPoolExecutor(numOfAvProc, numOfAvProc,
                0, TimeUnit.MICROSECONDS, processThreadQueue);

        endChar = chunkSize = totalCharInFile / numOfAvProc;

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {
            numOfSkipped = sourceReader.skip(endChar);
            endChar = this.skipLetter(sourceReader, numOfSkipped);

            if (numOfAvProc != 1) --numOfAvProc;         //leave alone LAST cycle
            for (int i = 0; i < numOfAvProc; ++i) {
                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, skipCharCount, endChar, showTime));

                if (sourceReader.skip(chunkSize) < chunkSize) break;

                skipCharCount = endChar;
                endChar = this.skipLetter(sourceReader, skipCharCount + chunkSize);
            }

            if (endChar < totalCharInFile) {            // process LAST cycle
                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, endChar, totalCharInFile, showTime));
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        this.poolExecutor.prestartAllCoreThreads();
        this.poolExecutor.shutdown();
        try {
            this.poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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

    public void countWords(Path path, Map<String, Long> resultMap, long startWord, long endWord, boolean showTime) {
        long s = System.currentTimeMillis();

        int charCount = 0;
        int charLetter = 0;
        StringBuilder word = new StringBuilder();

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(path.toFile()))) {
            sourceReader.skip(startWord);

            // FIXME: 4/27/2023 remake it with readline
            while ((charLetter = sourceReader.read()) != -1 && charCount < endWord - startWord) {   // FIXME: 4/25/2023 remake regex to recognize "*.*" for example java.Math
                if (charLetter == '\n' || charLetter == '\r' || charLetter == ' ' || charLetter == ',' || charLetter == '.' ||
                        charLetter == '!' || charLetter == '?' || charLetter == '\"' || charLetter == ':' || charLetter == '-' ||
                        charLetter == '_' || charLetter == ';' || charLetter == '&' || charLetter == '*' || charLetter == '(' ||
                        charLetter == ')' || charLetter == '{' || charLetter == '}' || charLetter == '“' || charLetter == '”' ||
                        charLetter == '‘' || charLetter == '/' || charLetter == '\\'|| charLetter == '•') {
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

            if (showTime)
                System.out.println(Thread.currentThread() + " read time: " + (System.currentTimeMillis() - s));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
