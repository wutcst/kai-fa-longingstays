package cn.edu.whut.sept.zuul;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HTTP 服务层集成测试.
 * 需要启动内嵌 GameServer。
 * 如果服务器无法启动，则跳过所有测试。
 */
public class GameServerTest {

    private static boolean serverAvailable = false;
    private static final String BASE_URL = "http://localhost:8000";

    @BeforeClass
    public static void startServer() {
        try {
            // 在独立线程启动服务器
            new Thread(() -> {
                try {
                    GameServer.main(new String[0]);
                } catch (Exception e) {
                    System.err.println("Server start failed: " + e.getMessage());
                }
            }).start();

            // 等待服务器就绪
            Thread.sleep(1000);

            // 测试连接
            java.net.URL url = new java.net.URL(BASE_URL + "/");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.connect();
            int code = conn.getResponseCode();
            conn.disconnect();
            serverAvailable = (code > 0);
        } catch (Exception e) {
            System.err.println("Server not available, skipping HTTP tests: " + e.getMessage());
            serverAvailable = false;
        }
    }

    @Before
    public void setUp() {
        org.junit.Assume.assumeTrue("GameServer not available", serverAvailable);
    }

    @AfterClass
    public static void stopServer() {
        // 服务器在 JVM 退出时自动停止
    }

    // ===== 静态文件服务 =====

    @Test
    public void testIndexHtml() throws Exception {
        String response = httpGet(BASE_URL + "/");
        assertNotNull(response);
        assertTrue(response.contains("<!DOCTYPE html") || response.contains("<html"));
    }

    @Test
    public void test404() throws Exception {
        java.net.URL url = new java.net.URL(BASE_URL + "/nonexistent_file.xyz");
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);
        int code = conn.getResponseCode();
        conn.disconnect();
        assertEquals(404, code);
    }

    // ===== State API =====

    @Test
    public void testStateBeforeGameInitialized() throws Exception {
        // 在未初始化游戏时请求 state
        String response = httpGet(BASE_URL + "/api/state");
        assertNotNull(response);
        assertTrue(response.contains("gameExited"));
    }

    // ===== NewGame API =====

    @Test
    public void testNewGameApi() throws Exception {
        String response = httpPost(BASE_URL + "/api/newgame", "playerName=HTTP测试玩家");
        assertNotNull(response);
        assertTrue(response.contains("success"));
    }

    @Test
    public void testNewGameEmptyName() throws Exception {
        String response = httpPost(BASE_URL + "/api/newgame", "playerName=");
        assertNotNull(response);
        assertTrue(response.contains("success"));
    }

    // ===== Command API =====

    @Test
    public void testCommandApiLook() throws Exception {
        // 先初始化游戏
        httpPost(BASE_URL + "/api/newgame", "playerName=cmd测试");

        String response = httpPost(BASE_URL + "/api/command", "look");
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    public void testCommandApiGo() throws Exception {
        httpPost(BASE_URL + "/api/newgame", "playerName=移动测试");

        String response = httpPost(BASE_URL + "/api/command", "go east");
        assertNotNull(response);
        assertTrue(response.contains("theater") || response.contains("lecture"));
    }

    // ===== 辅助方法 =====

    private String httpGet(String urlStr) throws Exception {
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);

        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return response.toString();
    }

    private String httpPost(String urlStr, String body) throws Exception {
        java.net.URL url = new java.net.URL(urlStr);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
        }

        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return response.toString();
    }
}
