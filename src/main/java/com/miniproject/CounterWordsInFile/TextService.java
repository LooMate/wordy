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

    private final int numberOfAvailableProcessors;
    private int separatorLineLength;

    private boolean showTime;
    private int numOfProc;          // number of processors used by application

    public TextService() {
        this.numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();// set number of processors that available on a machine
    }


    public Map<String, Long> splitTaskAndCountWords(Path pathToFile, ProgramMode programMode) {
        Map<String, Long> resultMap = new ConcurrentHashMap<>();

        // this method counts all the chars in the file
        long timeStart = System.currentTimeMillis();

        long totalCharInFile = countCharsInFile(pathToFile);
        setModeForService(programMode);

        if (this.showTime) {
            System.out.println("Read with time: " + (System.currentTimeMillis() - timeStart));
        }


        // distribute the load to the threads
        BlockingQueue<Runnable> processThreadQueue = apportionTextToThreads(pathToFile, totalCharInFile, resultMap);

        runAllThreadPool(processThreadQueue);

        return resultMap;
    }

    // this method counts all the chars in the file
    private long countCharsInFile(Path pathToFile) {
        String line = "";
        long totalCharInFile = 0;       //total number of chars in file

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {//read all file and count

            totalCharInFile = determineLineSeparatorLengthInFile(sourceReader); //determine separator line length in the file

            while ((line = sourceReader.readLine()) != null) { // pass through the whole file and count
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
        return totalCharInFile;
    }


    private int determineLineSeparatorLengthInFile(BufferedReader sourceReader) throws IOException {
        int addCharToTotal = 0;
        int chr = 0;
        while ((chr = sourceReader.read()) != -1 && chr != 10 && chr != 13) {
            ++addCharToTotal;
        }
        if(chr == 10 || chr == 13) {
            this.separatorLineLength = 1;
            ++addCharToTotal;
        }
        if ((chr = sourceReader.read()) != -1 && (chr == 10 || chr == 13)) {
            this.separatorLineLength = 2;
            ++addCharToTotal;
        }

        return addCharToTotal;
    }

    // set the number of processors and showTime
    private void setModeForService(ProgramMode programMode) {
        switch (programMode) {
            case EXPLAIN_IN_SINGLE_THREAD:
                this.showTime = true;
            case SINGLE_THREAD:
                this.numOfProc = 1;
                break;
            case EXPLAIN:
                this.showTime = true;
            case DEFAULT:
                this.numOfProc = this.numberOfAvailableProcessors;
                break;
            }
    }

    //method that will distribute the load to the threads
    private BlockingQueue<Runnable> apportionTextToThreads(Path pathToFile, long totalCharInFile, Map<String, Long> resultMap) {

        long chunkSize = 0;             //chunks     to be processed in multiple threads
        long skipCharCount = 0;         //first line to be processed in a certain thread
        long endChar = 0;               //last line  to be processes in a certain thread
        long numOfSkipped = 0;          //actual number of skipped chars

        BlockingQueue<Runnable> processThreadQueue = new ArrayBlockingQueue<>(this.numOfProc);

        endChar = chunkSize = totalCharInFile / this.numOfProc;
        try (BufferedReader sourceReader = new BufferedReader(new FileReader(pathToFile.toFile()))) {

            numOfSkipped = sourceReader.skip(endChar);
            endChar = this.skipLetter(sourceReader, numOfSkipped);

            for (int i = 0; i < this.numOfProc--; ) { // FIXME: 5/4/2023 explain what's going on right here

                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, skipCharCount, endChar));

                if (sourceReader.skip(chunkSize) < chunkSize) break;

                skipCharCount = endChar;
                endChar = this.skipLetter(sourceReader, skipCharCount + chunkSize);
            }

            if (endChar < totalCharInFile && this.numOfProc > 0) {            // process LAST cycle
                processThreadQueue.add(new ProcessThread(this, pathToFile, resultMap, endChar, totalCharInFile));
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return processThreadQueue;
    }

    private void runAllThreadPool(BlockingQueue processThreadQueue) {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(this.numOfProc, this.numOfProc, 0,
                TimeUnit.MICROSECONDS, processThreadQueue);

        poolExecutor.prestartAllCoreThreads(); // run all threads in a thread pool
        poolExecutor.shutdown();               // does not take any more threads to execute

        try {
            poolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS); // wait for terminating all threads
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // it makes chunk's end at the end of the line
    private long skipLetter(BufferedReader sourceReader, long fromChar) throws IOException {
        switch (sourceReader.read()) {
            case -1:                                                    // the end of the file
            case '\n':                                                  // the end of the line CRLF(\r\n) of LF(\n),
                return fromChar + 1;                                        // so we jump to next line with +1
            case '\r': {                                                // the end of the CR(/r) or part of CRLF(\r\n)
                if (this.separatorLineLength == 2)
                    sourceReader.read(); // if it is part of CRLF(\r\n) read next char to maintain reader consistency
                return fromChar + this.separatorLineLength;             // and jump to the next line
            }
            default:
                return fromChar + sourceReader.readLine().length() + this.separatorLineLength + 1; // +1 to jump to next line
        }
    }

    // count words in the file using thread pool
    public void countWords(Path path, Map<String, Long> resultMap, long startWord, long endWord) {
        long s = System.currentTimeMillis();

        int charCount = 0;
        int charLetter = 0;
        StringBuilder word = new StringBuilder();

        try (BufferedReader sourceReader = new BufferedReader(new FileReader(path.toFile()))) {
            sourceReader.skip(startWord);       // skip to the start point for processing

            // read char by char and process
            while ((charLetter = sourceReader.read()) != -1 && charCount < endWord - startWord) {   // FIXME: 4/25/2023 remake regex to recognize "*.*" for example java.Math
                if (charLetter == '\n' || charLetter == '\r' || charLetter == ' ' || charLetter == ',' || charLetter == '.' ||
                        charLetter == '!' || charLetter == '?' || charLetter == '\"' || charLetter == ':' || charLetter == '-' ||
                        charLetter == '_' || charLetter == ';' || charLetter == '&' || charLetter == '*' || charLetter == '(' ||
                        charLetter == ')' || charLetter == '{' || charLetter == '}' || charLetter == '“' || charLetter == '”' ||
                        charLetter == '‘' || charLetter == '/' || charLetter == '\\' || charLetter == '•') {
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

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        if (this.showTime) {
            System.out.println(Thread.currentThread() + " read time: " + (System.currentTimeMillis() - s));
        }
    }


    public int getSeparatorLineLength() {
        return separatorLineLength;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public int getNumOfProc() {
        return numOfProc;
    }
}
