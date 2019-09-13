package android.util;

public class Assert {
    public static void assertTrue(boolean b) {
        if(!b) throw new RuntimeException();
    }

    public static void assertNotNull(Object b) {
        if(b==null) throw new RuntimeException();
    }
}
