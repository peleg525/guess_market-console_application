package gm.ui;

import gm.engine.GmEngine;
import gm.engine.GmEngineImpl;

public class Main {

    public static void main(String[] args) {
        GmEngine engine = new GmEngineImpl();
        new ConsoleApp(engine).run();
    }
}
