package com.miniproject.CounterWordsInFile;


import java.nio.file.Path;
import java.util.Map;

/**
 * @author _KaTarin_
 * 3/30/2023
 */

public class ProcessThread implements Runnable {

    private TextService textService;

    private Path path;
    private Map<String, Long> originMap;
    private long skipCharCount;
    private long endChar;
    private boolean showTime;

    public ProcessThread() {
    }

    public ProcessThread(TextService textService, Path path, Map<String, Long> originMap, long skipCharCount, long endChar, boolean showTime) {
        this.textService = textService;
        this.path = path;
        this.originMap = originMap;
        this.skipCharCount = skipCharCount;
        this.endChar = endChar;
        this.showTime = showTime;
    }

    @Override
    public void run() {
        textService.countWords(this.path, this.originMap, this.skipCharCount, this.endChar, this.showTime);
    }

    public TextService getTextService() {
        return textService;
    }

    public void setTextService(TextService textService) {
        this.textService = textService;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Map<String, Long> getOriginMap() {
        return originMap;
    }

    public void setOriginMap(Map<String, Long> originMap) {
        this.originMap = originMap;
    }

    public long getSkipCharCount() {
        return skipCharCount;
    }

    public void setSkipCharCount(long skipCharCount) {
        this.skipCharCount = skipCharCount;
    }

    public long getEndChar() {
        return endChar;
    }

    public void setEndChar(long endChar) {
        this.endChar = endChar;
    }

    @Override
    public String toString() {
        return "ProcessThread{" +
                "path=" + path +
                ", originMap=" + originMap +
                ", startWord=" + skipCharCount +
                ", endWord=" + endChar +
                '}';
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }
}
