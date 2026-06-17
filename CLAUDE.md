# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Compile and package (output: target/zuul-1.0-SNAPSHOT.jar with lib/)
mvn package

# Compile without tests
mvn compile

# Run tests
mvn test

# Run a specific test class
mvn -Dtest=PlayerTest test

# Run the server (starts on http://localhost:8000)
mvn exec:java -Dexec.mainClass="cn.edu.whut.sept.zuul.GameServer"
# Or after packaging:
java -jar target/zuul-1.0-SNAPSHOT.jar
```

## Architecture

This is a "World of Zuul" text adventure game with a web-based graphical UI. The project is a Maven-based Java 8 application using `com.sun.net.httpserver` (no Spring Boot). The frontend is a single-page app (`webapp/index.html`) using Vue 3 and Bootstrap 5 loaded via CDN — no bundler or build step.

### Key layers

1. **HTTP layer** — `GameServer.java` starts an embedded HTTP server on port 8000. It registers handlers for static files and REST API endpoints (`/api/command`, `/api/state`, `/api/save`, `/api/load`, `/api/saves`, `/api/newgame`, `/api/exit`). The JAR manifest points to this class as the main class.

2. **Game engine** — `Game.java` holds all game state (rooms, player, history stack). It creates the world (rooms + exits + items) and processes commands. `Main.java` is the old CLI entry point (not actively used).

3. **Command pattern** — `CommandExecution` is an interface with `boolean execute(Game, Command)`. Nine concrete command classes (`CommandGo`, `CommandLook`, `CommandBack`, `CommandTake`, `CommandDrop`, `CommandItems`, `CommandEat`, `CommandHelp`, `CommandQuit`) are registered in `CommandWords`. `Parser.java` tokenizes input strings into `Command` objects.

4. **Domain model** — `Room` (exits in 4 directions + items), `TransporterRoom` (extends Room, behaves as a random teleporter), `Player` (name, current room, backpack, max carry weight), `Item` (description, weight, x/y screen coordinates for the 2D viewport).

5. **Persistence** — `DatabaseManager.java` wraps Hibernate 5.6 to persist game state to a local SQLite file (`zuul_game.db`). `GameStateDTO.java` is the serialization POJO used to save/restore the complete game state via Gson JSON stored in a TEXT column. `HibernateUtil.java` provides a thread-safe SessionFactory singleton. `SQLiteDialect.java` adapts Hibernate to SQLite.

### Data flow for a player action

```
Browser (Vue) → POST /api/command {command:"go east"}
  → GameServer.CommandHandler → Game.runCommand("go east")
    → Parser → Command → CommandWords.get("go").execute(game, cmd)
      → mutates Player/Room state
    → auto-saves via DatabaseManager.saveGame()
  → returns JSON {result: "...", state: {...}, items: [...], ...}
Browser re-renders the 2D room view
```

### Auto-save behavior

After every successful command, `GameServer` automatically persists the game state to SQLite under the save name `【自动存档】`. This happens on the server side, not via an explicit save API call.

### Frontend notes

- `webapp/index.html` contains all HTML, CSS, and JS inline (~80KB). No separate `.js` or `.css` files except for CDN-loaded libraries.
- Room backgrounds are static images (`webapp/images/bg-*.png`). Items have per-type icon images.
- The player character uses CSS sprite-based walking/attack animations.
- Items can be dragged/dropped within a room in the viewport.
