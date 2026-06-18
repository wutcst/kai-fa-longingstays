# World of Zuul — 2D RPG 图形化冒险游戏

> **武汉理工大学 2026 软件工程实践 · 任务二：小组协同开发**
>
> 基于经典文字冒险游戏 "World of Zuul"（Michael Kölling & David J. Barnes）进行大规模扩展，将其重构为一款拥有完整 2D 图形化界面、数据库持久化、RPG 战斗系统和沉浸式音效的 Web 端冒险游戏。

---

## 目录

- [1. 项目简介](#1-项目简介)
- [2. 小组信息与详细分工](#2-小组信息与详细分工)
- [3. 完整开发历程](#3-完整开发历程)
- [4. 功能特性详解](#4-功能特性详解)
- [5. 技术架构详解](#5-技术架构详解)
- [6. 项目结构](#6-项目结构)
- [7. 构建、运行与操作指南](#7-构建运行与操作指南)
- [8. 需求完成情况](#8-需求完成情况)
  - [8.1 课设需求对照](#81-课设需求对照)
  - [8.2 测试体系总览](#82-测试体系总览)
  - [8.3 P0 批次：领域模型与核心命令](#83-p0-批次领域模型与核心命令94-个测试)
  - [8.4 P1 批次：扩展功能测试](#84-p1-批次扩展功能测试52-个测试)
  - [8.5 P2 批次：边界条件与解析器测试](#85-p2-批次边界条件与解析器测试60-个测试)
  - [8.6 P3 批次：集成测试与端到端测试](#86-p3-批次集成测试与端到端测试36-个测试)
  - [8.7 测试发现的关键缺陷](#87-测试发现的关键缺陷)
  - [8.8 测试执行命令速查](#88-测试执行命令速查)
- [9. 项目统计](#9-项目统计)
- [10. 致谢](#10-致谢)

---

## 1. 项目简介

《World of Zuul》原本是一款教学用的纯文字冒险游戏——玩家在由若干房间组成的迷宫中探索，通过输入文字指令（`go east`、`look`、`take` 等）与游戏世界交互。

本小组在保留原游戏核心逻辑（命令模式、房间-出口模型、解析器架构）的基础上，完成了以下维度的全面升级：

1. **从命令行到浏览器**：基于 Vue 3 构建 2D RPG 图形界面，支持角色精灵动画、物品图标拖拽、全屏房间背景、CSS 雪碧图动画
2. **从内存到数据库**：引入 Hibernate 5.6 + SQLite，实现多存档管理、自动存档、跨会话进度恢复
3. **从探索到战斗**：实装完整 RPG 战斗系统——攻击模组、Saber Boss 战、魔法技能（魔法子弹）、HP/Mana 双资源系统
4. **从静默到沉浸**：集成 BGM 背景音乐、按物品材质分类的丢弃音效、拾取音效
5. **从自由到规则**：引入房间碰撞边界系统、分轴碰撞检测、安全推离算法
6. **从英文到中文**：全局 UI 中文本地化，自订中文字体，新手引导传说背景

游戏背景设定为——苍穹裂变，时空倒转，玩家被召唤至失落的祖尔世界，需跨越六重秘境，收集散落的神器遗物，直面英灵之座上的守护者 Saber，完成试炼方可重归故土。

---

## 2. 小组信息与详细分工

| 姓名 | GitHub | 角色 | 分支 |
|------|--------|------|------|
| **范奕轩** | `1804426938` | 组长 / 前端与地图引擎 | `dev-FanYiXuan` |
| **王启赢** | `小赢一下` | 后端交互与音效 | `dev-WangQiYing` |
| **何宇航** | `heyuhang` | 数据库与战斗系统 | `dev-HeYuHang` |

> 小组采用 **GitHub Flow 分支模型**：每位成员在独立的个人分支上开发，通过 Pull Request 合并至 `master`。所有功能需求通过 GitHub Issues（#2 ~ #29）追踪和分配，每人对应独立的 Issue 编号段。

---

### 2.1 范奕轩（组长）— 前端架构与地图引擎负责人

**负责领域**：图形化界面、地图引擎、视觉体验、UI 汉化、胜利结算

**详细贡献：**

| 提交 | 说明 |
|------|------|
| `4fda4b4` init | 引入软件工程实践一的完整业务代码，包括：命令模式体系（9 个 Command 实现类）、Player/Room/Item 领域模型、Parser 解析器、Game 主控类、以及基于 `com.sun.net.httpserver` 的嵌入式 HTTP 服务器骨架（GameServer.java），为后续所有开发奠定基础 |
| `3241fdb` feat7_1 | **2D 图形化地图引擎**：创建 `webapp/index.html` 单页应用（最终 ~1400 行），基于 Vue 3（CDN）响应式框架 + Bootstrap 5 构建。实现 CSS 精灵动画系统（4×4 帧雪碧图，通过 `SpriteAnalyzer` 自动检测每帧底部边界实现精确锚定）、四方向角色移动（down/up/left/right 对应雪碧图 0%/33%/66%/100% 行偏移）、房间背景映射（6 个房间对应 6 张 `bg-*.png` 全景图） |
| `3124cff` feat7_2 | **2D RPG 互动地图引擎与门景透视**：设计基于百分比的房间物理坐标系统（x/y 范围 0-100），玩家通过走到房间边缘的"门区域"（距边界 4%，门宽 15%）触发场景切换。门近侧显示浮动气泡提示（CSS `exit-bubble`，金色边框 + `bubble-float` 动画），按下方向键确认后调用 `sendCommand('go ' + direction)` 切换房间 |
| `a9fd5d1` feat7_4 | **全屏背景与边缘碰撞**：为 6 个房间分别设计全景背景图（outside/theater/pub/lab/office/transporter），通过 CSS `object-fit: cover` 全屏铺满。实现 `EDGE_MIN=4` / `EDGE_MAX=96` 的硬边界碰撞，玩家不可走出画面之外 |
| `404e08f` feat7_5 | **魔法阵交互与传送**：创建 `TransporterRoom` 类（继承 Room），增加中央魔法阵交互区域（x: 35-65, y: 35-65）。玩家走入法阵时前端自动发送隐藏命令 `trigger_teleport`，后端调用 `getRandomRoom()` 将玩家随机传送至 5 个普通房间之一，法阵房间自身被排除在传送目标之外以防止连续传送死循环 |
| `ac5f400` fix7_3 | **物品坐标修复**：修复旧存档反序列化后物品坐标全部为 (0,0) 的问题。在 `Game.deserializeGameState()` 中为 17 种物品建立完整的 `contains()` 坐标映射表，当检测到 (0,0) 时自动恢复预设坐标 |
| `3bd7b57` fix7_3(2) | **背包面板丢弃坐标同步**：修复从背包拖拽丢弃物品时坐标异常的问题，确保 `CommandDrop` 中解析的坐标参数正确写入物品的 x/y 字段 |
| `bbbe9a6` feat7_7 | **胜利结算弹窗与 UI 汉化**：新增 `showVictoryDialog` 胜利结算弹窗，击败 Saber 且收集齐全部 4 件任务物品后触发，绿色边框 + 通关奖励（满血满蓝）。全局 UI 中文化——所有按钮（"开始新游戏"/"读取存档"/"保存"/"返回主菜单"）、提示信息、操作标签（拾取/丢弃/警告）均从英文迁移至中文。引入自订中文字体 `ActionFont`（MyFont.ttf）和 `BodyFont`（BodyFont.ttf） |
| `05616d5` style7_1 | **游戏指南 UI 优化**：重构游戏指南弹窗的视觉呈现——移除原有的 `border-radius` 硬边裁剪和矩形 `box-shadow` 发光效果，改用 CSS `filter: drop-shadow()` 自动识别 PNG 书本图片的不规则透明边缘，投射出真实的 3D 立体阴影（`drop-shadow(0 15px 30px rgba(0,0,0,0.8))`），实现书本与暗色背景的自然融合，消除"一刀切"的视觉割裂感 |
| `e94da31` fix | **清理误提交文件**：删除误提交至仓库的 CLAUDE.md 本地配置文件，并将 CLAUDE.md 加入 `.gitignore` 防止再次误提交 |

**核心技术实现细节：**

- **SpriteAnalyzer 算法**：通过 Canvas 读取雪碧图 `naturalWidth/naturalHeight`，计算 `frameW = totalW/4`、`frameH = totalH/4`。从每帧底部向上扫描 Alpha 通道（`alpha > 128`）检测底部 Y 坐标，计算 `avgFootRatio` 用于 `translateY` 变换实现像素级精确定位
- **攻击动画**：`@keyframes attack-swing` — 0%→25% 蓄力（背景位移 0%→30%），25%→50% 挥砍（30%→60% 配合 opacity 闪白），50%→100% 收刀（60%→100% + 渐隐），总时长 1s
- **方向映射**：`.sprite-dir-down { background-position-y: 0% }`、`.sprite-dir-left { 33.33% }`、`.sprite-dir-right { 66.66% }`、`.sprite-dir-up { 100% }`
- **房间识别**：通过 `state.room.desc` 中的子串匹配（`includes('outside')` / `includes('pub')` / `includes('theater')` / `includes('lab')` / `includes('office')` / `includes('mysterious')`）确定当前房间并加载对应背景

---

### 2.2 王启赢 — 后端交互逻辑与音效系统负责人

**负责领域**：前后端交互逻辑、数据库同步、音效系统、碰撞机制、游戏玩法完善

**详细贡献：**

| 提交 | 说明 |
|------|------|
| `8b0a592` feat7_3+feat8_2 | **前端交互逻辑改进 + 数据库同步**：重构前端的物品交互流程——物品拾取/丢弃操作通过 REST API 同步至后端 Game 引擎，再由后端自动存档机制写入 SQLite，确保图形界面操作与数据库状态的一致性。具体包括：F 键拾取最近物品（距离 < `PICKUP_RANGE=10` 时高亮发光，`@keyframes glow-pulse` 脉冲动画）、G 键丢弃背包物品到玩家当前位置（附带精确坐标参数）、E 键吃魔法饼干触发 `CommandEat`（增加负重 10kg + 恢复 50HP） |
| `1f51921` feat7_6 | **BGM 与音效系统（初版）**：集成 HTML5 Audio API，实现 BGM 背景音乐（`/sounds/bgm.mp3`，循环播放，音量 0.3）、拾取音效（`/sounds/pickup.mp3`，音量 0.6）、按物品材质分类的丢弃音效系统。`getDropSoundId(itemName)` 通过关键词匹配分发：玻璃类（烧杯/试剂瓶/酒瓶）→ `drop-glass.mp3`、陶瓷类（咖啡杯）→ `drop-cup.mp3`、金属类（铁锹/剑/显微镜）→ `drop-metal.mp3`、默认 → `drop.mp3` |
| `386933d` feat7_6 | **音效系统（重提交）**：修复初版合并中出现的问题，重新提交完整音效功能 |
| `9f687eb` fix | **恢复 revert 误删文件**：因 PR #22 的 revert 操作（`7538396`）误删了掉落音效文件及 `index.html` 中被连带删除的 DOM 元素和 CSS 样式，手动修复恢复所有丢失内容 |
| `48953a3` feat7_8 | **碰撞机制与玩法完善**：为每个房间定义 `ROOM_COLLISIONS` 碰撞矩形数组，实现圆形-矩形碰撞检测（AABB vs Circle）和分轴滑动碰撞响应算法。同时完善任务物品收集系统——每局从 17 种候选物品中随机抽取 4 件作为收集目标，背包面板实时显示收集进度（已收集显示绿色勾选 ✓，未收集显示灰色方块 ◻），击败 Saber 前需集齐全部任务物品 |

**核心技术实现细节：**

- **碰撞数据结构**：每个房间的碰撞区域定义为 `{ x: number, y: number, w: number, h: number }`（百分比坐标）数组。例如 theater 房间有 8 个碰撞矩形（舞台边缘 38×2、演讲台 1×1、4 组座位 15×0.2 等）、pub 房间有 6 个（吧台 30×5、5 个吧凳区域 2×2）
- **碰撞检测算法（`checkCollision`）**：采用标准 AABB vs Circle 检测 — `closestX = max(rect.x, min(playerX, rect.x + rect.w))`，同理计算 `closestY`，然后判断 `distance² < radius²`
- **分轴滑动响应**：游戏循环中先沿 X 轴移动并检测碰撞（碰撞则回退 X），再沿 Y 轴移动并检测碰撞（碰撞则回退 Y），实现沿墙滑动的顺滑体验
- **安全推离（`pushOutOfCollision`）**：传送或回退后调用，检测玩家是否卡在碰撞区内，若重叠则沿最短法线方向推出（距离极小 < 0.001 时默认向上推出）

---

### 2.3 何宇航 — 数据库持久化与战斗系统负责人

**负责领域**：数据库架构、存档系统、RPG 战斗引擎、Boss AI

**详细贡献：**

| 提交 | 说明 |
|------|------|
| `09645c9` feat | **数据库支持与存档系统**：引入 Hibernate 5.6.15 ORM 框架，配置 SQLite 3.44 本地数据库（`zuul_game.db`）。设计完整的持久化层架构：`HibernateUtil.java` 实现线程安全的 SessionFactory 单例（双重检查锁定模式）、`SQLiteDialect.java` 自定义 Hibernate 方言适配（注册 SQLite 特有数据类型映射——BOOLEAN→integer、CLOB→text，覆盖 `supportsLimit()`/`dropConstraints()`/`hasAlterTable()` 方法，自定义 `IdentityColumnSupport` 返回 `select last_insert_rowid()` 作为自增 ID 查询语句）、`DatabaseManager.java` 提供 7 个静态 CRUD 方法（`saveGame`/`loadGame`/`listSaves`/`listSavesMeta`/`findSaveByName`/`updateSave`/`deleteGame`）、`GameSaveEntity.java` JPA 实体映射到 `game_saves` 表（字段：`id` 自增主键、`save_name` varchar 100、`player_name` varchar 100、`game_state_json` TEXT、`created_at` long、`data_size` int） |
| `7d5b3c2` fix | **存档模块异常修复**：修复 Hibernate 事务处理中的异常问题 |
| `6be6ee5` fix | **读档数据读取修复**：修复 `Game.deserializeGameState()` 中房间出口连接与物品重建逻辑的缺陷，确保反序列化后房间-出口拓扑结构完整恢复 |
| `0fbafaa` fix8_1 | **忽略游戏数据**：将 `zuul_game.db` 及 SQLite 相关运行时文件加入 `.gitignore` |
| `167e143` feat9_1 | **RPG 战斗系统**：这是项目中最复杂的功能模块，包含以下子系统：|
| `5a1ab9c` feat | **金币系统、酒水商店与任务系统，优化 NPC 交互**：新增三大游戏功能——（1）**金币系统**：玩家初始 30 金币（`playerState.coins = 30`），游戏画面顶部状态栏和右上角面板实时显示金币数量 🪙；（2）**酒水商店**：在校园酒吧（pub）房间新增酒吧老板 NPC（`barmaid.png` 雪碧图），玩家靠近（距离 < 10%）自动触发对话框，可购买麦脉暖酿（20🪙，饮用 HP+20）和月花蜜醴（30🪙，饮用 MP+20）；（3）**博士任务系统**：在计算机实验室（lab）房间新增博士 NPC（`doctor.png` 雪碧图），引入三阶段任务链——`find_doctor`（寻找博士）→ `collect_items`（收集任务物品）→ `completed`（交付完成），玩家集齐 4 件任务物品后交付博士可获得 100 金币奖励并移除任务物品。同时将玩家初始负重上限从 10kg 提升至 20kg，新增 `CommandBuy` 购买命令，扩展 `CommandEat` 支持酒水类物品消耗 |
| `be42a98` fix | **酒水实体修改**：修复酒水物品（麦脉暖酿/月花蜜醴）在前端渲染和交互流程中的显示问题，确保酒水图标、快捷栏映射和饮用效果正确生效 |
| `68e6f3a` feat9_2 | **道具快捷栏**：在游戏画面右下角新增 3 个圆形快捷栏位组件（52×52px，圆形，半透明暗色背景）。背包前 3 件物品按顺序自动填充至栏位，每个栏位显示物品图标（28×28px）和名称标签。支持数字键 `1`/`2`/`3` 快速选中栏位（金色边框高亮 `#ffd700`），`R` 键使用选中物品（自动识别可食用/饮用类型并触发对应效果），`G` 键丢弃选中物品至当前位置。空栏位显示"—"占位符。`refreshQuickSlots()` 在每次状态刷新时同步栏位数据 |

**战斗系统详细设计：**

1. **玩家战斗属性**（前端 `playerState` reactive 对象）：
   - `hp: 100` / `maxHp: 100` — 生命值
   - `mp: 100` / `maxMp: 100` — 法力值
   - `attackPower: 10` / `baseAttack: 10` — 攻击力（拾取道具剑后 +10 提升至 20）
   - `attacking: bool` — 是否正在攻击（控制挥剑动画播放）
   - `attackDir: string` — 攻击方向（控制攻击精灵图朝向）

2. **Saber Boss 属性**（`saber` reactive 对象）：
   - `hp: 200` / `maxHp: 200` — Boss 生命值（玩家 2 倍）
   - `damage: 10` — 每次近战攻击造成 10 点伤害
   - `battleMode: bool` — 是否进入战斗状态
   - `alert: bool` — 警戒状态（防止重复触发战斗对话框）
   - `visible: bool` — 是否在地图中显示

3. **近战攻击系统**（J 键或攻击按钮触发）：
   - 检查 Saber 是否处于战斗模式且距离 < 10%
   - 造成 `playerState.attackPower` 点伤害（基础 10，装备道具剑后 20）
   - 播放攻击动画（CSS `attack-swing`，`player-attack` 元素 1.6 倍缩放）
   - 攻击持续 1020ms 后自动复位

4. **魔法子弹系统**（Q 键触发）：
   - 消耗 10 MP，沿玩家当前方向发射
   - 每帧移动 `BULLET_SPEED * 0.05`（速度值为 5）
   - 碰撞检测：撞墙（碰撞矩形）或出界（< 2 或 > 98）时移除
   - 击中 Saber（距离 < 6）造成 `BULLET_DAMAGE = 10` 点伤害并移除子弹
   - 使用 CSS 背景图片 `/images/Magic_Bullet.png` 渲染

5. **治疗系统**（E 键触发）：
   - 消耗 30 MP，恢复 20 HP
   - MP 不足时显示警告

6. **Saber AI 系统**（`startSaberAI()`，`setInterval` 500ms）：
   - **追击状态**（距离 > 8）：以速度 1 向玩家方向移动，沿较大坐标差的方向移动，坐标钳制在 [4, 96] 范围内
   - **攻击状态**（距离 <= 8）：停止移动，触发近战攻击——设置 `saber.isAttacking = true`，造成 10 点伤害，攻击动画持续 1020ms
   - 每 500ms 重新评估距离，动态切换追击/攻击状态

7. **战斗结算逻辑**：
   - **胜利条件**：Saber HP ≤ 0 且任务物品全部收集完成 → 弹出 `showVictoryDialog`（绿色边框，显示"🏆 战斗胜利！"）
   - **未完成击杀**：Saber HP ≤ 0 但任务物品未集齐 → Saber 满血复活并返回坐标 (50,50)，提示"任务未完成"
   - **战败处理**：玩家 HP ≤ 0 → 弹出 `showDefeatDialog`（红色边框），提供两个选项——"重新对战"（满血满蓝重置，Saber 回到 50,50，玩家回到 50,80，AI 重新启动）或"离开对战"（退出战斗模式，玩家放置到 50,90）

**存档系统详细设计：**

- **自动存档机制**：每次 `/api/command` 调用后自动执行 `autoSave()`，存档名固定为 `【自动存档】`。通过 `findSaveByName()` 查找同名存档——存在则 `updateSave()`（保留原始 `createdAt` 时间戳），不存在则 `saveGame()` 插入新行
- **`GameStateDTO` 序列化方案**：使用 Gson 将完整游戏状态转换为 JSON，包含：`playerName`、`currentRoomId`、`maxWeight`、`inventory`（ItemDTO 数组）、`rooms`（LinkedHashMap，保留房间顺序）、`historyRoomIds`（历史栈的房间 ID 列表）、`requiredItems`（任务物品名称列表）
- **反序列化五步重建**：① 重建所有 Room 对象（区分普通 Room 与 TransporterRoom）→ ② 重建出口连接（遍历 exits map）→ ③ 重建房间物品（含坐标回退映射）→ ④ 重建 Player（名称/当前位置/最大负重/背包物品）→ ⑤ 重建历史栈
- **轻量存档列表查询**：`listSavesMeta()` 使用 HQL `SELECT id, saveName, playerName, createdAt FROM GameSaveEntity ORDER BY createdAt DESC`，避免加载 TEXT 大字段提升性能

---

## 3. 完整开发历程

整个项目在 **6 天**（2026 年 6 月 13 日—18 日）内完成，共 **26 次功能提交**、**15 个 Pull Request**。

### 📅 时间线总览

```
2026-05-12  ■  GitHub Classroom 初始化仓库
2026-06-06  ■  添加任务截止日期
            │
2026-06-13  ■■ 第一阶段：基础搭建（2 次提交，2 个 PR）
            │   ├── init: 引入核心业务代码与 Web 资源
            │   └── feat7_1: 2D 图形化地图引擎
            │
2026-06-14  ■■■ 第二阶段：存档系统（3 次提交，1 个 PR）
            │   ├── feat: Hibernate + SQLite 存档与读档
            │   ├── fix: 存档模块异常修复
            │   └── fix: 读档进度数据读取修复
            │
2026-06-15  ■■■■■■■■ 第三阶段：功能密集开发（8 次提交，5 个 PR）
            │   ├── fix8_1: 忽略游戏数据文件
            │   ├── feat7_2: 2D RPG 互动地图引擎与门景透视
            │   ├── feat7_3+8_2: 前端交互改进 + 数据库同步
            │   ├── fix7_3: 物品坐标 (0,0) 修复
            │   ├── fix7_3(2): 背包丢弃坐标同步
            │   ├── feat7_4: 全屏背景与边缘碰撞
            │   ├── feat7_5: 魔法阵交互与传送逻辑
            │   └── feat7_6: BGM 与音效系统（含 revert 事故）
            │
2026-06-16  ■■ 第四阶段：战斗系统与修复（2 次提交，1 个 PR）
            │   ├── fix: 恢复 revert 误删文件
            │   └── feat9_1: RPG 战斗系统（攻击/Saber/魔法/道具）
            │
2026-06-17  ■■■ 第五阶段：最终完善（4 次提交，3 个 PR）
            ├── feat7_7: 胜利结算弹窗与全局 UI 汉化
            ├── feat7_8: 碰撞机制与游戏玩法完善
            ├── style7_1: 游戏指南 UI 优化（透明投影融合）
            └── fix: 清理误提交文件
            │
2026-06-18  ■■ 第六阶段：扩展完善（3 次提交，2 个 PR）
            ├── feat: 金币系统 + 酒水商店 + 博士任务 + NPC 交互
            ├── fix: 酒水实体修改
            └── feat9_2: 道具快捷栏
```

---

### 第一阶段：基础搭建（6 月 13 日）

**PR #2 — 代码引入 + 2D 地图引擎**

**`4fda4b4` init（范奕轩）**

从软件工程实践一中引入完整业务代码。此时项目已具备：
- **命令模式体系**：`CommandExecution` 接口 + 10 个具体命令类（`CommandGo`、`CommandLook`、`CommandBack`、`CommandTake`、`CommandDrop`、`CommandItems`、`CommandEat`、`CommandBuy`、`CommandHelp`、`CommandQuit`），通过 `CommandWords` 注册表统一管理
- **领域模型**：`Player`（姓名、当前位置、背包 ArrayList、最大负重）、`Room`（描述、HashMap 出口、物品列表）、`Item`（描述、重量、x/y 坐标）
- **解析器**：`Parser` 类负责将用户输入字符串 token 化并构造 `Command` 对象
- **HTTP 服务器骨架**：`GameServer.java` 基于 JDK 内置 `com.sun.net.httpserver` 在 8000 端口提供静态文件服务和 `/api/command` 端点
- **Web 基础界面**：`webapp/index.html` 具备基本的 HTML 结构和命令输入框
- **Maven 构建配置**：`pom.xml` 包含 JUnit 4.13.2 测试依赖

**`3241fdb` feat7_1（范奕轩，Resolve #2）**

完成 2D 图形化地图引擎，这是项目从 CLI 到 GUI 的关键转折：
- 使用 Vue 3 CDN 版（`vue.global.js`）构建响应式单页应用
- 引入 Bootstrap 5.3.0 CDN 提供 UI 组件和样式基础
- **雪碧图动画系统**：将角色帧动画组织为 4×4 网格 PNG 雪碧图，通过 CSS `background-position-x/y` 偏移实现逐帧切换。`SpriteAnalyzer` 工具函数通过 Canvas API 读取图像并逐帧扫描 Alpha 通道检测底部边界，自动计算 `avgFootRatio` 为每帧提供精确的 `translateY` 锚定偏移
- **四方向角色**：CSS 类 `.sprite-dir-down/left/right/up` 分别对应 `background-position-y: 0%/33.33%/66.66%/100%`
- **房间背景映射**：`currentRoomBg` computed 属性根据 `state.room.desc` 中的关键词匹配加载对应 PNG 背景图
- 移除旧的命令行输入框，改为全屏沉浸式游戏画面

---

### 第二阶段：存档系统（6 月 14 日）

**PR #6 — 数据库持久化（何宇航）**

**`09645c9` feat（何宇航）**

引入完整的数据库持久化层，包括：

*持久化架构：*
- `HibernateUtil.java`：线程安全的 SessionFactory 单例（双重检查锁定 `volatile` 字段），调用 `new Configuration().configure().buildSessionFactory()`
- `SQLiteDialect.java`：自定义 Hibernate 方言，注册 SQLite 数据类型映射（`BOOLEAN` → `integer`、`CLOB` → `text`、`BIGINT` → `integer`），覆盖 `supportsLimit() = true`、`dropConstraints() = false`、`hasAlterTable() = false`、`supportsIfExistsBeforeTableName() = true`。自定义 `IdentityColumnSupport` 返回 `"integer"` 作为标识列类型、`"select last_insert_rowid()"` 作为自增 ID 查询
- `DatabaseManager.java`：7 个静态方法——`saveGame(saveName, playerName, gameStateJson)` 返回 `long id`、`loadGame(saveId)` 返回 `GameSaveEntity`、`listSaves()` 返回完整实体列表、`listSavesMeta()` 返回轻量元数据列表（HQL 投影查询避免加载 TEXT 大字段）、`findSaveByName(saveName)` 参数化 HQL 查询、`updateSave(id, saveName, playerName, gameStateJson)` 使用 `session.merge()`、`deleteGame(saveId)` 按 ID 删除
- `GameSaveEntity.java`：JPA `@Entity` 映射到 `game_saves` 表，字段 `id`（`@Id @GeneratedValue`）、`saveName`（`@Column(name = "save_name", length = 100)`）、`playerName`（`@Column(name = "player_name", length = 100)`）、`gameStateJson`（`@Column(name = "game_state_json", columnDefinition = "TEXT")`）、`createdAt`（long 时间戳）、`dataSize`（int 字符数）
- `GameStateDTO.java`：完整的序列化 POJO，使用 Gson 双向转换。嵌套 `ItemDTO`（description、weight、x、y）和 `RoomDTO`（id、description、isTransporter、exits map、items list）。顶层字段：`playerName`、`currentRoomId`、`maxWeight`、`inventory`、`rooms`（LinkedHashMap）、`historyRoomIds`、`requiredItems`

*API 端点扩展：*
- `POST /api/save` — 手动存档，读取表单编码的 `saveName` 参数
- `GET /api/load?saveId=N` — 读档，创建新 Game 实例并调用 `deserializeGameState()`
- `GET /api/saves` — 获取存档列表（JSON 数组，包含 id/saveName/playerName/createdAt）
- `POST /api/newgame` — 开始新游戏，调用 `startNewGame(playerName)`
- `POST /api/exit` — 退出到主菜单

*自动存档机制：*
- 每次 `/api/command` 成功执行后自动触发 `autoSave()`
- 固定存档名 `【自动存档】`，通过 `findSaveByName()` + `updateSave()` 原地更新
- 仅在游戏初始化后且未退出时执行（`gameInitialized && game.isRunning()`）

**`7d5b3c2` fix（何宇航）**

修复 Hibernate 事务处理中的边界条件问题。

**`6be6ee5` fix(save-system)（何宇航）**

修复 `deserializeGameState()` 中两个关键缺陷：
- 房间出口连接的反序列化顺序问题——需先创建所有 Room 对象，再遍历 exits map 逐一 `setExit()` 建立连接
- 物品坐标重建时未正确处理 `Item` 构造函数重载，导致使用随机坐标构造器而非精确坐标构造器

**PR #7 — 忽略游戏数据（何宇航）**

**`0fbafaa` fix8_1（何宇航）**

将 `zuul_game.db` 加入 `.gitignore`，避免运行时产生的本地数据库文件被误提交至代码仓库。

---

### 第三阶段：地图引擎与交互增强（6 月 15 日）

**PR #9 — 2D RPG 互动地图引擎（范奕轩）**

**`3124cff` feat7_2（范奕轩）**

- 设计基于百分比的房间物理坐标系统（x/y 范围 0-100），玩家位置、物品位置、门位置均使用百分比表示
- 实现门近侧检测与场景切换：玩家靠近房间边缘（距边界 4% 以内，水平方向 ±7.5% / 垂直方向 ±7.5% 为门宽）时显示浮动气泡提示，确认方向后切换房间
- 浮动气泡 UI：CSS 类 `.exit-bubble`，使用金色边框（`border: 2px solid rgba(255,215,0,0.6)`）、半透明黑色背景、`@keyframes bubble-float` 上下浮动动画（`translateY` ±3px，周期 1.8s）
- 气泡文字映射：根据方向和目标房间动态显示"前往：大学正门外"/"前往：剧院大厅"/"前往：校园酒吧"/"前往：计算机实验室"/"前往：计算机管理办公室"/"前往：未知时空（法阵）"
- 优化房间边缘视觉过渡，减少切换时的闪烁感

**PR #12 — 前端交互与数据库同步（王启赢）**

**`8b0a592` feat7_3 + feat8_2（王启赢）**

- 重构前端物品交互流程，所有操作通过 REST API 同步至后端
- **拾取流程**：F 键 → 查找最近物品（距离 < `PICKUP_RANGE=10`）→ 物品高亮发光（`glow-pulse` 动画，金色 `box-shadow` + `brightness(1.3)`）→ 调用 `sendCommand('take ' + name)` → 后端执行 `CommandTake` → 自动存档
- **丢弃流程**：G 键 → 获取背包选中物品 → 调用 `sendCommand('drop ' + name + ' ' + Math.round(x) + ' ' + Math.round(y))` → 后端执行 `CommandDrop`（解析坐标参数，设置物品 x/y 后放入当前房间）→ 自动存档
- **进食流程**：E 键 → 物品名含"饼干"则调用 `CommandEat` → 后端 `player.increaseMaxWeight(10.0)` + `player.dropItem(item)` → 前端同步显示"负重上限提升至 20kg！"
- 数据库交互同步：确保图形界面操作与 SQLite 持久化状态完全一致

**PR #14 — 物品坐标修复（范奕轩）**

**`ac5f400` fix7_3（范奕轩）**

- 问题根因：旧存档中物品坐标以默认 (0,0) 存储，反序列化时 `Item` 构造器接收到 (0,0) 后不会触发随机坐标逻辑
- 修复方案：在 `Game.deserializeGameState()` 的物品重建循环中，检测 `ix == 0.0 && iy == 0.0` 时，通过 `itemDTO.getDescription().contains()` 关键词匹配恢复预设坐标（17 个物品 → 17 组坐标映射）
- 例如：含"铁锹" → (5, 65)、含"地图" → (75, 70)、含"显微镜" → (25, 55)、含"吧台" → (50, 55) 等

**PR #16 — 背包面板坐标同步（范奕轩）**

**`3bd7b57` fix7_3(2)（范奕轩）**

- 修复背包面板中拖拽丢弃物品时坐标未正确写入的问题
- `CommandDrop.execute()` 中坐标参数解析后需显式调用 `item.setX(dropX)` 和 `item.setY(dropY)`
- 同时为背包物品的反序列化增加防错处理——若背包物品坐标也为 (0,0)，随机分配 20-80 范围内的坐标

**PR #18 — 全屏背景与边缘碰撞（范奕轩）**

**`a9fd5d1` feat7_4（范奕轩，Resolve #17）**

- 为全部 6 个房间设计专属全景背景图：`bg-outside.png`（大学正门广场）、`bg-theater.png`（演讲厅内部）、`bg-pub.png`（校园酒吧）、`bg-lab.png`（计算机实验室）、`bg-office.png`（行政办公室）、`bg-transporter.png`（神秘传送室）
- 背景通过 CSS `object-fit: cover` 全屏铺满视口，`position: absolute` 叠加在游戏画面底层
- 房间切换时通过 Vue 的 `computed` 属性 `currentRoomBg` 动态切换背景图 URL
- 实现 `EDGE_MIN = 4` / `EDGE_MAX = 96` 边界约束，玩家坐标钳制在 [4, 96] 范围内

**PR #20 — 魔法阵与传送（范奕轩）**

**`404e08f` feat7_5（范奕轩，Resolve #19）**

- 创建 `TransporterRoom`（继承 `Room`），对应描述 `"in a mysterious room with glowing symbols"`
- 传送房间位于 Pub 的西侧出口（`pub.setExit("west", transporter)`）
- 传送房间不加入 `allRooms` 列表，防止随机传送死循环（进入 → 传出 → 再进入）
- 传送触发：前端检测玩家坐标在 (35-65, 35-65) 魔法阵范围内 → 发送隐藏命令 `trigger_teleport` → `Game.runCommand()` 捕获 → 调用 `pushHistory()` + `player.setCurrentRoom(getRandomRoom())`
- `getRandomRoom()` 从 `allRooms`（5 个普通房间）中均匀随机选取
- 传送时显示闪烁文本提示："✨ 你踏入了发光的魔法阵，剧烈的空间扭曲将你吞噬..."

---

### 第四阶段：音效系统（6 月 15 日—16 日）

**PR #22 → Revert → PR #24 → 修复（王启赢 & 范奕轩）**

这是项目中唯一一次合并事故，经历了四个步骤：

**`1f51921` feat7_6（王启赢，Resolve #21）— 初版提交**

- BGM 系统：`<audio id="bgm-audio" src="/sounds/bgm.mp3" loop>` 元素，循环播放，音量 0.3。通过 BGM 切换按钮控制播放/暂停
- 拾取音效：`<audio id="sfx-pickup" src="/sounds/pickup.mp3">` 元素，音量 0.6，调用 `sfxPickup.play()` 播放
- 丢弃音效分类系统：`getDropSoundId(itemName)` 函数通过关键词匹配实现按物品材质选择音效——匹配 `['烧杯', '试剂瓶', '酒瓶', '玻璃']` → `drop-glass.mp3`、匹配 `['咖啡杯']` → `drop-cup.mp3`、匹配 `['铁锹', '剑', '显微镜', '铁']` → `drop-metal.mp3`、默认 → `drop.mp3`

**`7538396` Revert（范奕轩）— 回退**

因初版合并后出现兼容性问题（音效文件与 `index.html` 的其他修改冲突），执行了全文 revert。

**`386933d` feat7_6（王启赢）— 重提交**

修复问题后重新提交完整的音效功能。

**`9f687eb` fix（王启赢，6 月 16 日）— 善后修复**

Revert 操作误删了 3 个掉落音效文件（`drop-glass.mp3`、`drop-cup.mp3`、`drop-metal.mp3`）以及 `index.html` 中被连带删除的 DOM 元素和 CSS 样式。手动逐一恢复所有丢失内容。

---

### 第五阶段：战斗系统与最终完善（6 月 16 日—17 日）

**PR #26 — RPG 战斗系统（何宇航）**

**`167e143` feat9_1（何宇航）**

实现完整的 RPG 战斗引擎，包含以下子系统：

*1. 攻击模组：*
- 普通攻击（J 键）：设置 `playerState.attacking = true`，1020ms 后复位。CSS `attack-swing` 关键帧动画在 1s 内依次展示蓄力→挥砍→收刀三个阶段。攻击精灵图缩放 1.6 倍（`SCALE = 1.6`），方向与玩家朝向一致
- 魔法攻击（Q 键）：消耗 10 MP，发射魔法子弹（`Magic_Bullet.png`），沿当前方向以速度 5 飞行。子弹每帧更新位置，撞墙（碰撞矩形检测）或出界（< 2% 或 > 98%）时移除
- 治疗（E 键）：消耗 30 MP 恢复 20 HP，MP 不足时提示

*2. Saber Boss 战：*
- Saber 作为 NPC 角色在地图中渲染（`saber.png` 雪碧图，与玩家使用相同的 4×4 帧精灵系统）
- 玩家靠近 Saber（距离 < 12）时触发战斗对话框（`showBattleDialog`），两个选项：
  - 🔥 **战斗**：`saber.battleMode = true`，启动 AI，重置 Saber 位置至 (50,50)
  - ✋ **取消**：关闭对话框，Saber 恢复待机状态
- Saber AI（`startSaberAI()`，500ms 间隔）：追击（距离 > 8 时以速度 1 向玩家移动）→ 近战（距离 ≤ 8 时停止移动并造成 10 点伤害，播放 Saber 攻击动画）
- Saber 攻击动画：`saber_attack.png` 雪碧图，同样 1.6 倍缩放，`isAttacking` 状态下播放

*3. 魔法技能系统：*
- 魔法子弹飞行渲染：`<div class="magic-bullet">` 元素，32×32px，绝对定位，`translate(-50%, -50%)` 居中
- 子弹数组 `bullets[]` 每帧更新位置并检测碰撞
- 击中 Saber 造成 `BULLET_DAMAGE = 10` 点伤害

*4. 战斗结算：*
- 胜利：Saber HP ≤ 0 且任务物品全收集 → `showVictoryDialog`（绿色边框），通关奖励满血满蓝
- 未完成击杀：Saber HP ≤ 0 但物品不全 → Saber 重生（满血，回到 50,50），提示玩家继续收集
- 战败：玩家 HP ≤ 0 → `showDefeatDialog`（红色边框），选择"重新对战"或"离开对战"

*5. 资源管理：*
- HP/Mana 在战斗外每 3 秒自然恢复 1 点
- 吃魔法饼干恢复 50 HP
- 道具剑提升攻击力 10（从 10 → 20）

**PR #28 — 胜利结算与 UI 汉化（范奕轩）**

**`bbbe9a6` feat7_7（范奕轩，Resolve #27）**

- **胜利结算弹窗**：击败 Saber 并收集齐全部 4 件任务物品后触发。弹窗设计——CSS `battle-dialog` 类扩展，绿色边框（`rgba(76, 255, 76, 0.8)`），半透明黑色背景，圆角 12px。内容："🏆 战斗胜利！" 标题 + "干得漂亮！你成功击败了 Saber！" 副标题 + "返回主菜单" 按钮
- **全局 UI 中文化**：
  - 主菜单：标题"祖尔世界"、占位文本"请输入你的名字"、按钮"开始新游戏"/"读取存档"、存档列表"存档记录"/"没有找到存档"/"读取"
  - 游戏中：面板标题"🎒 物品栏"/"📋 任务清单"/"🎮 操作面板"、按钮"攻击"/"魔法"/"治疗"/"拾取"/"丢弃"/"存档"/"返回主菜单"、状态"生命"/"法力"/"攻击力"
  - 提示信息：拾取标签"拾取"（墨绿色 `#1e561b`）、丢弃标签"丢弃"（殷红色 `#8b1c1c`）、警告标签"警告"（暗金色 `#b8730b`）
  - 系统消息："拾取物品：{name}"、"丢弃物品：{name}"、"附近没有物品"、"MP 不足！"、"道具剑让你攻击力 +10！"
- **自订字体**：引入 `@font-face` 定义 `ActionFont`（映射 `/fonts/MyFont.ttf`，用于标题/标签）和 `BodyFont`（映射 `/fonts/BodyFont.ttf`，用于正文），操作标签使用 `ActionFont` + 3.6rem 特大字号
- **新手引导弹窗**：游戏开始时全屏覆盖弹窗（毛玻璃模糊 `backdrop-filter: blur(8px)`），展示 `/images/游戏指南.png` 图片和中文传说背景故事（关于六重秘境和 Saber 试炼），4 秒后自动淡出（`opacity` 过渡 0.6s）

**PR #29 — 碰撞机制与玩法完善（王启赢，当前分支）**

**`48953a3` feat7_8（王启赢，Resolve #29）**

- **碰撞边界系统**：为 5 个房间（outside/theater/pub/lab/office）分别定义 `ROOM_COLLISIONS` 碰撞矩形数组，transporter 房间无碰撞物。矩形数据（百分比坐标）：
  - **outside**（1 个）：石柱 {64, 68, 2, 2}
  - **theater**（8 个）：两侧墙壁 {10, 30, 15, 10}/{15, 10, 3, 8}、舞台边缘 {42, 42, 38, 2}、演讲台 {61, 30, 1, 1}、四组观众席 {37, 75, 15, 0.2}/{37, 90, 15, 0.2}/{70, 75, 15, 0.2}/{70, 90, 15, 0.2}
  - **pub**（6 个）：吧台 {34, 27, 30, 5}、五个吧凳区域 {12, 85, 2, 2}/{16, 70, 2, 2}/{28, 85, 2, 2}/{71, 86, 2, 2}/{88, 78, 2, 2}
  - **lab**（1 个）：实验台 {20, 58, 14, 14}
  - **office**（1 个）：办公桌 {47, 25, 10, 7}
- **碰撞检测算法**：`checkCollision(px, py, radius, roomId)` — 标准 AABB vs Circle 检测。对每个矩形计算最近点 `closestX = clamp(px, rect.x, rect.x+rect.w)`，同理 `closestY`，然后判断 `(px-closestX)² + (py-closestY)² < radius²`。玩家碰撞半径 `PLAYER_RADIUS = 4.5`
- **分轴滑动响应**：游戏循环中先沿 X 轴移动并检测碰撞（碰撞则回退 X），再沿 Y 轴移动并检测碰撞（碰撞则回退 Y），实现沿障碍物边缘滑动的顺滑体验
- **安全推离**：`pushOutOfCollision(px, py, radius, roomId)` — 传送或回退后调用，检测玩家是否被卡入碰撞区域。若重叠则计算重叠量并沿最短法线方向推出。极端情况（`dist < 0.001`）时默认向上推到矩形上方
- **玩法完善**：任务物品面板实时显示收集进度（已收集 ✓ 绿色 / 未收集 ◻ 灰色），Saber 击败条件与任务完成度绑定（必须集齐 4 件才能通关），完善了"探索→收集→战斗→胜利"的完整游戏循环
- **按键拦截增强**：战斗胜利/战败弹窗显示时拦截所有按键输入，防止弹窗穿透。INPUT 元素聚焦时也拦截按键

---

### 第六阶段：扩展完善（6 月 17 日—18 日）

**PR #31 — 游戏指南 UI 优化与清理（范奕轩）**

**`05616d5` style7_1（范奕轩）**

重构游戏指南弹窗的视觉呈现：
- 移除原有的 `border-radius: 16px` 和 `overflow: hidden`，防止 PNG 透明书本图片边缘被硬性裁剪
- 移除矩形金色发光 `box-shadow`（`0 0 60px rgba(212, 175, 55, 0.3)`），消除"矩形边框"的视觉割裂感
- 改用 CSS `filter: drop-shadow(0 15px 30px rgba(0, 0, 0, 0.8))`——`drop-shadow` 滤镜能够自动识别 PNG 图片的 Alpha 透明通道，沿书本不规则边缘投射出真实的 3D 立体阴影
- 设置 `background: transparent`，实现书本与暗色背景的自然融合

**`e94da31` fix（范奕轩）**

删除误提交的 CLAUDE.md 本地配置文件，将其加入 `.gitignore` 防止再次误提交。

**PR #32 → #33 — 金币系统、酒水商店与任务系统（何宇航）**

**`5a1ab9c` feat（何宇航）**

这是继战斗系统后最大规模的功能扩展，同时引入三大新系统：

*1. 金币系统：*
- 玩家初始金币 30🪙（`playerState.coins = 30`）
- 游戏画面顶部状态栏实时显示金币数：`<span class="badge bg-warning text-dark ms-2">🪙 {{ playerState.coins }}</span>`
- 右上角独立金币面板（黄底黑字半透明 overlay）持续显示
- 金币通过完成任务获得（博士奖励 100🪙），用于在酒吧商店消费

*2. 酒水商店系统：*
- **酒吧老板 NPC**：在校园酒吧（pub）房间渲染女性 NPC 角色（`barmaid.png`，4×4 雪碧图，64×96px 渲染），位于坐标 (50, 25)。使用 `sprite-dir-*` 类实现四方向朝向，玩家靠近（距离 < 10%）时自动 `facePlayer()` 面向玩家
- **商店对话框**：金色边框（`rgba(255, 215, 0, 0.6)`）弹窗，显示"欢迎光临！你想买点什么呢？"和当前金币数。提供两个购买按钮——🍺 麦脉暖酿 20🪙 和 🍯 月花蜜醴 30🪙，以及 👋 离开按钮
- **购买流程**：点击购买按钮 → 前端检查金币是否足够（不足则提示"你只有 X 🪙，需要 Y 🪙"）→ 扣除金币 → `sendCommand('buy WarmveinAle')`/`sendCommand('buy Moonhoney')` → 后端 `CommandBuy.execute()` 在酒吧房间内创建物品对象并加入玩家背包（超重检测）
- **购买后交互**：金币变化在前端即时反应，玩家可在背包中看到新购买的酒水物品及重量（麦脉暖酿 0.5kg / 月花蜜醴 0.4kg）
- **`CommandBuy` 实现**：检查房间必须包含 "pub" 或 "酒吧"，按物品名关键词匹配（"麦脉暖酿"/"麦脉"→麦脉暖酿，"月花蜜醴"/"月花"→月花蜜醴），创建 `Item` 对象并检查 `player.getCurrentWeight() + newItem.getWeight() > player.getMaxWeight()` 防超重

*3. 博士任务系统：*
- **博士 NPC**：在计算机实验室（lab）房间渲染男性 NPC 角色（`doctor.png`，4×4 雪碧图，64×96px 渲染），位于坐标 (50, 50)
- **三阶段任务链**：`questPhase` ref 变量跟踪任务进度
  - **阶段一 `find_doctor`**：玩家靠近博士时对话框显示"年轻人，你来得正好！我需要帮助..."，点击交谈 → 博士布置收集任务"我需要你帮我收集一些任务物品，去探索各个房间找到它们吧！" → 阶段切换至 `collect_items`
  - **阶段二 `collect_items`**：对话框显示"你收集到足够的任务物品了吗？"，任务面板标题变为"📜 任务物品 (X/4)"。玩家在房间中收集任务物品，实时进度追踪。点击交谈 → 博士判断 `checkAllQuestItemsCollected()` ——若未集齐，提示"你还没收集够物品，继续探索吧！"；若集齐，进入下一阶段
  - **阶段三 `completed`**：对话框显示"多谢你帮我找齐素材，这是任务报酬！"，点击交谈 → 博士给予 100 金币奖励（`playerState.coins += 100`），移除背包中所有任务物品（遍历 required 列表逐一 `sendCommand('drop')`），提示"获得 100 🪙！"。此后任务面板标题变为"📜 任务 ✅ 已完成"，博士对话变为"随时可以来找我聊天！"
- NPC 防重复触发：`barmaidAlert`/`doctorAlert` 标志位 + 2 秒冷却期（`setTimeout(() => { alert = false; }, 2000)`），防止对话框反复弹出
- NPC 离开房间时自动关闭对话框并重置 alert 状态（`updateNPCs()` 在每次状态刷新时调用）

*4. 辅助改进：*
- 玩家初始负重上限从 10kg 提升至 20kg（`Game.java` 中 `Player` 构造器和 `startNewGame()` 方法均已更新）
- `CommandEat` 扩展：新增麦脉暖酿饮用逻辑（`player.dropItem(item)` → "HP +20"）和月花蜜醴饮用逻辑（"MP +20"）
- 前端 `canEat` computed 属性扩展：选中物品名包含"麦脉暖酿"/"麦脉"/"月花蜜醴"/"月花"时也识别为可食用
- 前端 `processItemEffect` 扩展：饮用药水时更新 `playerState.hp`/`playerState.mp` 并显示对应系统消息
- 新增 `META-INF/MANIFEST.MF` JAR 清单文件（Main-Class + Class-Path），支持 `java -jar` 直接运行
- `rebuild.bat` 编译脚本优化：`javac` 改为 `-sourcepath` 模式自动编译所有 Java 文件，`jar uf` 改为批量通配符更新

**`be42a98` fix（何宇航）**

修复酒水物品（麦脉暖酿/月花蜜醴）在前端渲染中的实体显示问题，确保物品图标（`WarmveinAle.png`/`Moonhoney-Sippen-Sippen.png`）在所有显示位置（背包列表、快捷栏、道具剑图标）正确加载和呈现。

**PR #33 — 道具快捷栏（何宇航）**

**`68e6f3a` feat9_2（何宇航）**

在游戏画面右下角新增道具快捷栏系统：

*UI 设计：*
- 3 个圆形快捷栏位（52×52px，`border-radius: 50%`），水平排列，间距 12px
- 半透明暗色面板（`rgba(24, 20, 25, 0.8)`，圆角 12px，内边距 6px 10px）包裹
- 每个栏位的边框——未选中：`2px solid rgba(255,255,255,0.3)`（半透明白边），选中：`3px solid #ffd700`（金色高亮边框）
- 栏位背景——有物品：`rgba(255,255,255,0.1)`（微亮），空栏位：`rgba(0,0,0,0.3)`（深暗）
- 物品图标：28×28px，`object-fit: contain`，`@error` 时自动隐藏
- 物品名称标签：9px 字号，灰色（`#ddd`），最大宽 48px，溢出省略号
- 栏位编号：栏位下方显示金色数字 1/2/3（`#ffd700` + 文字阴影 `text-shadow: 0 0 4px rgba(0,0,0,0.8)`）
- 空栏位：显示"—"占位符（16px，半透明白色）

*交互设计：*
- **数字键 `1`/`2`/`3`**：快速选中对应栏位（再次按同一数字键取消选中，按不同数字键切换选中）。选中同时同步 `selectedItemName`，用于后续操作
- **`R` 键**：使用当前选中栏位的物品。检查 `canEat`（可食用/饮用）→ 调用 `processItemEffect()`（前端即时更新 HP/MP/+负重）→ `sendCommand('eat ' + item.name)`（后端同步）→ 清除选中状态
- **`G` 键**（保留原有逻辑）：丢弃当前选中栏位的物品至玩家当前位置，播放对应材质丢弃音效，清除选中状态
- **鼠标点击**：直接点击栏位圆圈进行选中/取消选中切换

*数据同步：*
- `refreshQuickSlots()` 在每次 `fetchState()` 完成后自动调用：按顺序将背包 `state.inventory` 前 3 件物品映射到栏位，超出部分不受影响
- 当选中的栏位物品被消耗/丢弃后（栏位变为 `null`），自动取消选中状态
- 快捷栏与背包面板的状态实时联动——背包物品变化 → `refreshQuickSlots()` → 栏位内容同步更新

---

## 4. 功能特性详解

### 4.1 核心探索玩法

| 功能 | 实现细节 |
|------|----------|
| **2D 图形化世界** | 6 个房间（大学正门外 / 演讲厅 / 校园酒吧 / 计算机实验室 / 行政办公室 / 神秘传送室），每间拥有独立手绘风格全景背景和物理坐标系统 |
| **四方向角色移动** | 方向键 ↑↓←→ 或 WASD 控制，CSS 雪碧图动画（4 行 × 4 列帧网格），行走时 `background-position-x` 按帧循环切换，配合 `translateY` 精确锚定脚底 |
| **房间切换** | 走到房间边缘的"门区域"（距边界 4%，门宽 15%）触发场景切换，切换前显示浮动气泡方向提示 |
| **物品交互** | **拾取**（F 键）：靠近物品（< 10% 距离）时高亮发光，点击拾取进入背包。**丢弃**（G 键）：从背包丢弃至当前位置（附坐标）。**查看**（点击背包面板）：显示物品详情和重量 |
| **负重管理** | 初始最大负重 20kg。拾取时实时计算背包总重量，超重时系统提示"The item is too heavy!"并拒绝拾取。吃魔法饼干可永久提升负重上限至 30kg |
| **魔法传送** | 走入 TransporterRoom 中央魔法阵区域（坐标 35-65 范围），触发随机传送至 5 个普通房间之一。传送房间自身被排除在目标外防止死循环。传送时显示闪现文字特效 |
| **多级回退** | `back` 命令基于 `Stack<Room>` 历史栈实现，支持逐层回退至起点。每次移动前自动将当前房间 `pushHistory()`，回退时 `pop()`。栈空时提示"You can't go back!" |
| **房间侦查** | `look` 命令输出当前房间的完整描述（位置 + 出口方向 + 所有物品及其重量）和背包状态 |

### 4.2 任务系统

| 功能 | 实现细节 |
|------|----------|
| **任务物品分配** | 每局新游戏从 17 种候选物品中随机抽取 4 件作为收集目标（`Collections.shuffle(pool)`） |
| **候选物品池** | 生锈的铁锹、破旧的地图、空鸟巢、鹅卵石堆、道具剑、乐谱架、威士忌酒瓶、飞镖盘、酒吧凳、显微镜、烧杯组、化学试剂瓶、实验记录本、魔法饼干、笔记本电脑、咖啡杯、文件堆 |
| **收集进度面板** | 实时显示 4 件任务物品的收集状态——已收集显示绿色勾号 ✓（`#4cff4c`），未收集显示灰色方块 ◻。标题"📋 任务清单" |
| **通关条件** | 集齐全部 4 件任务物品 + 击败 Saber Boss → 触发胜利结算 |

### 4.3 RPG 战斗系统

| 功能 | 实现细节 | 操作方式 |
|------|----------|----------|
| **战斗触发** | 玩家靠近 Saber（距离 < 12%）时自动弹出战斗对话框 | 自动检测 |
| **普通攻击** | 挥剑近战攻击，伤害 `attackPower`（基础 10，装备道具剑后 20）。判定范围 10%，播放 `attack-swing` 动画（1s） | J 键 |
| **魔法子弹** | 消耗 10 MP 发射魔法弹，沿当前方向飞行，撞 Saber（< 6%）造成 10 伤害。撞墙/出界消失 | Q 键 |
| **治疗** | 消耗 30 MP 恢复 20 HP | E 键 |
| **Saber AI** | 500ms 决策周期：距离 > 8 追击（speed=1），距离 ≤ 8 近战攻击（10 伤害）。移动方向按较大坐标差选择 | — |
| **HP 系统** | 玩家 HP 100/100，Saber HP 200/200。HP ≤ 0 触发战败/胜利。非战斗状态每 3s 回复 1 HP/MP | — |
| **MP 系统** | 玩家 MP 100/100，魔法子弹消耗 10，治疗消耗 30 | — |
| **战败处理** | 红色弹窗：选项"重新对战"（重置满血满蓝）/ "离开对战"（撤退恢复） | 点击按钮 |
| **胜利结算** | 绿色弹窗：通关奖励满血满蓝，可返回主菜单 | 点击按钮 |
| **道具加成** | 拾取道具剑 → `attackPower` 从 10 提升至 20。吃魔法饼干 → HP +50、负重上限 +10kg | 自动生效 |

### 4.4 音效系统

| 功能 | 文件 | 触发条件 |
|------|------|----------|
| **背景音乐** | `bgm.mp3` | 游戏循环播放，音量 0.3，可切换开关 |
| **拾取音效** | `pickup.mp3` | 拾取任意物品时播放，音量 0.6 |
| **丢弃（金属）** | `drop-metal.mp3` | 丢弃铁锹/剑/显微镜/含"铁"物品 |
| **丢弃（玻璃）** | `drop-glass.mp3` | 丢弃烧杯组/试剂瓶/酒瓶/含"玻璃"物品 |
| **丢弃（陶瓷）** | `drop-cup.mp3` | 丢弃咖啡杯 |
| **丢弃（默认）** | `drop.mp3` | 丢弃其他物品 |

### 4.5 碰撞系统

| 功能 | 实现细节 |
|------|----------|
| **碰撞检测** | AABB（轴对齐矩形）vs Circle（圆形玩家，半径 4.5%）。最近点钳位 + 距离平方比较 |
| **分轴响应** | X 轴先行 → 检测碰撞 → 回退 X → Y 轴跟进 → 检测碰撞 → 回退 Y，实现墙壁滑动 |
| **安全推离** | 传送/回退后检测是否卡入碰撞区，沿最短法线推出 |
| **房间边界** | 玩家坐标硬钳制在 [EDGE_MIN=4, EDGE_MAX=96] 范围内 |
| **各房间障碍物** | outside: 1 个石柱、theater: 8 个（墙壁/舞台/演讲台/观众席）、pub: 6 个（吧台/吧凳）、lab: 1 个实验台、office: 1 个办公桌、transporter: 无 |

### 4.6 持久化系统

| 功能 | 实现细节 |
|------|----------|
| **自动存档** | 每次命令执行后自动写入 `【自动存档】` 记录，同名覆盖更新（保留原始创建时间） |
| **手动存档** | 玩家自定义存档名称，通过 `POST /api/save` 保存 |
| **多存档管理** | `GET /api/saves` 返回元数据列表（id/名称/玩家/时间），支持选择加载和删除 |
| **状态完整性** | 序列化全部状态：玩家（名称/位置/背包/最大负重）、所有房间物品及精确坐标、出口拓扑、历史栈、任务物品列表 |
| **兼容性修复** | 旧存档物品坐标 (0,0) 自动通过关键词映射恢复，背包物品 (0,0) 随机分配 |
| **性能优化** | 存档列表查询使用 HQL 投影（`SELECT id, saveName, playerName, createdAt`），避免加载 TEXT 大字段 |

### 4.7 金币与酒水商店系统

| 功能 | 实现细节 |
|------|----------|
| **金币系统** | 玩家初始 30🪙 金币。顶部状态栏（`badge bg-warning`）和右上角独立面板（黄底半透明）实时显示。金币通过博士任务奖励获得，在酒吧商店消费 |
| **酒吧老板 NPC** | 在 pub 房间渲染（`barmaid.png` 雪碧图，64×96px），四方向精灵动画，坐标 (50, 25)。玩家靠近（< 10% 距离）自动面向玩家并弹出商店对话框 |
| **商品 — 麦脉暖酿** | 售价 20🪙，重量 0.5kg。购买后加入背包，饮用（E 键）恢复 20 HP（`playerState.hp += 20`）。图标 `WarmveinAle.png` |
| **商品 — 月花蜜醴** | 售价 30🪙，重量 0.4kg。购买后加入背包，饮用（E 键）恢复 20 MP（`playerState.mp += 20`）。图标 `Moonhoney-Sippen-Sippen.png` |
| **购买检测** | 金币不足时提示"你只有 X 🪙，需要 Y 🪙"。超重时提示"The item is too heavy!"。后端 `CommandBuy` 检查房间必须为 pub，按物品名关键词匹配创建 Item |
| **防重复触发** | `barmaidAlert` 标志位 + 2 秒冷却期，防止对话框反复弹出。离开 pub 房间自动关闭对话框 |

### 4.8 博士任务与 NPC 交互系统

| 功能 | 实现细节 |
|------|----------|
| **博士 NPC** | 在 lab 房间渲染（`doctor.png` 雪碧图，64×96px），四方向精灵动画，坐标 (50, 50)。玩家靠近（< 10% 距离）自动面向玩家并弹出对话对话框 |
| **阶段一 `find_doctor`** | 寻找博士。对话框提示"年轻人，你来得正好！我需要帮助..."，交谈后博士布置收集任务，阶段切换至 `collect_items` |
| **阶段二 `collect_items`** | 收集任务物品。对话框提示"你收集到足够的任务物品了吗？"，任务面板标题显示"📜 任务物品 (X/4)"。交谈时博士检查 `checkAllQuestItemsCollected()`——集齐则进入阶段三，未集齐则鼓励继续 |
| **阶段三 `completed`** | 任务完成。对话框提示"多谢你帮我找齐素材，这是任务报酬！"，交谈后获得 100🪙 金币奖励，背包中所有任务物品被移除，任务面板标题变为"📜 任务 ✅ 已完成" |
| **NPC 朝向** | `facePlayer(npc)` 函数根据玩家与 NPC 的相对坐标计算朝向（dx/dy 较大者决定水平/垂直方向，符号决定 left/right/up/down） |
| **对话样式** | 绿色边框弹窗（`rgba(0, 200, 100, 0.6)`），不同阶段显示不同的对话文本（`v-if="questPhase === 'xxx'"`），提供"💬 交谈"和"👋 离开"按钮 |
| **任务面板联动** | `find_doctor` 阶段隐藏任务面板（`v-if="questPhase !== 'find_doctor'"`），`completed` 阶段显示"✅ 已完成"，`collect_items` 阶段显示收集进度 |

### 4.9 道具快捷栏

| 功能 | 实现细节 |
|------|----------|
| **UI 布局** | 3 个圆形栏位（52×52px），右下角固定位置，半透明暗色面板包裹（`rgba(24,20,25,0.8)`，圆角 12px） |
| **自动填充** | `refreshQuickSlots()` 将背包 `state.inventory` 前 3 件物品按顺序映射到栏位，背包物品变化时自动同步 |
| **选中高亮** | 选中栏位金色边框（`3px solid #ffd700`），未选中半透明白边（`2px solid rgba(255,255,255,0.3)`）。空栏位显示"—"占位符 |
| **物品展示** | 物品图标（28×28px PNG）+ 物品名称标签（9px 灰色，溢出省略号）+ 栏位编号（底部金色数字 1/2/3） |
| **快速选中** | 数字键 `1`/`2`/`3` 快速选中对应栏位。重复按同一数字键取消选中，按不同数字键切换选中 |
| **快速使用** | `R` 键使用选中物品——自动识别可食用/饮用类型（`canEat` computed 属性），前端 `processItemEffect()` 即时更新 HP/MP，后端 `sendCommand('eat')` 同步 |
| **快速丢弃** | `G` 键（在手快捷键模式下）丢弃选中物品至当前位置，播放对应材质丢弃音效 |
| **状态同步** | 物品被消耗/丢弃后自动清除选中状态（`selectedSlot = -1`），`fetchState()` 后同步刷新栏位内容 |

---


## 5. 技术架构详解

### 5.1 系统架构图

```
┌──────────────────────────────────────────────────────────┐
│                    Browser (Frontend)                     │
│  ┌────────────────────────────────────────────────────┐  │
│  │              webapp/index.html (~1660 lines)        │  │
│  │  ┌──────────────────┐  ┌────────────────────────┐  │  │
│  │  │  Vue 3 (reactive │  │  CSS Sprite Engine     │  │  │
│  │  │   + ref/computed)│  │  - 4x4 frame grid      │  │  │
│  │  │                  │  │  - SpriteAnalyzer       │  │  │
│  │  │  Game State      │  │  - attack-swing anim    │  │  │
│  │  │  - playerState   │  │  - 4-dir bg-pos map    │  │  │
│  │  │  - saber         │  └────────────────────────┘  │  │
│  │  │  - state         │  ┌────────────────────────┐  │  │
│  │  │  - barmaid/doctor│  │  Audio System           │  │  │
│  │  └──────────────────┘  │  - BGM (loop, vol 0.3) │  │  │
│  │  ┌──────────────────┐  │  - SFX (pickup/drop)   │  │  │
│  │  │  Collision Engine │  │  - Material matching   │  │  │
│  │  │  - AABB vs Circle │  └────────────────────────┘  │  │
│  │  │  - per-room rects │  ┌────────────────────────┐  │  │
│  │  │  - sliding sep    │  │  Battle Engine          │  │  │
│  │  └──────────────────┘  │  - Saber AI (500ms)     │  │  │
│  │  ┌──────────────────┐  │  - Magic bullets        │  │  │
│  │  │  NPC Interaction │  │  - HP/MP manage         │  │  │
│  │  │  - Proximity det.│  │  - Victory/Defeat UI    │  │  │
│  │  │  - Quest chain   │  └────────────────────────┘  │  │
│  │  │  - Shop dialog   │  ┌────────────────────────┐  │  │
│  │  └──────────────────┘  │  Coin & Item System      │  │  │
│  │  ┌──────────────────┐  │  - 🪙 coin state/mgmt   │  │  │
│  │  │  Render Loop     │  │  - QuickBar (3 slots)   │  │  │
│  │  │  - requestAnim-  │  │  - Buy/Sell logic       │  │  │
│  │  │    ationFrame    │  └────────────────────────┘  │  │
│  │  │  - 60fps gameLoop│                                │  │
│  │  └──────────────────┘                                │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────┬───────────────────────────────────────┘
                   │  HTTP REST (JSON)
┌──────────────────▼───────────────────────────────────────┐
│            GameServer.java  (JDK HttpServer :8000)        │
│  ┌────────────┐ ┌────────────┐ ┌──────────┐ ┌────────┐  │
│  │ StaticFile │ │ /api/      │ │ /api/    │ │ /api/  │  │
│  │ Handler    │ │ command    │ │ state    │ │ save/  │  │
│  │ (/, *.html,│ │ (POST,     │ │ (GET,    │ │ load/  │  │
│  │  *.png,    │ │  text/plain│ │  JSON)   │ │ saves/ │  │
│  │  *.mp3)    │ │  request)  │ │          │ │ newgame│  │
│  └────────────┘ └────────────┘ └──────────┘ │ /exit  │  │
│                                              └────────┘  │
│  autoSave() — triggered after each command execution      │
└──────────────────┬───────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────┐
│                  Game Engine (Core Logic)                  │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Game.java  — 世界创建 / 命令路由 / 状态序列化      │  │
│  │  ┌──────────┬──────────┬──────────┬──────────────┐ │  │
│  │  │ Parser   │ Command  │ Player   │ Room[]       │ │  │
│  │  │          │ Words    │          │              │ │  │
│  │  │ tokenize │ HashMap  │ name     │ Room         │ │  │
│  │  │ ->Command│ register │ current  │ Transporter- │ │  │
│  │  │          │ 9 cmds   │ Room     │ Room         │ │  │
│  │  │          │          │ inventory│ Item[]       │ │  │
│  │  └──────────┴──────────┴──────────┴──────────────┘ │  │
│  │  10 Command Implementations:                        │  │
│  │  Go / Look / Back / Take / Drop / Items / Eat       │  │
│  │  / Buy / Help / Quit                                │  │
│  │  + hidden: trigger_teleport                         │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────┬───────────────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────────────┐
│              Persistence Layer (Hibernate ORM)             │
│  ┌────────────────────────────────────────────────────┐  │
│  │  DatabaseManager.java  — CRUD (7 static methods)   │  │
│  │  ┌──────────────┐  ┌─────────────────────────────┐ │  │
│  │  │ HibernateUtil│  │ SQLiteDialect               │ │  │
│  │  │ (Singleton)  │  │ (type mappings, identity)    │ │  │
│  │  └──────────────┘  └─────────────────────────────┘ │  │
│  │  GameSaveEntity  ↔  game_saves table               │  │
│  │  GameStateDTO    ↔  Gson JSON serialization         │  │
│  └────────────────────────────────────────────────────┘  │
│                      SQLite (zuul_game.db)                 │
└──────────────────────────────────────────────────────────┘
```

### 5.2 技术栈明细

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 后端语言 | Java | 8 | 核心业务逻辑 |
| HTTP 服务器 | `com.sun.net.httpserver` | JDK 内置 | 嵌入式 Web 服务器，无 Spring Boot |
| 构建工具 | Maven | 3.6+ | 依赖管理、编译、打包（含 fat JAR 配置） |
| ORM | Hibernate Core | 5.6.15 | 对象关系映射 |
| 数据库 | SQLite | 3.44.1 | 本地文件数据库（零配置、免安装） |
| JSON | Gson | 2.10.1 | 游戏状态序列化与反序列化 |
| 测试 | JUnit | 4.13.2 | 单元测试 |
| 前端框架 | Vue 3 | CDN latest | 响应式 UI 框架（`createApp`、`reactive`、`ref`、`computed`、`onMounted`） |
| CSS 框架 | Bootstrap 5 | 5.3.0 CDN | UI 组件与栅格布局 |
| 音效 | HTML5 Audio API | — | BGM 循环播放 + SFX 按需触发 |
| 图形 | CSS Sprite + Canvas API | — | 角色动画渲染 + 雪碧图分析 |

### 5.3 数据流（以"拾取物品"为例）

```
1. 用户按下 F 键
   ↓
2. 前端 gameLoop 检测按键 → 遍历 positionedItems，查找距离 < PICKUP_RANGE(10) 的最近物品
   ↓
3. 物品高亮发光（glow-pulse CSS 动画）
   ↓
4. sendCommand('take ' + itemName)
   → fetch POST /api/command, body: "take 道具剑"
   ↓
5. GameServer.CommandHandler:
   - 捕获 System.out → ByteArrayOutputStream
   - game.runCommand("take 道具剑")
   ↓
6. Game.runCommand → Parser.parseCommand → CommandWords.get("take").execute(game, cmd)
   ↓
7. CommandTake.execute():
   - currentRoom.getItem("道具剑") → 找到 Item 对象
   - player.takeItem(item) → 检查 getCurrentWeight() + item.weight <= maxWeight
   - 通过 → player.inventory.add(item), room.removeItem(item)
   - 打印: "You picked up: 道具剑：舞台用的仿古长剑..."
   ↓
8. 返回 GameServer → autoSave() → DatabaseManager.updateSave()
   ↓
9. 返回 HTTP 200，body = 捕获的游戏输出文本
   ↓
10. 前端收到响应 → refreshState() → GET /api/state
    ↓
11. StateHandler 返回 JSON → 更新 state.inventory, state.room.items
    ↓
12. Vue 响应式渲染 → 物品从房间消失，出现在背包面板
    播放 pickup.mp3 拾取音效
    显示系统消息："拾取物品：道具剑"
```

### 5.4 命令模式架构

```
                    ┌────────────────────┐
                    │  CommandExecution   │
                    │  (interface)        │
                    │  +execute(Game,     │
                    │   Command): boolean │
                    └─────────┬───────────┘
                              │ implements
        ┌─────────┬───────────┼───────────┬──────────┬──────────┬───────────┐
        │         │           │           │          │          │           │
   CommandGo CommandLook CommandBack CommandTake CommandDrop CommandBuy ...
        │         │           │           │          │          │
        │         │           │           │          │          │
        ▼         ▼           ▼           ▼          ▼          ▼
    getExit() getLongDesc()  goBack()  takeItem()  dropItem() 在酒吧房间
    pushHistory             Stack.pop 超重检测   坐标设置   创建Item+超重检测
```

---

## 6. 项目结构

```
kai-fa-longingstays/
│
├── pom.xml                          # Maven 项目配置（Java 8, Hibernate, Gson, JUnit）
├── README.md                        # 本文件
├── REPORT.docx                      # 小组实训报告（电子版）
├── .gitignore                       # 忽略 zuul_game.db 等运行时文件
│
├── src/
│   ├── main/java/cn/edu/whut/sept/zuul/
│   │   │
│   │   ├── Game.java                # 【核心】游戏主控类
│   │   │   ├── createRooms()        #   创建 6 房间 + 完整物品布局
│   │   │   ├── initializeCommandMap() # 注册 10 个命令（含 buy）
│   │   │   ├── runCommand(String)   #   Web 入口（含 trigger_teleport）
│   │   │   ├── serializeGameState() #   → GameStateDTO → Gson → JSON
│   │   │   ├── deserializeGameState()#   JSON → 五步重建（房间/出口/物品/玩家/历史）
│   │   │   ├── startNewGame()       #   重置世界 + 随机 4 任务物品
│   │   │   └── pushHistory()/goBack()# 历史栈管理
│   │   │
│   │   ├── GameServer.java          # 【入口】HTTP 服务器（main 方法）
│   │   │   ├── main()               #   启动 :8000，注册 8 个上下文
│   │   │   ├── StaticFileHandler    #   静态文件（HTML/PNG/MP3/TTF）
│   │   │   ├── CommandHandler       #   POST /api/command
│   │   │   ├── StateHandler         #   GET  /api/state (JSON)
│   │   │   ├── SaveHandler          #   POST /api/save
│   │   │   ├── LoadHandler          #   GET  /api/load?saveId=N
│   │   │   ├── SavesListHandler     #   GET  /api/saves
│   │   │   ├── NewGameHandler       #   POST /api/newgame
│   │   │   ├── ExitHandler          #   POST /api/exit
│   │   │   ├── ensureGameInitialized() # 延迟初始化
│   │   │   └── autoSave()           #   每次命令后自动存档
│   │   │
│   │   ├── Player.java              # 玩家模型
│   │   │   ├── name, currentRoom, inventory, maxWeight
│   │   │   ├── takeItem(Item): bool #   超重检测
│   │   │   ├── dropItem(Item)       #   移除物品
│   │   │   ├── getCurrentWeight()   #   迭代求和
│   │   │   ├── getItem(String)      #   按描述包含匹配
│   │   │   └── increaseMaxWeight()  #   吃饼干 +10kg
│   │   │
│   │   ├── Room.java                # 普通房间模型
│   │   │   ├── description, exits (HashMap), items (ArrayList)
│   │   │   ├── setExit/getExit()    #   四方向连接
│   │   │   ├── addItem/removeItem/getItem() # 物品管理
│   │   │   └── getLongDescription() #   位置 + 出口 + 物品清单
│   │   │
│   │   ├── TransporterRoom.java     # 传送房间（extends Room）
│   │   ├── Item.java                # 物品模型
│   │   │   └── description, weight, x, y (百分比坐标)
│   │   │
│   │   ├── Parser.java              # 命令解析器
│   │   │   ├── parseCommand(String) #   Web 端输入解析
│   │   │   └── getCommand()         #   CLI 端输入解析
│   │   │
│   │   ├── Command.java             # 命令封装（commandWord + secondWord）
│   │   ├── CommandWords.java        # 命令注册表（HashMap<String, CommandExecution>）
│   │   ├── CommandExecution.java    # 命令执行接口（boolean execute(Game, Command)）
│   │   │
│   │   ├── CommandGo.java           # go — 移动（含 pushHistory）
│   │   ├── CommandLook.java         # look — 查看房间 + 背包
│   │   ├── CommandBack.java         # back — 多级回退（Stack.pop）
│   │   ├── CommandTake.java         # take — 拾取（超重检测）
│   │   ├── CommandDrop.java         # drop — 丢弃（含坐标参数解析）
│   │   ├── CommandItems.java        # items — 物品统计
│   │   ├── CommandEat.java          # eat — 吃饼干/饮用药水（负重+10/HP恢复/MP恢复）
│   │   ├── CommandBuy.java          # buy — 从酒吧购买酒水
│   │   ├── CommandHelp.java         # help — 命令列表
│   │   ├── CommandQuit.java         # quit — 结束游戏
│   │   │
│   │   ├── Main.java                # 旧 CLI 入口（保留）
│   │   └── package-info.java        # 包信息
│   │
│   │   └── db/                      # 持久化层
│   │       ├── DatabaseManager.java # 存档 CRUD（7 个静态方法）
│   │       ├── GameSaveEntity.java  # JPA 实体（game_saves 表）
│   │       ├── GameStateDTO.java    # JSON 序列化 DTO（含 ItemDTO/RoomDTO 内部类）
│   │       ├── HibernateUtil.java   # SessionFactory 单例（双重检查锁定）
│   │       └── SQLiteDialect.java   # Hibernate SQLite 方言适配
│   │
│   └── test/java/
│       ├── GameHistoryTest.java     # 原有：回退功能测试 (2)
│       ├── PlayerTest.java          # 原有：玩家负重管理测试 (4)
│       ├── RoomTest.java            # 原有：房间物品管理测试 (1)
│       ├── TestHelper.java          # 测试工具：输出捕获
│       │
│       ├── ItemTest.java            # P0: Item 模型 (8)
│       ├── RoomFullTest.java        # P0: Room 完整覆盖 (20)
│       ├── PlayerFullTest.java      # P0: Player 完整覆盖 (23)
│       ├── CommandGoTest.java       # P0: 移动命令 (8)
│       ├── CommandTakeTest.java     # P0: 拾取命令 (6)
│       ├── CommandDropTest.java     # P0: 丢弃命令 (6)
│       ├── GameSerializeTest.java   # P0: 序列化往返 (13)
│       ├── GameNewGameTest.java     # P0: 新游戏流程 (10)
│       │
│       ├── CommandEatTest.java      # P1: 食用/饮用 (9)
│       ├── CommandBuyTest.java      # P1: 酒水购买 (10)
│       ├── GameWorldTest.java       # P1: 世界创建 (8)
│       ├── GameTeleportTest.java    # P1: 传送系统 (7)
│       ├── GameQuestTest.java       # P1: 任务系统 (7)
│       ├── GameRunCommandTest.java  # P1: runCommand 综合 (11)
│       │
│       ├── CommandLookTest.java     # P2: 侦查命令 (7)
│       ├── CommandBackTest.java     # P2: 回退命令 (6)
│       ├── CommandItemsTest.java    # P2: 物品统计 (6)
│       ├── CommandHelpTest.java     # P2: 帮助命令 (3)
│       ├── CommandQuitTest.java     # P2: 退出命令 (5)
│       ├── ParserTest.java          # P2: 解析器 (9)
│       ├── CommandWordsTest.java    # P2: 命令注册表 (10)
│       │
│       ├── GameE2ETest.java         # P3: 端到端流程 (9)
│       ├── GameServerTest.java      # P3: HTTP 端点 (7)
│       │
│       └── db/
│           ├── GameStateDTOTest.java   # P2: DTO 序列化 (14)
│           ├── DatabaseManagerTest.java # P3: 数据库 CRUD (16)
│           └── HibernateUtilTest.java   # P3: SessionFactory (4)
│
├── webapp/                          # 前端静态资源
│   ├── index.html                   # 单页应用（~1,400 行）
│   │   ├── HTML: 主菜单 / 游戏画面 / 面板 / 弹窗 / 引导
│   │   ├── CSS: 动画 / 布局 / 雪碧图 / 音效按钮 ~300 行
│   │   └── JS (Vue 3 setup):
│   │       ├── 状态管理（state, playerState, saber, barmaid, doctor）
│   │       ├── 渲染循环（gameLoop, requestAnimationFrame）
│   │       ├── 碰撞系统（ROOM_COLLISIONS, checkCollision）
│   │       ├── 战斗系统（Saber AI, 魔法子弹, HP/MP）
│   │       ├── NPC 交互（接近检测, 对话弹窗, 任务链, 商店）
│   │       ├── 物品交互（拾取/丢弃/进食/购买/道具效果）
│   │       ├── 快捷栏系统（3 槽位, 数字键选中, R 键使用）
│   │       ├── 金币系统（coins 状态, 购买检测, 任务奖励）
│   │       ├── 音效管理（BGM, SFX, 材质匹配）
│   │       ├── 存档 UI（列表/保存/读取/删除）
│   │       └── 系统消息（翻译/显示/隐藏）
│   │
│   ├── images/                      # 游戏图像资源
│   │   ├── bg-*.png                 # 6 张房间全景背景
│   │   ├── player.png               # 玩家 4×4 雪碧图
│   │   ├── attack.PNG               # 攻击动画 4×4 雪碧图
│   │   ├── saber.png                # Saber NPC 4×4 雪碧图
│   │   ├── saber_attack.PNG         # Saber 攻击动画雪碧图
│   │   ├── barmaid.png              # 酒吧老板 NPC 4×4 雪碧图
│   │   ├── doctor.png               # 博士 NPC 4×4 雪碧图
│   │   ├── Magic_Bullet.png         # 魔法子弹
│   │   ├── WarmveinAle.png / Moonhoney-Sippen-Sippen.png  # 酒水商品图标
│   │   ├── door-h.png / door-v.png  # 门图标（水平/垂直）
│   │   ├── parchment.png            # 系统消息背景（羊皮纸纹理）
│   │   ├── 游戏指南.png             # 新手引导图
│   │   └── *.png                    # 21 个物品图标
│   │
│   ├── sounds/                      # 音效资源
│   │   ├── bgm.mp3                  # 背景音乐
│   │   ├── pickup.mp3               # 拾取音效
│   │   ├── drop.mp3                 # 丢弃（默认）
│   │   ├── drop-metal.mp3           # 丢弃（金属）
│   │   ├── drop-glass.mp3           # 丢弃（玻璃）
│   │   └── drop-cup.mp3             # 丢弃（陶瓷）
│   │
│   └── fonts/                       # 自订字体
│       ├── MyFont.TTF               # 操作标签/标题字体
│       └── BodyFont.ttf             # 正文字体
│
└── .github/                         # GitHub 配置
    └── workflows/                   # CI/CD（可选）
```

---

## 7. 构建、运行与操作指南

### 7.1 环境要求

- **JDK 8** 或更高版本
- **Maven 3.6** 或更高版本
- **现代浏览器**：Chrome 90+ / Edge 90+ / Firefox 88+
- 无需安装数据库（SQLite 为嵌入式文件数据库）

### 7.2 编译与打包

```bash
# 克隆项目
git clone <repo-url>
cd kai-fa-longingstays

# 编译（跳过测试）
mvn compile

# 运行测试
mvn test

# 运行指定测试类
mvn -Dtest=PlayerTest test
mvn -Dtest=RoomTest test
mvn -Dtest=GameHistoryTest test

# 打包（含依赖复制 + MANIFEST 主类配置）
mvn package
# 输出: target/zuul-1.0-SNAPSHOT.jar
# 依赖目录: target/lib/
```

### 7.3 启动游戏

```bash
# 方式一：Maven 直接运行
mvn exec:java -Dexec.mainClass="cn.edu.whut.sept.zuul.GameServer"

# 方式二：打包后运行
java -jar target/zuul-1.0-SNAPSHOT.jar
```

启动后控制台输出 `Web Server started on http://localhost:8000`。

打开浏览器访问 **http://localhost:8000**。

### 7.4 游戏操作指南

#### 键盘操作

| 按键 | 功能 | 说明 |
|------|------|------|
| ↑ ↓ ← → | 移动 | 四方向行走，支持连按 |
| W A S D | 移动（备用） | 同方向键 |
| F | 拾取 | 拾取最近物品（需在 10% 范围内） |
| G | 丢弃 | 丢弃背包中选中的物品（或快捷栏选中物品）至当前位置 |
| J | 攻击 | 近战挥剑（战斗模式下靠近 Saber） |
| Q | 魔法 | 消耗 10 MP 发射魔法子弹 |
| E | 治疗/使用 | 消耗 30 MP 恢复 20 HP；或饮用酒水类物品 |
| R | 使用快捷物品 | 使用当前快捷栏选中的物品（自动识别食用/饮用） |
| 1 / 2 / 3 | 快捷栏选择 | 选中对应快捷栏位（金色高亮），重复按取消选中 |
| B | 切换背包 | 打开/关闭物品背包面板 |
| Tab | 切换操作面板 | 打开/关闭操作按钮面板 |

#### 鼠标操作

| 操作 | 说明 |
|------|------|
| 点击物品 → 拾取按钮 | 拾取房间内物品 |
| 点击背包物品 → 丢弃按钮 | 丢弃选中物品 |
| 点击攻击/魔法/治疗按钮 | 战斗操作（移动端支持） |
| 主菜单按钮 | 新游戏、读档、存档、返回主菜单 |

#### 文字命令（兼容旧版）

| 命令 | 用法 | 示例 |
|------|------|------|
| `go <方向>` | 向指定方向移动 | `go east` |
| `look` | 查看当前房间详情 | `look` |
| `back` | 回退至上一个房间 | `back` |
| `take <物品>` | 拾取指定物品 | `take 道具剑` |
| `drop <物品>` | 丢弃指定物品 | `drop 道具剑` |
| `items` | 查看房间和背包物品 | `items` |
| `eat <物品>` | 食用物品 | `eat 魔法饼干` |
| `buy <物品>` | 从酒吧购买酒水 | `buy WarmveinAle` |
| `help` | 显示命令帮助 | `help` |
| `quit` | 退出游戏 | `quit` |

### 7.5 游戏目标与流程

1. **开始新游戏**：输入角色名 → 进入大学正门外（outside），初始 30🪙 金币、20kg 负重上限
2. **探索世界**：在 6 个房间中探索，寻找任务物品（共需收集 4 件）
3. **拜访酒吧**：从校园酒吧（pub）向酒吧老板购买麦脉暖酿（20🪙，HP+20）和月花蜜醴（30🪙，MP+20），补充战斗资源
4. **寻找博士**：前往计算机实验室（lab），找到博士 NPC 并交谈，触发收集任务（阶段一 → 阶段二）
5. **利用传送**：从校园酒吧（pub）西门进入神秘传送室，踏入魔法阵随机传送至其他房间
6. **使用快捷栏**：收集到的消耗品自动出现在右下角快捷栏，按 1/2/3 快速选中，按 R 键快速使用
7. **寻找魔法饼干**：位于计算机实验室（lab），吃掉可恢复 50 HP 并提升负重至 20kg
8. **收集任务物品**：注意查看任务清单面板，追踪 4 件目标物品的收集进度
9. **交付任务**：集齐 4 件任务物品后返回实验室找博士交谈，获得 100🪙 奖励（阶段二 → 阶段三）
10. **挑战 Saber**：找到 Saber → 靠近触发战斗 → 使用攻击（J）、魔法子弹（Q）和饮用酒水/治疗（E/R）的组合击败她
11. **胜利条件**：集齐 4 件任务物品 + 击败 Saber → 胜利结算 → 通关！

---

## 8. 需求完成情况

### 8.1 课设需求对照

| 编号 | 需求 | 状态 | 实现说明 |
|------|------|------|----------|
| 1 | 房间可存放任意数量物件，look 查看 | ✅ | Room.items 为 ArrayList，支持动态增删。look 输出位置+出口+物品清单（含重量） |
| 2 | back 命令，返回上一个房间 | ✅ | `CommandBack` + `Game.pushHistory()`/`goBack()` |
| 3 | 高级 back，逐层回退至起点 | ✅ | 基于 `Stack<Room>` 历史栈，支持多级回退 |
| 4 | 传输房间，随机传送 | ✅ | `TransporterRoom` + 魔法阵区域 + `getRandomRoom()` |
| 5a | Player 类（姓名、当前位置） | ✅ | `Player.java`，字段：name、currentRoom、inventory、maxWeight |
| 5b | 随身携带物品，总重量有上限 | ✅ | `takeItem()` 超重检测，初始 20kg，吃饼干升至 30kg |
| 5c | take / drop 命令，超重提示 | ✅ | `CommandTake`（超重→保留物品+提示），`CommandDrop`（含坐标参数） |
| 5d | items 命令，打印物品和重量 | ✅ | `CommandItems` 输出房间物品+重量和背包物品+重量 |
| 5e | magic cookie，增加负重 | ✅ | `CommandEat` → `increaseMaxWeight(10.0)` + `dropItem` |
| 6 | 网络多人游戏 | ⬜ | 未实现。本项目定位为单人 RPG 冒险游戏 |
| 7 | 图形化用户界面 | ✅ | Vue 3 + Bootstrap 5 2D RPG 图形界面，雪碧图动画，全屏背景 |
| 8 | 数据库保存游戏状态 | ✅ | Hibernate 5.6 + SQLite，手动存档 + 自动存档 + 多存档管理 |
| 9+ | RPG 战斗系统 | ✅ | 攻击模组 + Saber Boss 战 + 魔法技能 + HP/MP 双资源 |
| 9+ | 音效系统 | ✅ | BGM + 按材质分类的拾取/丢弃音效 |
| 9+ | 碰撞系统 | ✅ | AABB vs Circle + 分轴滑动 + 安全推离 + 各房间独立碰撞矩形 |
| 9+ | 任务系统 | ✅ | 17 种候选物品中随机 4 件 + 实时进度追踪 |
| 9+ | UI 中文化 | ✅ | 全局界面汉化 + 自订中文字体 + 新手引导传说背景 |

### 8.2 测试体系总览

本项目建立了完整的四层测试金字塔，覆盖全部 30 个 Java 源文件中的 27 个（91% 文件覆盖率）。

| 层级 | 批次 | 测试文件数 | 测试方法数 | 覆盖范围 |
|------|------|-----------|-----------|----------|
| **单元测试** | P0 | 8 | 94 | 领域模型（Item/Room/Player）+ 核心命令（Go/Take/Drop）+ 序列化 + 新游戏 |
| **功能测试** | P1 | 6 | 52 | 扩展命令（Eat/Buy）+ 世界创建 + 传送系统 + 任务系统 + RunCommand |
| **边界测试** | P2 | 8 | 60 | 剩余命令（Look/Back/Items/Help/Quit）+ Parser + 注册表 + DTO |
| **集成测试** | P3 | 4 | 36 | 数据库 CRUD + Hibernate 会话 + HTTP 端点 + 端到端流程 |
| **原有测试** | — | 3 | 7 | Player/Room/GameHistory 基础验证 |
| **合计** | | **29** | **249** | **BUILD SUCCESS，零失败** |

#### 测试环境

| 项目 | 配置 |
|------|------|
| 测试框架 | JUnit 4.13.2 |
| 构建工具 | Maven 3.6+（Surefire 3.5.4 插件） |
| 数据库 | SQLite 3.44（Hibernate 5.6.15，测试中自动创建 `zuul_game.db`） |
| 输出捕获 | 自定义 `TestHelper.captureOutput()`（System.out 重定向至 ByteArrayOutputStream） |
| 测试运行时间 | ~5 秒（全量 249 个测试） |

#### 测试辅助工具

**`TestHelper.java`** — 输出捕获工具类：

```java
public class TestHelper {
    public static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try { action.run(); }
        finally { System.setOut(originalOut); }
        return baos.toString();
    }
}
```

所有测试通过 `game.runCommand(String)` 模拟前端命令调用，通过 `TestHelper.captureOutput()` 捕获 System.out 输出进行断言验证。

---

### 8.3 P0 批次：领域模型与核心命令（94 个测试）

#### ItemTest.java（8 个测试）

覆盖 `Item` 模型类的完整行为：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testConstructor` | 构造器正确设置 description / weight / x / y |
| `testConstructorWithoutCoords` | 无坐标构造器（x/y 为 0） |
| `testSetters` | setX / setY 方法正确更新坐标 |
| `testZeroWeight` | 零重量物品允许创建 |
| `testNegativeWeight` | 负重量物品允许创建（记录为已知缺陷：无校验） |
| `testNegativeCoords` | 负坐标物品允许创建（记录为已知缺陷：前端碰撞可能失效） |
| `testExtremeCoords` | 极端大值坐标（999999）：无坐标钳制 |
| `testRandomCoordsDiffer` | 同物品多次构造的随机坐标不同 |

#### RoomFullTest.java（20 个测试）

覆盖 `Room` 类的全部方法：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testRawDescription` | getRawDescription() 返回原始描述 |
| `testShortDescription` | getShortDescription() 含 "You are" + Exits |
| `testSetExitAndGetExit` | 设置并获取出口（east → neighbor） |
| `testGetExitNull` | 未设置出口返回 null |
| `testMultipleExits` | 设置 north/south/east/west 四个出口 |
| `testGetExitString` | 出口字符串拼接正确 |
| `testOverwriteExit` | 覆盖同方向出口 |
| `testAddAndGetItem` | 添加物品后 getItem 可查找到 |
| `testGetItemPartialMatch` | 子串匹配："麦脉" 匹配 "麦脉暖酿" |
| `testGetItemFirstMatch` | 多物品时返回第一个 contains() 匹配项 |
| `testGetItemNotFound` | 查找不存在的物品返回 null |
| `testRemoveItem` | 移除物品后 getItem 返回 null |
| `testGetTotalItemWeight` | 物品总重量正确求和 |
| `testGetTotalWeightEmpty` | 空房间总重量为 0.0 |
| `testEmptyRoom` | 空房间不抛异常 |
| `testGetLongDescription` | 长描述含房间描述 + 出口 + 物品清单 |
| `testGetItems` | getItems() 返回物品列表 |
| `testGetItemsEmpty` | 空房间 getItems() 返回空列表 |
| `testLongDescriptionNoItems` | 无物品时显示 "No items here." |
| `testMultipleItemsInRoom` | 添加多个含相同关键词物品场景 |

#### PlayerFullTest.java（23 个测试）

覆盖 `Player` 类的全部方法，包含边界条件和可变引用暴露：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testConstructor` | name / currentRoom / maxWeight / inventory 正确初始化 |
| `testConstructorNullName` | 允许 null 姓名 |
| `testTakeItemSuccess` | 拾取物品成功，currentWeight 正确增加 |
| `testTakeItemOverweight` | 超重拾取返回 false，物品不进背包 |
| `testTakeItemExactLimit` | 刚好达到上限：takeItem 返回 true |
| `testTakeItemJustOver` | 刚好超过上限 0.1kg：返回 false |
| `testTakeItemSequential` | 多次拾取物品的重量累积计算 |
| `testDropItem` | 丢弃物品成功，currentWeight 正确减少 |
| `testDropItemNotInInventory` | 丢弃不存在的物品：静默无操作 |
| `testDropItemOneOfMultiple` | 多物品时丢弃其中一个不影响其他 |
| `testGetItemPartialMatch` | 子串匹配："麦脉" → "麦脉暖酿" |
| `testGetItemFirstMatch` | 多物品含相同关键词时返回第一个匹配 |
| `testGetItemNotFound` | 查找不存在的物品返回 null |
| `testGetItemEmptyInventory` | 空背包 getItem 返回 null |
| `testGetCurrentWeightEmpty` | 空背包 currentWeight = 0.0 |
| `testGetCurrentWeightSum` | 多物品重量累加正确 |
| `testIncreaseMaxWeight` | 增加负重上限 10 → maxWeight = 30 |
| `testIncreaseMaxWeightMultipleTimes` | 连续 5 次增加 → maxWeight = 70（记录：无上限） |
| `testSetCurrentRoom` | 切换当前房间生效 |
| `testGetInventoryString` | 背包字符串含物品描述和重量 |
| `testGetInventoryReturnsMutableReference` | `getInventory()` 返回内部可变 ArrayList，外部可直接修改绕过重量检查（记录为已知缺陷） |
| `testGetName` | getName() 返回构造时设定的名称 |
| `testGetMaxWeight` | getMaxWeight() 返回正确的初始值 |

#### CommandGoTest.java（8 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testGoEast` | outside → theater 移动成功 |
| `testGoNorthNoDoor` | north 无出口 → 输出含 "no door" |
| `testGoWest` | outside → pub 移动成功 |
| `testGoSouth` | outside → lab 移动成功 |
| `testGoWithoutDirection` | go 无参数 → 输出含 "Go where?" |
| `testGoInvalidDirection` | go xyz → 输出含 "no door" |
| `testGoMultipleSteps` | outside → theater → outside 连续移动 |
| `testGoPushesHistory` | 移动后 history 栈包含前一个房间 |

#### CommandTakeTest.java（6 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testTakeExistingItem` | 拾取房间内存在的物品 → 背包含该物品，房间移除 |
| `testTakeNonExistent` | 拾取不存在的物品 → 输出含 "no item" |
| `testTakeWithoutSecondWord` | take 无参数 → 输出含 "Take what?" |
| `testTakeOverweight` | 超重拾取 → 输出含 "heavy"/"carry"，物品保留在房间 |
| `testTakeMultipleItems` | 多次拾取 → 背包大小正确增加 |
| `testTakeWeightIncreases` | 拾取后 player.getCurrentWeight() 正确增加 |

#### CommandDropTest.java（6 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testDropExistingItem` | 丢弃背包物品 → 物品出现在房间，背包移除 |
| `testDropWithCoords` | 丢弃带坐标参数 → 物品在房间的坐标正确 |
| `testDropWithoutSecondWord` | drop 无参数 → 输出含 "Drop what?" |
| `testDropNonExistent` | 丢弃不持有的物品 → 输出含 "don't have" |
| `testDropReducesWeight` | 丢弃后 player.getCurrentWeight() 减少 |
| `testDropLastItemClearsInventory` | 丢弃最后一件物品后背包为空 |

#### GameSerializeTest.java（13 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testSerializeReturnsNonEmptyJson` | 序列化返回非空 JSON，含 playerName 和房间信息 |
| `testRoundtripSimpleState` | 初始状态序列化 → 新 Game 反序列化 → 关键字段一致 |
| `testRoundtripWithInventory` | 背包物品经序列化往返后完整恢复 |
| `testRoundtripWithHistory` | 移动历史栈经序列化往返后 goBack 可用 |
| `testRoundtripRoomExitsPreserved` | 房间出口拓扑经往返后完整保留 |
| `testRoundtripRoomItemsPreserved` | 房间物品（含坐标）经往返后正确恢复 |
| `testDeserializeZeroZeroCoordsFixed` | 旧存档 (0,0) 坐标被关键词映射修复为非零 |
| `testDeserializeEmptyInventory` | 空背包 JSON 反序列化不抛异常 |
| `testDeserializeMissingFields` | 缺失 requiredItems/historyRoomIds 的 JSON 兼容处理 |
| `testDeserializeCorruptedJson` | 损坏 JSON 反序列化的异常处理行为 |
| `testDeserializeUnknownRoomId` | currentRoomId 指向不存在的房间 → 回退到 outside |
| `testSerializeAfterDeserialize` | 序列化 → 反序列化 → 再序列化的完整往返链 |
| `testTransporterRoomNotInAllRooms` | TransporterRoom 存在于 roomIdMap 中且类型正确 |

#### GameNewGameTest.java（10 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testStartNewGameResetsAll` | 新游戏重置玩家名/位置/负重/运行状态/任务物品 |
| `testStartNewGameRandomQuestItems` | 两次开局任务物品大概率不同（三次重试排除极小概率） |
| `testStartNewGameWithNullName` | null 姓名：getName() 返回 null |
| `testStartNewGameWithEmptyName` | 空姓名：getName() 返回 "" |
| `testStartNewGameClearsInventory` | 旧背包物品在新游戏后被清空 |
| `testStartNewGameCanStartAfterExit` | exitToMenu() 后 startNewGame 可重新开始 |
| `testQuitCommandReturnsTrue` | quit 命令返回 true（供 CLI play() 循环使用） |
| `testStartNewGameMultipleTimes` | 连续三次新游戏：状态始终正确 |
| `testRequiredItemsAreUnique` | 4 件任务物品无重复 |
| `testStartNewGamePlayerInOutside` | 新游戏玩家起始位置为 outside |

---

### 8.4 P1 批次：扩展功能测试（52 个测试）

#### CommandEatTest.java（9 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testEatMagicCookie` | 吃魔法饼干 → maxWeight 增加 10.0，饼干从背包移除 |
| `testEatCookieTwice` | 连续吃两个饼干 → maxWeight 累计增加 20.0 |
| `testDrinkWarmveinAle` | 饮用麦脉暖酿 → 输出含 "HP +20"，物品消耗 |
| `testDrinkMoonhoney` | 饮用月花蜜醴 → 输出含 "MP +20"，物品消耗 |
| `testEatNonEdibleItem` | 吃不可食用物品（道具剑）→ 输出含 "can't eat"，物品保留 |
| `testEatWithoutSecondWord` | eat 无参数 → 输出含 "Eat what?" |
| `testEatNotOwned` | 吃不持有的物品 → 输出含 "don't have" |
| `testEatPartialNameMatch` | "eat 麦脉" 匹配 "麦脉暖酿" → 成功饮用 |
| `testEatPartialNameMatchMoonhoney` | "eat 月花" 匹配 "月花蜜醴" → 成功饮用 |

#### CommandBuyTest.java（10 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testBuyWarmveinAleInPub` | 在 pub 购买麦脉暖酿 → 物品进背包，名称含"麦脉暖酿" |
| `testBuyMoonhoneyInPub` | 在 pub 购买月花蜜醴 → 物品进背包，名称含"月花蜜醴" |
| `testBuyWithChinesePartialName` | "buy 麦脉" 购买成功 |
| `testBuyWarmveinAleWeight` | 购买后当前重量 = 0.5kg |
| `testBuyMoonhoneyWeight` | 购买后当前重量 = 0.4kg |
| `testBuyOutsidePub` | 非 pub 购买 → 输出含 "only buy drinks in the pub" |
| `testBuyInvalidDrink` | 购买不存在的商品 → 输出含 "doesn't sell" |
| `testBuyWithoutSecondWord` | buy 无参数 → 输出含 "Buy what?" |
| `testBuyOverweight` | 背包接近满载时购买 → 输出含 "too heavy" 或 "can't carry" |
| `testBuyMultipleSame` | 重复购买同一商品 → 背包含两个相同物品 |

#### GameWorldTest.java（8 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testAllSixRoomsCreated` | roomIdMap 含 6 个房间（outside/theater/pub/lab/office/transporter） |
| `testExitsOutside` | outside 出口：east / south / west 存在，north 不存在 |
| `testExitsTheater` | theater.west = outside |
| `testExitsPub` | pub.east = outside，pub.west = transporter |
| `testTransporterExit` | transporter.east = pub |
| `testRoomsHaveItems` | 除 transporter 外每个房间至少有 1 件物品 |
| `testTransporterNotInAllRooms` | getRandomRoom() 50 次迭代不返回 TransporterRoom |
| `testPlayerInitialState` | 初始 maxWeight = 20.0，start room 为 outside |

#### GameTeleportTest.java（7 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testTransporterRoomExists` | transporter 房间存在且为 TransporterRoom 实例 |
| `testTransporterNotInAllRooms` | 100 次 getRandomRoom() 不返回 TransporterRoom |
| `testTriggerTeleportInTransporterRoom` | 在传送房间内触发 teleport → 不在传送房间 |
| `testTriggerTeleportPushesHistory` | 传送后 goBack() 回到传送房间 |
| `testTriggerTeleportInNormalRoom` | 普通房间内触发 teleport → 位置不变 |
| `testGetRandomRoomAlwaysInAllRooms` | 100 次 getRandomRoom() 结果都在 roomIdMap 中 |
| `testGetRandomRoomDiversity` | 100 次至少返回 2 个不同房间 |

#### GameQuestTest.java（7 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testRequiredItemsCount` | startNewGame 后 requiredItems.size() = 4 |
| `testRequiredItemsUnique` | 4 件任务物品无重复 |
| `testRequiredItemsVaryAcrossGames` | 三次开局任务物品不完全相同（概率验证） |
| `testMultipleStartNewGameNoException` | 20 次连续新游戏不抛异常 |
| `testRequiredItemsFromPool` | 每件任务物品存在于 17 种候选池中 |
| `testRequiredItemsExposesInternalReference` | getRequiredItems() 返回内部可变引用，外部 clear() 影响内部状态 |
| `testStartNewGameResetsRequiredItems` | 新游戏生成新的任务物品列表（非同一对象引用） |

#### GameRunCommandTest.java（11 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testRunCommandGoEast` | runCommand("go east") 正确切换房间 |
| `testRunCommandLook` | runCommand("look") 输出含房间信息 |
| `testRunCommandUnknown` | 未知命令输出含 "don't know" |
| `testRunCommandEmptyString` | 空字符串输出含 "don't know" |
| `testRunCommandNull` | null 输入抛出 NullPointerException（已知缺陷 #2） |
| `testRunCommandTriggerTeleportInNormalRoom` | 普通房间内 trigger_teleport 不改变位置 |
| `testRunCommandQuitOutput` | quit 命令不抛异常 |
| `testRunCommandAfterQuitStillWorks` | quit 后仍可执行 look 命令（已知设计差异） |
| `testRunCommandSequence` | 连续多步命令（east → west → south）正确执行 |
| `testRunCommandGoWithoutDirection` | go 无方向输出含 "Go where?" |
| `testRunCommandExtraSpaces` | 前后多余空格不影响解析 |

---

### 8.5 P2 批次：边界条件与解析器测试（60 个测试）

#### CommandLookTest.java（7 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testLookShowsRoomDescription` | look 输出含房间描述 |
| `testLookShowsExits` | look 输出含 "Exits" 和三个方向 |
| `testLookShowsRoomItems` | look 输出含房间内物品信息 |
| `testLookShowsInventory` | 拾取物品后 look 输出含背包物品 |
| `testLookWithExtraParams` | "look around" 仍然正常执行 |
| `testLookEmptyInventory` | 空背包时 look 输出含 "not carrying anything" |
| `testLookInDifferentRoom` | 切换房间后 look 显示新房间描述 |

#### CommandBackTest.java（6 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testBackAfterMove` | 移动后 back 回到前一个房间 |
| `testBackAtStart` | 起点 back 输出 "can't go back" |
| `testBackMultipleSteps` | 三步移动后三次 goBack 回到起点 |
| `testBackWithSecondWord` | "back there" 输出 "Back where?" |
| `testBackAfterTeleport` | 传送后回退到传送房间 |
| `testBackEmptyStackAfterMultipleBacks` | 回退至栈空后再 back 提示 "can't go back" |

#### CommandItemsTest.java（6 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testItemsShowsRoomSection` | items 输出含 "Room Items" 和房间物品 |
| `testItemsShowsRoomTotalWeight` | items 输出含 "Total weight in room" |
| `testItemsShowsInventorySection` | items 输出含 "Player Inventory" |
| `testItemsEmptyInventory` | 空背包时 items 输出含 "not carrying anything" |
| `testItemsWithInventory` | 有物品时 items 输出含物品名称和总重量 |
| `testItemsShowsWeightStats` | items 输出含 kg 或 weight 统计信息 |

#### CommandHelpTest.java（3 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testHelpShowsAllCommands` | help 输出含全部 10 个命令名 |
| `testHelpOutputIsConsistent` | 两次 help 输出完全一致 |
| `testHelpShowsIntroduction` | help 输出含 "lost"/"alone" 引导文字 |

#### CommandQuitTest.java（5 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testQuitWithSecondWord` | "quit now" 输出 "Quit what?" |
| `testQuitWithoutSecondWord` | quit 命令不抛异常 |
| `testExitToMenuChangesRunningState` | exitToMenu() 将 isRunning 设置为 false |
| `testQuitCommandReturnsTrue` | CommandQuit.execute() 返回 true（供 CLI 循环使用） |
| `testQuitDoesNotExitToMenu` | quit 命令不调用 exitToMenu()（已知设计差异） |

#### ParserTest.java（9 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testParseCommandTwoWords` | "go east" 正确解析为 commandWord="go"、secondWord="east" |
| `testParseCommandOneWord` | "look" 解析为 commandWord="look"、secondWord=null |
| `testParseCommandEmpty` | 空字符串解析为未知命令 |
| `testParseCommandUnknown` | 未知命令词解析为 commandWord=null |
| `testParseCommandNull` | null 输入抛出 NullPointerException |
| `testParseCommandThreeTokens` | "drop 道具剑 30 70" 剩余部分拼接至 secondWord |
| `testParseCommandExtraSpaces` | 前后多余空格正确跳过 |
| `testParseCommandCaseSensitive` | 命令词区分大小写（"GO" 视为未知命令） |
| `testShowCommandsOutput` | showCommands() 输出含 "go" 和 "quit" |

#### CommandWordsTest.java（10 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testNewCommandWordsIsEmpty` | 新建 CommandWords 不含任何命令 |
| `testRegisterAndGet` | 注册后 get 返回同一个 CommandExecution 对象 |
| `testIsCommand` | 注册前 isCommand 返回 false，注册后返回 true |
| `testIsCommandCaseSensitive` | "GO" 和 "Go" 不匹配注册的 "go" |
| `testRegisterMultipleCommands` | 注册多个命令后各自独立查询 |
| `testGetUnknownReturnsNull` | 查询未注册命令返回 null |
| `testOverwriteCommand` | 重复注册同一命令词→ get 返回后者 |
| `testShowAllOutput` | showAll 输出含已注册命令名 |
| `testShowAllEmpty` | 空表 showAll 不抛异常 |
| `testAllRegisteredCommandsCount` | 模拟 Game 中完整注册的 10 个命令 |

#### GameStateDTOTest.java（14 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testGsonRoundtrip` | GameStateDTO JSON 序列化/反序列化往返 |
| `testDefaultConstructor` | 默认构造器初始化所有集合为非 null 空集合 |
| `testItemDTOSerialization` | ItemDTO 序列化含所有字段，反序列化数值精度正确 |
| `testItemDTODefaultConstructor` | ItemDTO 默认构造器各字段初始值 |
| `testItemDTOSetters` | ItemDTO setter 方法正确更新各字段 |
| `testRoomDTOSerialization` | RoomDTO 序列化含 id/description/exits/isTransporter |
| `testRoomDTOIsTransporter` | isTransporter=true 经 JSON 往返后保持 |
| `testRoomDTODefaultConstructor` | RoomDTO 默认构造器初始化 exits 和 items 为空集合 |
| `testRoomDTOWithItems` | RoomDTO 嵌套 ItemDTO 列表正确序列化/反序列化 |
| `testNullPlayerName` | null 玩家名经 JSON 往返后保持 null |
| `testNullInventory` | null 背包列表反序列化为空列表（Gson 默认行为） |
| `testRequiredItemsRoundtrip` | 任务物品列表经 JSON 往返正确 |
| `testHistoryRoomIdsRoundtrip` | 历史栈房间 ID 列表经 JSON 往返正确 |
| `testFloatPrecision` | 浮点数 0.1+0.2 精度误差 < 0.001 |

---

### 8.6 P3 批次：集成测试与端到端测试（36 个测试）

#### DatabaseManagerTest.java（16 个测试）

覆盖 `DatabaseManager` 的全部 7 个 CRUD 静态方法及 `GameSaveEntity` 实体：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testSaveGameReturnsValidId` | saveGame 返回正数 ID |
| `testSaveGameEmptyJson` | 空 JSON 存档保存和读取正常 |
| `testSaveGameNullSaveName` | null 存档名触发 PropertyValueException（记录行为） |
| `testLoadGameReturnsEntity` | loadGame 返回完整实体，所有字段匹配 |
| `testLoadGameNonExistent` | 加载不存在的 ID 返回 null |
| `testListSavesReturnsList` | listSaves 返回非 null 列表 |
| `testListSavesMetaReturnsProjection` | listSavesMeta 每行 4 列：id(Number)/saveName(String)/playerName(String)/createdAt(Number) |
| `testFindSaveByNameFound` | findSaveByName 按名称查找成功 |
| `testFindSaveByNameNotFound` | 查找不存在的名称返回 null |
| `testUpdateSaveChangesJson` | updateSave 修改 JSON 后 loadGame 验证变更 |
| `testDeleteGameRemovesEntity` | deleteGame 后 loadGame 返回 null |
| `testDeleteGameNonExistent` | 删除不存在的 ID 返回 false |
| `testGameSaveEntityConstructor` | 构造器正确设置 saveName/playerName/gameStateJson/createdAt/dataSize |
| `testGameSaveEntityDefaultConstructor` | 默认构造器 id 和 saveName 为 null |
| `testSetGameStateJsonUpdatesDataSize` | setGameStateJson 自动更新 dataSize |
| `testSetGameStateJsonNull` | null JSON 时 dataSize = 0 |

#### HibernateUtilTest.java（4 个测试）

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testGetSessionFactoryReturnsNonNull` | SessionFactory 获取（可为 null，取决于配置） |
| `testGetSessionFactoryReturnsSameInstance` | 两次获取返回同一单例引用 |
| `testShutdownDoesNotThrow` | shutdown 不抛异常 |
| `testGetSessionFactoryAfterShutdown` | shutdown 后重新获取的行为记录 |

#### GameServerTest.java（7 个测试）

覆盖 HTTP 服务器的关键端点（如服务器不可用则自动跳过）：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testIndexHtml` | GET / 返回 HTML 内容 |
| `test404` | GET /nonexistent → HTTP 404 |
| `testStateBeforeGameInitialized` | 未初始化时 /api/state 返回 gameExited=true |
| `testNewGameApi` | POST /api/newgame → 返回 success |
| `testNewGameEmptyName` | 空姓名新游戏 → 返回 success |
| `testCommandApiLook` | POST /api/command "look" → 返回非空响应 |
| `testCommandApiGo` | POST /api/command "go east" → 响应含 theater/lecture |

#### GameE2ETest.java（9 个测试）

端到端游戏流程测试，模拟真实玩家的完整操作序列：

| 测试方法 | 覆盖内容 |
|----------|----------|
| `testFullExplorationFlow` | 完整探索 7 步链路：outside→theater→outside→pub→transporter→pub→outside→lab→office |
| `testPickupAndDropFlow` | 拾取两件物品 → 丢弃一件 → 验证房间/背包状态同步 |
| `testBuyAndDrinkFlow` | 移动到 pub → 购买麦脉暖酿 + 月花蜜醴 → 饮用 → 物品消耗 |
| `testEatCookieFlow` | 吃魔法饼干 → 验证负重上限从 20 增至 30 |
| `testTeleportAndBackFlow` | pub→transporter→传送→回退至 transporter |
| `testQuestCollectionFlow` | 验证任务物品列表格式正确（非空、无空字符串） |
| `testFullSerializeRoundtrip` | 建立复杂状态（移动+拾取）→ 序列化 → 反序列化 → 验证 4 项关键状态一致 |
| `testNewGameThenOperate` | 新游戏 → 移动 → look：正常流程 |
| `testOverweightBoundaryFlow` | 加背包至 19.5kg → take 3.2kg 铁锹 → 超重提示 |

---

### 8.7 测试发现的关键缺陷

在测试编写过程中发现并记录了 15 个缺陷，按严重度分级：

| # | 严重度 | 位置 | 描述 |
|---|--------|------|------|
| 1 | 🟠 高 | `Game.getRandomRoom()` | `allRooms` 为空时抛出 `IllegalArgumentException` |
| 2 | 🟡 中 | `Game.runCommand(null)` | 空输入直接抛出 NullPointerException，未返回错误消息 |
| 3 | 🟡 中 | `Parser.getCommand()` | CLI 解析器只读取两个 token，"drop item 50 60" 坐标丢失 |
| 4 | 🟡 中 | `Player.getInventory()` | 返回可变内部 ArrayList，调用者可绕过 takeItem 重量检查 |
| 5 | 🟡 中 | `Game.getRequiredItems()` | 返回可变内部 List，外部 clear() 直接影响内部任务物品列表 |
| 6 | 🟡 中 | `GameServer` | 全局 `System.setOut` 重定向，并发请求下输出混乱 |
| 7 | 🟡 中 | `CommandQuit` | execute() 返回 true 但不调用 exitToMenu()，Web 上下文下 isRunning 不变 |
| 8 | 🟡 中 | `Room.getItem()` | 子串匹配 `contains()` 不精确，多物品时返回第一个匹配而可能不是预期物品 |
| 9 | 🟡 中 | `CommandBuy` | 购买不检查金币（仅前端检查），后端可绕过免费购买 |
| 10 | 🟡 中 | `Item` 构造器 | 无校验：接受负重量、负坐标、极端大值 |
| 11 | 🟢 低 | `Player.increaseMaxWeight()` | 无上限，可无限增大负重 |
| 12 | 🟢 低 | `CommandEat` | HP/MP 恢复仅打印消息，后端无 HP/MP 状态模型，前端独立维护 |
| 13 | 🟢 低 | `Game.startNewGame()` | `createRooms()` 已创建 Player 后立即被覆盖，浪费一次对象创建 |
| 14 | 🟢 低 | `GameStateDTO` | Gson 序列化 double 存在精度损失风险 |
| 15 | 🟢 低 | `CommandEat` 子串匹配 | `getItem("花")` 可能匹配到非预期物品 |

---

### 8.8 测试执行命令速查

```bash
# 运行全部测试（249 个）
mvn test

# 按批次运行
# P0 — 领域模型 + 核心命令（94 个）
mvn -Dtest="ItemTest,RoomFullTest,PlayerFullTest,CommandGoTest,CommandTakeTest,CommandDropTest,GameSerializeTest,GameNewGameTest" test

# P1 — 扩展功能（52 个）
mvn -Dtest="CommandEatTest,CommandBuyTest,GameWorldTest,GameTeleportTest,GameQuestTest,GameRunCommandTest" test

# P2 — 边界条件（60 个）
mvn -Dtest="CommandLookTest,CommandBackTest,CommandItemsTest,CommandHelpTest,CommandQuitTest,ParserTest,CommandWordsTest,GameStateDTOTest" test

# P3 — 集成测试（36 个）
mvn -Dtest="DatabaseManagerTest,HibernateUtilTest,GameServerTest,GameE2ETest" test

# 单个测试类
mvn -Dtest="PlayerFullTest" test

# 单个测试方法
mvn -Dtest="PlayerFullTest#testTakeItemOverweight" test

# 跳过数据库依赖测试（CI 环境无 SQLite）
mvn -Dtest="!DatabaseManagerTest,!HibernateUtilTest,!GameServerTest" test
```

---

## 9. 项目统计

| 指标 | 数值 |
|------|------|
| **总提交数** | 26 次（不含 merge commit） |
| **合并 PR** | 15 个 |
| **开发周期** | 6 天（2026-06-13 → 2026-06-18） |
| **贡献者** | 3 人 |
| **Java 源文件（生产）** | 30 个 |
| **Java 源文件（测试）** | 29 个 |
| **Java 代码总行数** | ~2,308 行 |
| **前端代码行数** | ~1,661 行（index.html 单文件） |
| **测试类** | 29 个（249 个测试方法，零失败） |
| **房间数量** | 6 个（5 普通 + 1 传送） |
| **NPC 数量** | 3 个（Saber Boss / 酒吧老板 / 博士） |
| **命令数量** | 10 个已注册 + 1 个隐藏（trigger_teleport） |
| **任务物品候选池** | 17 种 |
| **物品图标** | 42 个 PNG |
| **房间背景** | 6 个 PNG |
| **NPC 雪碧图** | 4 个（saber.png / saber_attack.png / barmaid.png / doctor.png） |
| **音效文件** | 6 个 MP3（1 BGM + 1 拾取 + 4 丢弃材质） |
| **字体制品** | 2 个 TTF |
| **API 端点** | 8 个 REST 接口 |
| **数据库表** | 1 个（game_saves） |

---

## 10. 致谢

- 原版 "World of Zuul" 游戏框架由 **Michael Kölling** 和 **David J. Barnes** 编写，为项目提供了坚实的面向对象设计基础（命令模式、房间模型、解析器架构）
- 感谢 **武汉理工大学计算机与人工智能学院** 软件工程课程组提供的实训平台、评审指导和 GitHub Classroom 基础设施
- 本项目在开发过程中使用了 AI（Claude / DeepSeek）进行辅助开发，具体使用模型、工作内容及使用方式详见小组实训报告（REPORT.docx）
- 特别感谢三位小组成员在短短 5 天内的紧密协作——每天多次 PR 合并、相互 Code Review、快速修复合并冲突，充分体现了敏捷开发与小组协同的核心理念
