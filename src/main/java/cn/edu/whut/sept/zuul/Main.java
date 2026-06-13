package cn.edu.whut.sept.zuul;

/**
 * 程序的入口点。
 * 该类只负责创建 Game 对象并启动游戏。
 */
public class Main {

    /**
     * 应用程序的主方法。
     * @param args 命令行参数（未使用）。
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.play();
    }
}
