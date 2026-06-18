package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandHelpTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testHelpShowsAllCommands() {
        String out = TestHelper.captureOutput(() -> game.runCommand("help"));
        // 检查所有 10 个命令
        assertTrue(out.contains("go"));
        assertTrue(out.contains("look"));
        assertTrue(out.contains("back"));
        assertTrue(out.contains("take"));
        assertTrue(out.contains("drop"));
        assertTrue(out.contains("items"));
        assertTrue(out.contains("eat"));
        assertTrue(out.contains("buy"));
        assertTrue(out.contains("help"));
        assertTrue(out.contains("quit"));
    }

    @Test
    public void testHelpOutputIsConsistent() {
        String out1 = TestHelper.captureOutput(() -> game.runCommand("help"));
        String out2 = TestHelper.captureOutput(() -> game.runCommand("help"));
        assertEquals(out1, out2);
    }

    @Test
    public void testHelpShowsIntroduction() {
        String out = TestHelper.captureOutput(() -> game.runCommand("help"));
        assertTrue(out.contains("lost") || out.contains("alone") || out.contains("command words"));
    }
}
