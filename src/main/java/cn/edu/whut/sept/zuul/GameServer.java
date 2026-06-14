package cn.edu.whut.sept.zuul;

import cn.edu.whut.sept.zuul.db.DatabaseManager;
import cn.edu.whut.sept.zuul.db.GameSaveEntity;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单的游戏 Web 服务器.
 * 提供静态文件服务和游戏 API 接口，每次操作后自动存档.
 */
public class GameServer {
    private static Game game;
    private static boolean gameInitialized = false;
    /** 自动存档的固定存档名称 */
    private static final String AUTO_SAVE_NAME = "【自动存档】";

    public static void main(String[] args) throws IOException {
        // 不再在启动时初始化游戏世界，等待用户操作

        // 创建服务器，监听 8000 端口
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // 1. 静态文件处理器 (服务 HTML/CSS/JS/图片)
        server.createContext("/", new StaticFileHandler());

        // 2. 游戏命令 API 处理器 (接收指令，返回文本反馈，并自动存档)
        server.createContext("/api/command", new CommandHandler());

        // 3. 游戏状态 API 处理器 (返回 JSON 数据供前端渲染)
        server.createContext("/api/state", new StateHandler());

        // 4. 手动存档 API 处理器
        server.createContext("/api/save", new SaveHandler());

        // 5. 读档 API 处理器
        server.createContext("/api/load", new LoadHandler());

        // 6. 存档列表 API 处理器
        server.createContext("/api/saves", new SavesListHandler());

        // 7. 开始新游戏 API 处理器
        server.createContext("/api/newgame", new NewGameHandler());

        // 8. 退出到主菜单 API 处理器
        server.createContext("/api/exit", new ExitHandler());

        server.setExecutor(null);
        System.out.println("Web Server started on http://localhost:8000");
        server.start();
    }

    /**
     * 确保游戏已初始化（延迟初始化）.
     */
    private static void ensureGameInitialized() {
        if (!gameInitialized) {
            game = new Game();
            gameInitialized = true;
        }
    }

