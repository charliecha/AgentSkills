# Spec: 极简雨滴 — Android 躲避游戏

## Objective

构建一个竖版单屏 Android 躲避休闲游戏。玩家控制底部角色左右移动，躲避从顶部随机下落的障碍物。三条命制，存活时间即得分。

**用户：** 有 Android 基础的开发者，目标是走通完整 agent-skills 工程流程。

**成功标准：**
- 游戏可在 Android 模拟器或真机上正常启动、运行、结束
- 碰撞检测准确，无明显误判
- 帧率稳定在 60fps（SurfaceView 独立渲染线程）
- 手指跟随响应延迟不可感知（< 16ms）
- 完整走通 spec → plan → build → test → review → ship 流程

---

## Tech Stack

- **语言：** Kotlin
- **最小 API Level：** 24（Android 7.0，覆盖 ~94% 设备）
- **目标 API Level：** 34
- **渲染：** `SurfaceView` + `Canvas`（原生，不引入游戏引擎）
- **构建：** Gradle（Android Studio 创建标准项目）
- **测试：** JUnit 4 + Android Instrumented Tests

---

## Commands

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 运行 Instrumented 测试（需连接设备/模拟器）
./gradlew connectedAndroidTest

# 代码检查
./gradlew lint

# 安装到设备
./gradlew installDebug
```

---

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/dodgerain/
│   │   │   ├── MainActivity.kt        # 入口，持有 GameView
│   │   │   ├── GameView.kt            # SurfaceView，游戏主循环
│   │   │   ├── GameEngine.kt          # 游戏逻辑（状态机、更新、碰撞）
│   │   │   ├── Player.kt              # 玩家数据与移动逻辑
│   │   │   ├── Obstacle.kt            # 障碍物数据与下落逻辑
│   │   │   └── GameRenderer.kt        # Canvas 绘制逻辑
│   │   ├── res/
│   │   │   └── layout/
│   │   │       └── activity_main.xml  # 仅包含 GameView
│   │   └── AndroidManifest.xml
│   ├── test/
│   │   └── java/com/example/dodgerain/
│   │       ├── GameEngineTest.kt      # 碰撞检测、得分、命数单元测试
│   │       ├── PlayerTest.kt          # 玩家移动边界测试
│   │       └── ObstacleTest.kt        # 障碍物生成、速度递增测试
│   └── androidTest/
│       └── java/com/example/dodgerain/
│           └── GameViewTest.kt        # UI 渲染 Instrumented 测试
docs/
├── ideas/dodge-rain.md
└── spec/dodge-rain-spec.md
```

---

## Code Style

Kotlin 标准风格，类职责单一，无全局状态。

```kotlin
// 好：数据与逻辑分离，命名清晰
data class Obstacle(
    var x: Float,
    var y: Float,
    val width: Float = 60f,
    val height: Float = 40f,
) {
    fun update(speedPx: Float) {
        y += speedPx
    }

    fun isOffScreen(screenHeight: Int): Boolean = y > screenHeight

    fun toRect() = RectF(x, y, x + width, y + height)
}

// 好：碰撞检测在 GameEngine，不在 View
fun checkCollision(player: Player, obstacle: Obstacle): Boolean =
    RectF.intersects(player.toRect(), obstacle.toRect())
```

**命名规范：**
- 类：`PascalCase`
- 函数/变量：`camelCase`
- 常量：`UPPER_SNAKE_CASE`（放在 companion object）
- 文件名与类名一致

---

## Game Design

### 游戏状态机
```
IDLE ──[触摸屏幕]──→ PLAYING ──[命数归零]──→ GAME_OVER
                                               │
                          ←──[点击重玩]────────┘
```

### 玩家（Player）
- 位置：屏幕底部，垂直居中于底部 1/6 区域
- 尺寸：80×80px 矩形色块（亮色）
- 移动：`ACTION_MOVE` 事件中直接设置 `x = event.x - width/2`
- 边界：不可超出屏幕左右边缘

### 障碍物（Obstacle）
- 尺寸：宽 60px，高 40px，深色矩形色块
- 生成：每 800ms 在随机 X 位置（保证完整在屏幕内）从顶部生成
- 速度：初始 8px/frame，每存活 5 秒增加 1px/frame，上限 20px/frame
- 生命周期：超出屏幕底部后移除

### 得分与命数
- 得分：存活秒数（整数），每秒 +1
- 命数：初始 3，碰撞一次 -1，归零触发 GAME_OVER
- 碰撞冷却：碰撞后 1.5 秒无敌（防止连续扣血）

### UI 布局（Canvas 直接绘制）
```
┌─────────────────────────┐
│ ❤❤❤          00:12      │  ← 顶部状态栏（命数 + 计时）
│                         │
│      [障碍物]            │
│            [障碍物]      │
│                         │
│                         │
│        [玩家]            │  ← 底部角色
└─────────────────────────┘
```

---

## Testing Strategy

**框架：** JUnit 4（单元测试）+ AndroidX Test（Instrumented）

**原则：** 核心游戏逻辑（碰撞、得分、状态机）必须有单元测试；View 层不测绘制细节。

| 测试层级 | 覆盖内容 | 位置 |
|---|---|---|
| 单元测试 | 碰撞检测、边界移动、速度递增、命数扣减 | `src/test/` |
| Instrumented | GameView 启动不崩溃、状态切换 | `src/androidTest/` |

**覆盖率目标：** 核心逻辑（`GameEngine`、`Player`、`Obstacle`）> 80%

---

## Boundaries

**Always（必须做）：**
- 游戏逻辑写在 `GameEngine`，不写在 `GameView`（View 只负责渲染和输入转发）
- 新增功能前先更新本 spec
- 提交前运行 `./gradlew test` 确保单元测试通过

**Ask First（先确认）：**
- 引入任何第三方库
- 修改最小 API Level
- 改变游戏核心参数（初始速度、障碍物生成频率）

**Never（绝对不做）：**
- 在 `GameView` 里写游戏逻辑
- 在主线程做渲染循环
- 提交包含硬编码屏幕尺寸的代码（必须动态获取）

---

## Success Criteria

- [ ] `./gradlew assembleDebug` 构建成功，无 warning
- [ ] `./gradlew test` 全部单元测试通过
- [ ] 游戏在模拟器上启动，触摸开始游戏
- [ ] 角色跟随手指移动，无明显延迟
- [ ] 障碍物从顶部生成并下落，速度随时间递增
- [ ] 碰撞后命数 -1，UI 更新正确
- [ ] 三条命用完进入游戏结束界面，显示存活时间
- [ ] 点击重玩，游戏正确重置

---

## Open Questions

（已全部解决）
- ~~View vs SurfaceView？~~ → SurfaceView
- ~~最小 API Level？~~ → API 24
- ~~手势方式？~~ → 跟随手指 X 位置
