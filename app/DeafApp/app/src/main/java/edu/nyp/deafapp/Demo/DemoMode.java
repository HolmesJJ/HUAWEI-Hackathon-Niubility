package edu.nyp.deafapp.Demo;

public class DemoMode {

    private static boolean demoMode = false;
    private static boolean correctPronunciationDemo = false;

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setDemoMode(boolean demoMode) {
        DemoMode.demoMode = demoMode;
    }

    public static boolean isCorrectPronunciationDemo() {
        return correctPronunciationDemo;
    }

    public static void setCorrectDemo(boolean correctPronunciationDemo) {
        DemoMode.correctPronunciationDemo = correctPronunciationDemo;
    }
}
