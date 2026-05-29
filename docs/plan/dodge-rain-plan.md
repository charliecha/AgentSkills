# Implementation Plan: 极简雨滴 — Android 躲避游戏

## Overview

用 Android 原生（Kotlin + SurfaceView + Canvas）构建一个竖版单屏躲避游戏。按依赖顺序分三个阶段：先搭项目骨架，再实现核心游戏逻辑，最后接入渲染与输入。每阶段结束有检查点。

## Architecture Decisions

- **SurfaceView 独立渲染线程**：游戏循环运行在子线程，主线程只处理触摸事件，避免 ANR
- **GameEngine 与 GameView 分离**：逻辑不依赖 Android View，便于单元测试
- **依赖方向**：`GameView` → `GameEngine` → `Player` / `Obstacle`；`GameRenderer` 依赖所有数据类但不持有逻辑

## Dependency Graph

```
Player.kt         Obstacle.kt
    │                  │
    └──────┬───────────┘
           ▼
      GameEngine.kt
           │
    ┌──────┴──────────┐
    ▼                 ▼
GameRenderer.kt   GameView.kt
                      │
                 MainActivity.kt
```

---

## Phase 1：项目骨架

### Task 1：创建 Android 项目 + 基础文件结构

**Description：** 用 Android Studio 创建 Empty Activity 项目，建立 spec 中定义的包结构，创建所有 Kotlin 文件（内容为空壳）。

**Acceptance criteria：**
- [ ] 包名 `com.example.dodgerain`，minSdk 24，targetSdk 34，语言 Kotlin
- [ ] 所有 6 个 Kotlin 文件存在：`MainActivity`、`GameView`、`GameEngine`、`Player`、`Obstacle`、`GameRenderer`
- [ ] 测试目录结构存在：`test/` 和 `androidTest/`

**Verification：**
- [ ] `./gradlew assembleDebug` 构建成功

**Dependencies：** None

