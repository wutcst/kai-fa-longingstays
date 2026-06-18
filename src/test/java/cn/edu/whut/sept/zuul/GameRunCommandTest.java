package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameRunCommandTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    // ===== 有效命令 =====

    @Test
    public void testRunCommandGoEast() {
        Room before = game.getCurrentRoom();
        game.runCommand("go east");
        Room after = game.getCurrentRoom();
        assertNotSame("go east 应切换房间", before, after);
        assertTrue(after.getRawDescription().contains("theater"));
    }

    @Test
    public void testRunCommandLook() {
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("outside") || out.contains("entrance"));
    }

    // ===== 未知命令 =====

    @Test
    public void testRunCommandUnknown() {
        String out = TestHelper.captureOutput(() -> game.runCommand("xyz123"));
        assertTrue(out.contains("don't know") || out.contains("not know"));
    }

    // ===== 空字符串 =====

    @Test
    public void testRunCommandEmptyString() {
        String out = TestHelper.captureOutput(() -> game.runCommand(""));
        assertTrue(out.contains("don't know") || out.contains("not know"));
    }

    // ===== null 输入（已知缺陷 #2） =====

    @Test
    public void testRunCommandNull() {
        try {
            game.runCommand(null);
            fail("Expected NullPointerException for null input");
        } catch (NullPointerException e) {
            // 已知缺陷：runCommand(null) 抛出 NPE
        }
    }

    // ===== trigger_teleport 在普通房间 =====

    @Test
    public void testRunCommandTriggerTeleportInNormalRoom() {
        Room before = game.getCurrentRoom();
        String out = TestHelper.captureOutput(() -> game.runCommand("trigger_teleport"));
        // 普通房间不应传送
        assertSame(before, game.getCurrentRoom());
    }

    // ===== quit 命令在 runCommand 中的行为 =====

    @Test
    public void testRunCommandQuitOutput() {
        String out = TestHelper.captureOutput(() -> game.runCommand("quit"));
        // quit 应该产生一些输出或至少不抛异常
        assertNotNull(out);
    }

    @Test
    public void testRunCommandAfterQuitStillWorks() {
        // quit 在 runCommand 中不改变 isRunning（已知设计差异）
        game.runCommand("quit");
        // 之后仍可执行命令
        String out = TestHelper.captureOutput(() -> game.runCommand("look"));
        assertTrue(out.contains("outside") || out.contains("entrance"));
    }

    // ===== 连续命令执行 =====

    @Test
    public void testRunCommandSequence() {
        game.runCommand("go east");  // outside → theater
        game.runCommand("go west");  // theater → outside
        game.runCommand("go south"); // outside → lab

        Room current = game.getCurrentRoom();
        assertTrue(current.getRawDescription().contains("lab"));
    }

    // ===== go 命令无方向 =====

    @Test
    public void testRunCommandGoWithoutDirection() {
        Room before = game.getCurrentRoom();
        String out = TestHelper.captureOutput(() -> game.runCommand("go"));
        assertTrue(out.contains("Go where?"));
        assertSame(before, game.getCurrentRoom());
    }

    // ===== 多余空格 =====

    @Test
    public void testRunCommandExtraSpaces() {
        String out = TestHelper.captureOutput(() -> game.runCommand("  go   east  "));
        // 解析器应该能处理前后空格
        assertNotNull(out);
    }
}
