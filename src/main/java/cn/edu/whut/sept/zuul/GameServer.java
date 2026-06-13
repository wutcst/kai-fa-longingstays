package cn.edu.whut.sept.zuul;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * 简单的游戏 Web 服务器.
 * 提供静态文件服务和游戏 API 接口.
 */
public class GameServer {
    private static Game game;

    public static void main(String[] args) throws IOException {
        game = new Game(); // 初始化游戏实例

        // 创建服务器，监听 8000 端口
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // 1. 静态文件处理器 (服务 HTML/CSS/JS/图片)
        server.createContext("/", new StaticFileHandler());

        // 2. 游戏命令 API 处理器 (接收指令，返回文本反馈)
        server.createContext("/api/command", new CommandHandler());

        // 3. 游戏状态 API 处理器 (返回 JSON 数据供前端渲染)
        server.createContext("/api/state", new StateHandler());

        server.setExecutor(null); // 使用默认执行器
        System.out.println("Web Server started on http://localhost:8000");
        server.start();
    }

    /**
     * 静态文件处理器：读取 webapp 目录下的文件返回给浏览器
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            // 假设网页文件放在项目根目录的 webapp 文件夹下
            File file = new File("webapp" + path);

            if (file.exists()) {
                // 简单的 MIME 类型判断
                if (path.endsWith(".png")) t.getResponseHeaders().add("Content-Type", "image/png");
                else if (path.endsWith(".html")) t.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

                t.sendResponseHeaders(200, file.length());
                OutputStream os = t.getResponseBody();
                java.nio.file.Files.copy(file.toPath(), os);
                os.close();
            } else {
                String response = "404 Not Found";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    /**
     * 命令处理器：拦截 System.out 输出流，捕获游戏反馈
     */
    static class CommandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                // 读取前端发送的命令
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String cmd = br.readLine();

                // 准备捕获控制台输出
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos, true, "UTF-8");
                PrintStream oldOut = System.out; // 备份原来的 System.out
                System.setOut(ps); // 重定向到我们的缓存流

                // 执行游戏逻辑
                try {
                    // 调用 Game 的 runCommand 方法
                    // 如果你没有添加 runCommand，可以使用反射调用 private processCommand
                    game.runCommand(cmd);
                } catch (Exception e) {
                    System.out.println("Error executing command: " + e.getMessage());
                }

                // 恢复 System.out
                System.setOut(oldOut);
                ps.close();

                // 获取捕获到的文本
                String output = baos.toString("UTF-8");

                // 发送响应
                byte[] responseBytes = output.getBytes(StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = t.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        }
    }

    /**
     * 状态处理器：手动构建 JSON 字符串
     */
    static class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            Player player = game.getPlayer();
            Room currentRoom = game.getCurrentRoom();

            StringBuilder json = new StringBuilder();
            json.append("{");

            // 1. Player Info
            json.append("\"player\": {");
            json.append("\"name\": \"").append(jsonEscape(player.getName())).append("\",");
            json.append("\"weight\": ").append(player.getCurrentWeight()).append(",");
            json.append("\"maxWeight\": ").append(player.getMaxWeight());
            json.append("},");

            // 2. Player Inventory
            json.append("\"inventory\": [");
            ArrayList<Item> inventory = player.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                appendItemJson(json, inventory.get(i));
                if (i < inventory.size() - 1) json.append(",");
            }
            json.append("],");

            // 3. Room Info
            json.append("\"room\": {");
            json.append("\"desc\": \"").append(jsonEscape(currentRoom.getShortDescription())).append("\",");
            // 新增：向前端暴露当前房间可用的门（出口）
            json.append("\"exits\": {");
            json.append("\"north\": ").append(currentRoom.getExit("north") != null).append(",");
            json.append("\"south\": ").append(currentRoom.getExit("south") != null).append(",");
            json.append("\"east\": ").append(currentRoom.getExit("east") != null).append(",");
            json.append("\"west\": ").append(currentRoom.getExit("west") != null);
            json.append("},");

            // 4. Room Items
            json.append("\"items\": [");
            ArrayList<Item> roomItems = currentRoom.getItems();
            for (int i = 0; i < roomItems.size(); i++) {
                appendItemJson(json, roomItems.get(i));
                if (i < roomItems.size() - 1) json.append(",");
            }
            json.append("]");

            json.append("}"); // end room
            json.append("}"); // end root

            byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        // 辅助方法：拼接单个 Item 的 JSON
        private void appendItemJson(StringBuilder sb, Item item) {
            sb.append("{");
            // 简单处理：取冒号前的部分作为短名称 (例如 "生锈的铁锹：..." -> "生锈的铁锹")
            String fullName = item.getDescription();
            String shortName = fullName.contains("：") ? fullName.split("：")[0] : fullName;

            sb.append("\"name\": \"").append(jsonEscape(shortName)).append("\",");
            sb.append("\"desc\": \"").append(jsonEscape(fullName)).append("\",");
            sb.append("\"weight\": ").append(item.getWeight());
            sb.append("}");
        }

        // 辅助方法：JSON 转义
        private String jsonEscape(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
        }
    }
}

