package cn.edu.whut.sept.zuul;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/** 测试工具类：捕获 System.out 输出 */
public class TestHelper {
    /**
     * 在捕获 System.out 的状态下执行操作，返回输出内容.
     */
    public static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }
}
