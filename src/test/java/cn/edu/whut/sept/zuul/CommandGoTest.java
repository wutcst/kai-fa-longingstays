package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandGoTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
        // Player 初始在 outside 房间
    }

    @Test
    public void testGoEast() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go east"));
        game.getPlayer(); // 确保不报错
        // Player 应在 theater
        assertNotNull(game.getCurrentRoom());
        assertTrue(game.getCurrentRoom().getRawDescription().contains("theater"));
        assertTrue(out.contains("theater"));
    }

    @Test
    public void testGoNorthNoDoor() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go north"));
        assertTrue(out.contains("no door"));
    }

    @Test
    public void testGoWestFromOutside() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go west"));
        assertTrue(game.getCurrentRoom().getRawDescription().contains("pub"));
    }

    @Test
    public void testGoSouthFromOutside() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go south"));
        assertTrue(game.getCurrentRoom().getRawDescription().contains("lab"));
    }

    @Test
    public void testGoWithoutDirection() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go"));
        assertTrue(out.contains("Go where?"));
    }

    @Test
    public void testGoInvalidDirection() {
        String out = TestHelper.captureOutput(() -> game.runCommand("go up"));
        assertTrue(out.contains("no door"));
    }

    @Test
    public void testGoMultipleSteps() {
        // outside → theater → outside
        game.runCommand("go east");   // outside → theater
        game.runCommand("go west");   // theater → outside
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
    }

    @Test
    public void testGoPushesHistory() {
        // 验证 go 后可以 back 回去
        game.runCommand("go east");   // outside → theater
        assertTrue(game.getCurrentRoom().getRawDescription().contains("theater"));
        String out = TestHelper.captureOutput(() -> game.goBack());
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
    }
}
