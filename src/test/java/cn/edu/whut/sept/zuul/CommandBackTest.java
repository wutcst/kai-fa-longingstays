package cn.edu.whut.sept.zuul;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CommandBackTest {
    private Game game;

    @Before
    public void setUp() {
        game = new Game();
    }

    @Test
    public void testBackAfterMove() {
        game.runCommand("go east"); // outside → theater
        game.runCommand("back");
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
    }

    @Test
    public void testBackAtStart() {
        String out = TestHelper.captureOutput(() -> game.runCommand("back"));
        assertTrue(out.contains("can't go back"));
        // 仍在原房间
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
    }

    @Test
    public void testBackMultipleSteps() {
        game.runCommand("go east");  // outside → theater
        game.runCommand("go west");  // theater → outside
        game.runCommand("go south"); // outside → lab

        // 回退三步
        game.goBack(); // lab → outside
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));

        game.goBack(); // outside → theater
        assertTrue(game.getCurrentRoom().getRawDescription().contains("theater"));

        game.goBack(); // theater → outside
        assertTrue(game.getCurrentRoom().getRawDescription().contains("outside"));
    }

    @Test
    public void testBackWithSecondWord() {
        String out = TestHelper.captureOutput(() -> game.runCommand("back there"));
        assertTrue(out.contains("Back where?"));
    }

    @Test
    public void testBackAfterTeleport() {
        // 移动到 transporter 并触发传送
        game.runCommand("go west"); // outside → pub
        game.runCommand("go west"); // pub → transporter
        game.runCommand("trigger_teleport");

        // 回退应回到 transporter
        game.goBack();
        assertTrue(game.getCurrentRoom() instanceof TransporterRoom);
    }

    @Test
    public void testBackEmptyStackAfterMultipleBacks() {
        game.runCommand("go east"); // outside → theater
        game.goBack();              // theater → outside
        // 栈空，再次回退
        String out = TestHelper.captureOutput(() -> game.runCommand("back"));
        assertTrue(out.contains("can't go back"));
    }
}
