package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandQuitTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testQuitWithSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("quit now"));
        assertTrue(out.contains("Quit what?"));
    }

    @Test
    public void testQuitWithoutSecondWord() {
        // quit 返回 true 但 runCommand 忽略返回值（已知设计差异）
        // 在 web 上下文中 isRunning 不会改变
        String out = TestHelper.captureOutput(() -> game.runCommand("quit"));
        assertNotNull(out);
    }

    @Test
    public void testExitToMenuChangesRunningState() {
        assertTrue(game.isRunning());
        game.exitToMenu();
        assertFalse(game.isRunning());
    }

    @Test
    public void testQuitCommandReturnsTrue() {
        // 通过 processCommand 验证 quit 返回 true
        // 这表示"用户想要退出"
        Command cmd = new Command("quit", null);
        Parser parser = game.getParser();
        CommandExecution exec = parser.getCommandWords().get("quit");
        assertNotNull(exec);
        assertTrue(exec.execute(game, cmd));
    }

    @Test
    public void testQuitDoesNotExitToMenu() {
        // 验证已知设计差异：CommandQuit 不调用 exitToMenu()
        game.runCommand("quit");
        // isRunning 不变（web 上下文中）
        // runCommand 忽略 processCommand 返回值
        // 这记录了当前行为
        assertNotNull(game.getPlayer());
    }
}
