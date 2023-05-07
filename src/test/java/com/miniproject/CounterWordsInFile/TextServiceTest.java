package com.miniproject.CounterWordsInFile;

import com.miniproject.CounterWordsInFile.enums.ProgramMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

import java.io.BufferedReader;

import static org.assertj.core.api.Assertions.assertThat;

class TextServiceTest {

    private TextService textService = new TextService();

    @BeforeEach
    void setUp() {
        textService = new TextService();
    }

    @Test
    void shouldDetermineLineSeparatorAsCR() throws Exception {
        //given
        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(13).thenReturn(-1);

        //when
        Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);
        int separatorLineLength = textService.getSeparatorLineLength();

        //then
        int expected = 1;
        assertThat(separatorLineLength).isEqualTo(expected);
    }

    @Test
    void shouldDetermineLineSeparatorAsLF() throws Exception {
        //given
        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(10).thenReturn(-1);

        //when
        Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);
        int separatorLineLength = textService.getSeparatorLineLength();

        //then
        int expected = 1;
        assertThat(separatorLineLength).isEqualTo(expected);
    }

    @Test
    void shouldDetermineLineSeparatorAsCRLF() throws Exception {
        //given
        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(13, 10).thenReturn(-1);

        //when
        Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);
        int separatorLineLength = textService.getSeparatorLineLength();

        //then
        int expected = 2;
        assertThat(separatorLineLength).isEqualTo(expected);
    }

    @Test
    void shouldReturnNumberCharsInLineWithCR() throws Exception {

        //given
        String str = "This some mock text, check.\r";
        char[] charArray = str.toCharArray();
        Integer[] intArray = new Integer[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            intArray[i] = (int) charArray[i];
        }

        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(48, intArray).thenReturn(-1);

        //when
        int result = Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);

        //then
        int expected = str.length() + 1;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnNumberCharsInLineWithLF() throws Exception {

        //given
        String str = "This some mock text, check.\n";
        char[] charArray = str.toCharArray();
        Integer[] intArray = new Integer[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            intArray[i] = (int) charArray[i];
        }

        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(48, intArray).thenReturn(-1);

        //when
        int result = Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);

        //then
        int expected = str.length() + 1;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldReturnNumberCharsInLineWithCRLF() throws Exception {

        //given
        String str = "This some mock text, check.\r\n";
        char[] charArray = str.toCharArray();
        Integer[] intArray = new Integer[charArray.length];

        for (int i = 0; i < charArray.length; i++) {
            intArray[i] = (int) charArray[i];
        }

        BufferedReader br = Mockito.mock(BufferedReader.class);
        Mockito.when(br.read()).thenReturn(48, intArray).thenReturn(-1);

        //when
        int result = Whitebox.invokeMethod(textService, "determineLineSeparatorLengthInFile", br);

        //then
        int expected = str.length() + 1;
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldProgramModeTo_EXPLAIN_IN_SINGLE_THREAD_SetShowTimeTrueAndSetNumOfProcToOne() throws Exception {
        //given
        ProgramMode programMode = ProgramMode.EXPLAIN_IN_SINGLE_THREAD;

        //when
        Whitebox.invokeMethod(textService, "setModeForService", programMode);
        int numOfProcResult = textService.getNumOfProc();
        boolean showTimeResult = textService.isShowTime();

        //then
        int expectedNumOfProc = 1;
        boolean expectedShowTime = true;

        assertThat(numOfProcResult).isEqualTo(expectedNumOfProc);
        assertThat(showTimeResult).isEqualTo(expectedShowTime);
    }

    @Test
    void shouldProgramModeTo_SINGLE_THREAD_SetShowTimeFalseAndSetNumOfProcToOne() throws Exception {
        //given
        ProgramMode programMode = ProgramMode.SINGLE_THREAD;

        //when
        Whitebox.invokeMethod(textService, "setModeForService", programMode);
        int numOfProcResult = textService.getNumOfProc();
        boolean showTimeResult = textService.isShowTime();

        //then
        int expectedNumOfProc = 1;
        boolean expectedShowTime = false;

        assertThat(numOfProcResult).isEqualTo(expectedNumOfProc);
        assertThat(showTimeResult).isEqualTo(expectedShowTime);
    }

    @Test
    void shouldProgramModeTo_EXPLAIN_SetShowTimeTrueAndSetNumOfProcToSystemMax() throws Exception {
        //given
        ProgramMode programMode = ProgramMode.EXPLAIN;

        //when
        Whitebox.invokeMethod(textService, "setModeForService", programMode);
        int numOfProcResult = textService.getNumOfProc();
        boolean showTimeResult = textService.isShowTime();

        //then
        int expectedNumOfProc = Runtime.getRuntime().availableProcessors();
        boolean expectedShowTime = true;

        assertThat(numOfProcResult).isEqualTo(expectedNumOfProc);
        assertThat(showTimeResult).isEqualTo(expectedShowTime);
    }


    @Test
    void shouldProgramModeTo_DEFAULT_SetShowTimeFalseAndSetNumOfProcToSystemMax() throws Exception {
        //given
        ProgramMode programMode = ProgramMode.DEFAULT;

        //when
        Whitebox.invokeMethod(textService, "setModeForService", programMode);
        int numOfProcResult = textService.getNumOfProc();
        boolean showTimeResult = textService.isShowTime();

        //then
        int expectedNumOfProc = Runtime.getRuntime().availableProcessors();
        boolean expectedShowTime = false;

        assertThat(numOfProcResult).isEqualTo(expectedNumOfProc);
        assertThat(showTimeResult).isEqualTo(expectedShowTime);
    }



    @Test
    @Disabled
    void countWords() {
    }
}