**Files：**
- `app/build.gradle.kts`
- `app/src/main/java/com/example/dodgerain/*.kt`（6 个空壳文件）
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/AndroidManifest.xml`

**Scope：** M

---

### Task 2：实现 Player 数据类 + 移动逻辑

**Description：** 实现 `Player.kt`，包含位置、尺寸、边界移动、碰撞矩形。不依赖任何 Android View。

**Acceptance criteria：**
- [ ] `Player` 持有 `x`、`y`、`width`、`height`
- [ ] `moveTo(x: Float, screenWidth: Int)` 将角色移动到目标 X，并限制在屏幕边界内
- [ ] `toRect()` 返回 `RectF` 用于碰撞检测
- [ ] 单元测试覆盖：边界限制（左边界、右边界、中间正常移动）

**Verification：**
- [ ] `./gradlew test --tests "*.PlayerTest"` 全部通过

**Dependencies：** Task 1

**Files：**
- `app/src/main/java/com/example/dodgerain/Player.kt`
- `app/src/test/java/com/example/dodgerain/PlayerTest.kt`

**Scope：** S

---

### Task 3：实现 Obstacle 数据类 + 下落逻辑

**Description：** 实现 `Obstacle.kt`，包含位置、尺寸、下落更新、屏幕外判断、碰撞矩形。

**Acceptance criteria：**
- [ ] `Obstacle` 持有 `x`、`y`、`width=60f`、`height=40f`
- [ ] `update(speedPx: Float)` 使 `y += speedPx`
- [ ] `isOffScreen(screenHeight: Int)` 在 `y > screenHeight` 时返回 true
- [ ] `toRect()` 返回 `RectF`
- [ ] 单元测试覆盖：下落更新、屏幕外判断

**Verification：**
- [ ] `./gradlew test --tests "*.ObstacleTest"` 全部通过

**Dependencies：** Task 1

**Files：**
- `app/src/main/java/com/example/dodgerain/Obstacle.kt`
- `app/src/test/java/com/example/dodgerain/ObstacleTest.kt`

**Scope：** S

---

### ✅ Checkpoint 1：Foundation

- [ ] `./gradlew test` 全部单元测试通过
- [ ] `./gradlew assembleDebug` 构建成功，无 error
- [ ] `Player` 和 `Obstacle` 逻辑完整，有测试覆盖

---

## Phase 2：核心游戏逻辑

### Task 4：实现 GameEngine — 状态机 + 碰撞 + 得分

**Description：** 实现 `GameEngine.kt`，这是整个游戏的核心。包含状态机（IDLE/PLAYING/GAME_OVER）、障碍物生成计时、碰撞检测、命数管理、得分计时、无敌冷却。

**Acceptance criteria：**
- [ ] 状态机：`GameState` enum（IDLE / PLAYING / GAME_OVER）
- [ ] `startGame()` 重置所有状态，切换到 PLAYING
- [ ] `update(deltaMs: Long)` 在 PLAYING 状态下：
  - 每 800ms 生成一个随机 X 位置的 Obstacle
  - 更新所有 Obstacle 位置
  - 移除离开屏幕的 Obstacle
  - 每次碰撞检测：命中且不在无敌期则 `lives--`，触发 1.5s 无敌冷却
  - `lives == 0` 时切换到 GAME_OVER
  - 累加存活时间 → `scoreSeconds`
  - 速度递增：每 5 秒 `currentSpeed += 1`，上限 20
- [ ] `onTouchX(x: Float)` 调用 `player.moveTo()`
- [ ] 单元测试覆盖：碰撞扣血、无敌冷却、速度递增、GAME_OVER 触发

**Verification：**
- [ ] `./gradlew test --tests "*.GameEngineTest"` 全部通过

**Dependencies：** Task 2, Task 3

**Files：**
- `app/src/main/java/com/example/dodgerain/GameEngine.kt`
- `app/src/test/java/com/example/dodgerain/GameEngineTest.kt`

**Scope：** M

---

### ✅ Checkpoint 2：Core Logic

- [ ] `./gradlew test` 全部通过（Player + Obstacle + GameEngine）
- [ ] 核心逻辑覆盖率 > 80%（`./gradlew test` 查看报告）
- [ ] GameEngine 可独立运行，不依赖任何 View

---

## Phase 3：渲染与接入

### Task 5：实现 GameRenderer — Canvas 绘制

**Description：** 实现 `GameRenderer.kt`，负责将 GameEngine 的状态绘制到 Canvas。包含三个画面：IDLE（提示触摸开始）、PLAYING（游戏画面）、GAME_OVER（得分 + 重玩提示）。

**Acceptance criteria：**
- [ ] `render(canvas: Canvas, engine: GameEngine)` 根据 `engine.state` 分支绘制
- [ ] PLAYING 画面：绘制玩家（亮色矩形）、所有障碍物（深色矩形）、顶部状态栏（命数 ❤ + 计时）
- [ ] IDLE 画面：绘制居中提示文字"Touch to Start"
- [ ] GAME_OVER 画面：绘制得分 + "Tap to Restart"
- [ ] 所有坐标基于传入的 `screenWidth`/`screenHeight`，无硬编码

**Verification：**
- [ ] `./gradlew assembleDebug` 构建成功（Renderer 无编译错误）
- [ ] 视觉检查（下一步在模拟器上确认）

**Dependencies：** Task 4

**Files：**
- `app/src/main/java/com/example/dodgerain/GameRenderer.kt`

**Scope：** M

---

### Task 6：实现 GameView — SurfaceView + 游戏循环 + 触摸

**Description：** 实现 `GameView.kt`，继承 `SurfaceView`，启动独立渲染线程运行游戏循环，处理触摸事件并转发给 GameEngine。

**Acceptance criteria：**
- [ ] 继承 `SurfaceView`，实现 `SurfaceHolder.Callback`
- [ ] `surfaceCreated` 时启动渲染线程，`surfaceDestroyed` 时安全停止
- [ ] 游戏循环：固定 ~16ms/帧（~60fps），调用 `engine.update(delta)` 和 `renderer.render(canvas, engine)`
- [ ] `onTouchEvent`：`ACTION_DOWN` / `ACTION_MOVE` 时调用 `engine.onTouchX(event.x)`；`ACTION_UP` 在 IDLE/GAME_OVER 时调用 `engine.startGame()`
- [ ] 屏幕尺寸通过 `holder.surfaceFrame` 动态获取，传给 Engine 和 Renderer

**Verification：**
- [ ] `./gradlew assembleDebug` 构建成功
- [ ] 安装到模拟器，游戏可启动、可操作、可结束、可重玩

**Dependencies：** Task 5

**Files：**
- `app/src/main/java/com/example/dodgerain/GameView.kt`

**Scope：** M

---

### Task 7：接入 MainActivity + 布局

**Description：** 实现 `MainActivity.kt`，在布局中使用 `GameView`，处理全屏显示和屏幕旋转锁定（竖屏）。

**Acceptance criteria：**
- [ ] `activity_main.xml` 只包含 `GameView`，铺满全屏
- [ ] `AndroidManifest.xml` 锁定竖屏（`screenOrientation="portrait"`）
- [ ] 隐藏系统 UI（沉浸式全屏）
- [ ] `MainActivity` 仅做持有，不含任何游戏逻辑

**Verification：**
- [ ] `./gradlew installDebug` 安装成功
- [ ] 模拟器上全屏显示，旋转设备不影响游戏

**Dependencies：** Task 6

**Files：**
- `app/src/main/java/com/example/dodgerain/MainActivity.kt`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/AndroidManifest.xml`

**Scope：** S

---

### ✅ Checkpoint 3：完整可运行

- [ ] `./gradlew assembleDebug` 构建成功，无 warning
- [ ] `./gradlew test` 全部单元测试通过
- [ ] 模拟器上完整验证 spec 成功标准 8 条全部通过：
  - [ ] 游戏启动，触摸开始
  - [ ] 角色跟随手指
  - [ ] 障碍物下落，速度递增
  - [ ] 碰撞扣命，UI 更新
  - [ ] 三命归零进入 GAME_OVER
  - [ ] 显示存活时间得分
  - [ ] 点击重玩正确重置

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| SurfaceView 线程同步问题（Engine 状态被主线程和渲染线程同时访问） | High | GameEngine 所有状态用 `@Volatile` 或 `synchronized`，触摸事件只写 `targetX`，渲染线程只读 |
| 模拟器帧率不稳定 | Low | 用 `deltaMs` 做时间步长，速度基于时间而非帧数 |
| 碰撞检测误判（矩形太大/太小） | Med | 单元测试覆盖边界 case，模拟器上目视验证 |

## Open Questions

（无，所有决策已在 spec 中确认）