    /**
     * 执行自动存档：将当前游戏状态保存到"【自动存档】"记录.
     * 只有在游戏初始化后才执行.
     */
    private static void autoSave() {
        if (!gameInitialized || !game.isRunning()) return;
        try {
            String gameStateJson = game.serializeGameState();
            String playerName = game.getPlayer().getName();

            List<GameSaveEntity> saves = DatabaseManager.listSaves();
            boolean found = false;
            for (GameSaveEntity save : saves) {
                if (AUTO_SAVE_NAME.equals(save.getSaveName())) {
                    save.setGameStateJson(gameStateJson);
                    save.setPlayerName(playerName);
                    save.setCreatedAt(System.currentTimeMillis());
                    DatabaseManager.updateSave(save);
                    found = true;
                    break;
                }
            }
            if (!found) {
                DatabaseManager.saveGame(AUTO_SAVE_NAME, playerName, gameStateJson);
            }
        } catch (Exception e) {
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    /**
     * 静态文件处理器.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File("webapp" + path);

            if (file.exists()) {
                if (path.endsWith(".png")) t.getResponseHeaders().add("Content-Type", "image/png");
                else if (path.endsWith(".html")) t.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                else if (path.endsWith(".js")) t.getResponseHeaders().add("Content-Type", "application/javascript; charset=utf-8");

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
     * 命令处理器：执行游戏命令后自动存档.
     */
    static class CommandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equals(t.getRequestMethod())) {
                ensureGameInitialized();
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String cmd = br.readLine();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos, true, "UTF-8");
                PrintStream oldOut = System.out;
                System.setOut(ps);

                try {
                    game.runCommand(cmd);
                } catch (Exception e) {
                    System.out.println("Error executing command: " + e.getMessage());
                }

                System.setOut(oldOut);
                ps.close();

                String output = baos.toString("UTF-8");

                // 每次操作后自动存档
                autoSave();

                byte[] responseBytes = output.getBytes(StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = t.getResponseBody();
                os.write(responseBytes);
                os.close();
            }
        }
    }

    /**
     * 状态处理器.
     */
    static class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            StringBuilder json = new StringBuilder();
            if (!gameInitialized || !game.isRunning()) {
                json.append("{\"gameExited\": true}");
            } else {
                Player player = game.getPlayer();
                Room currentRoom = game.getCurrentRoom();

                json.append("{");
                json.append("\"gameExited\": false,");
                json.append("\"player\": {");
                json.append("\"name\": \"").append(jsonEscape(player.getName())).append("\",");
                json.append("\"weight\": ").append(player.getCurrentWeight()).append(",");
                json.append("\"maxWeight\": ").append(player.getMaxWeight());
                json.append("},");
                json.append("\"inventory\": [");
                ArrayList<Item> inventory = player.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    appendItemJson(json, inventory.get(i));
                    if (i < inventory.size() - 1) json.append(",");
                }
                json.append("],");
                json.append("\"room\": {");
                json.append("\"desc\": \"").append(jsonEscape(currentRoom.getShortDescription())).append("\",");
                json.append("\"exits\": {");
                json.append("\"north\": ").append(currentRoom.getExit("north") != null).append(",");
                json.append("\"south\": ").append(currentRoom.getExit("south") != null).append(",");
                json.append("\"east\": ").append(currentRoom.getExit("east") != null).append(",");
                json.append("\"west\": ").append(currentRoom.getExit("west") != null);
                json.append("},");
                json.append("\"items\": [");
                ArrayList<Item> roomItems = currentRoom.getItems();
                for (int i = 0; i < roomItems.size(); i++) {
                    appendItemJson(json, roomItems.get(i));
                    if (i < roomItems.size() - 1) json.append(",");
                }
                json.append("]");
                json.append("}"); // end room
            }
            json.append("}");

            byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private void appendItemJson(StringBuilder sb, Item item) {
            sb.append("{");
            String fullName = item.getDescription();
            String shortName = fullName.contains("：") ? fullName.split("：")[0] : fullName;
            sb.append("\"name\": \"").append(jsonEscape(shortName)).append("\",");
            sb.append("\"desc\": \"").append(jsonEscape(fullName)).append("\",");
            sb.append("\"weight\": ").append(item.getWeight());
            sb.append("}");
        }

        private String jsonEscape(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
        }
    }

    /**
     * 手动存档处理器.
     */
    static class SaveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            String response;

            if ("POST".equals(t.getRequestMethod())) {
                ensureGameInitialized();
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();

                String saveName = "手动存档";
                if (body != null && body.startsWith("saveName=")) {
                    saveName = URLDecoder.decode(body.substring(9), "UTF-8");
                }

                String gameStateJson = game.serializeGameState();
                String playerName = game.getPlayer().getName();

                long saveId = DatabaseManager.saveGame(saveName, playerName, gameStateJson);

                if (saveId > 0) {
                    response = "{\"success\": true, \"saveId\": " + saveId + ", \"message\": \"存档成功！\"}";
                } else {
                    response = "{\"success\": false, \"message\": \"存档失败！\"}";
                }
            } else {
                response = "{\"success\": false, \"message\": \"请使用 POST 方法\"}";
            }

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    /**
     * 读档处理器.
     */
    static class LoadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            String response;

            if ("GET".equals(t.getRequestMethod())) {
                String query = t.getRequestURI().getQuery();
                long saveId = -1;
                if (query != null && query.startsWith("saveId=")) {
                    try {
                        saveId = Long.parseLong(query.substring(7));
                    } catch (NumberFormatException e) {
                        response = "{\"success\": false, \"message\": \"无效的存档 ID\"}";
                        byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                        t.sendResponseHeaders(200, respBytes.length);
                        OutputStream os = t.getResponseBody();
                        os.write(respBytes);
                        os.close();
                        return;
                    }
                }

                GameSaveEntity entity = DatabaseManager.loadGame(saveId);
                if (entity != null) {
                    // 读档前确保 Game 已初始化（或者在读档时重新创建）
                    if (!gameInitialized) {
                        game = new Game();
                        gameInitialized = true;
                    }
                    game.deserializeGameState(entity.getGameStateJson());
                    response = "{\"success\": true, \"message\": \"读档成功！\", \"playerName\": \""
                            + jsonEscape(entity.getPlayerName()) + "\"}";
                } else {
                    response = "{\"success\": false, \"message\": \"未找到该存档！\"}";
                }
            } else {
                response = "{\"success\": false, \"message\": \"请使用 GET 方法\"}";
            }

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private String jsonEscape(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    /**
     * 存档列表处理器.
     */
    static class SavesListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            List<GameSaveEntity> saves = DatabaseManager.listSaves();
            StringBuilder json = new StringBuilder();
            json.append("{\"saves\": [");
            for (int i = 0; i < saves.size(); i++) {
                GameSaveEntity save = saves.get(i);
                json.append("{");
                json.append("\"id\": ").append(save.getId()).append(",");
                json.append("\"saveName\": \"").append(jsonEscape(save.getSaveName())).append("\",");
                json.append("\"playerName\": \"").append(jsonEscape(save.getPlayerName())).append("\",");
                json.append("\"createdAt\": ").append(save.getCreatedAt());
                json.append("}");
                if (i < saves.size() - 1) json.append(",");
            }
            json.append("]}");

            byte[] responseBytes = json.toString().getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private String jsonEscape(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    /**
     * 新游戏处理器：创建一个全新的游戏世界.
     */
    static class NewGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            String response;

            if ("POST".equals(t.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                String body = br.readLine();

                String playerName = "冒险者";
                if (body != null && body.startsWith("playerName=")) {
                    playerName = URLDecoder.decode(body.substring(11), "UTF-8");
                }
                if (playerName.trim().isEmpty()) {
                    playerName = "冒险者";
                }

                // 创建全新的游戏世界（不依赖之前的状态）
                game = new Game();
                gameInitialized = true;
                game.startNewGame(playerName.trim());
                // 新游戏后立刻自动存档
                autoSave();
                response = "{\"success\": true, \"message\": \"新游戏开始！\", \"playerName\": \""
                        + jsonEscape(playerName.trim()) + "\"}";
            } else {
                response = "{\"success\": false, \"message\": \"请使用 POST 方法\"}";
            }

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        private String jsonEscape(String input) {
            if (input == null) return "";
            return input.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    /**
     * 退出处理器.
     */
    static class ExitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            if (gameInitialized && game.isRunning()) {
                autoSave();
                game.exitToMenu();
            }
            String response = "{\"success\": true, \"message\": \"已退出到主菜单\"}";

            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            t.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = t.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    private static String jsonEscape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}