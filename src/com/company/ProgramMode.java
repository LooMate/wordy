package com.company;

public enum ProgramMode {
    DEFAULT(0), SINGLE_THREAD(1), EXPLAIN(7), EXPLAIN_IN_SINGLE_THREAD(71);

    int id;

    ProgramMode(int id) {
    }

}
