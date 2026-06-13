package cn.edu.whut.sept.zuul;

/**
 * 命令执行接口.
 * 所有的具体命令逻辑（如Go, Quit, Help）都必须实现此接口.
 */
public interface CommandExecution {
    /**
     * 执行命令.
     * @param game 游戏主对象，用于访问游戏状态（如当前房间）.
     * @param command 包含命令参数的命令对象.
     * @return 如果执行该命令后游戏需要结束，则返回 true；否则返回 false.
     */
    boolean execute(Game game, Command command);
}
