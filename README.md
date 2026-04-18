# Heima AI - 智能对话服务平台

基于 Spring Boot 和 Spring AI 构建的智能对话服务平台，支持多种AI模型、PDF文档问答、多模态对话等功能。

## 技术栈

- **框架**: Spring Boot 4.0.3
- **AI引擎**: Spring AI 2.0.0-M2
- **数据库**: MySQL 8.0+ + MyBatis Plus 3.5.11
- **语言**: Java 20
- **AI模型**: 
  - Ollama (DeepSeek R1 1.5B)
  - OpenAI Compatible (通义千问 qwen-max-latest)

## 功能特性

### 🤖 智能聊天
- 支持文本对话和多模态对话（图片+文本）
- 支持会话记忆功能
- 支持多种AI模型切换

### 📄 PDF文档问答
- 上传PDF文件进行智能问答
- 基于向量数据库的语义检索
- 支持文件下载

### 🛎️ 客服服务
- 专门的客服对话接口
- 独立的会话管理

### 📚 课程管理
- 课程信息查询
- 课程预约服务
- 校区管理

## 项目结构

```
src/
├── main/
│   ├── java/com/itheima/ai/
│   │   ├── controller/     # REST API 控制器
│   │   │   ├── ChatController.java       # 智能聊天接口
│   │   │   ├── CustomerServiceController.java  # 客服接口
│   │   │   ├── PdfController.java        # PDF问答接口
│   │   │   └── gameController.java       # 游戏接口
│   │   ├── service/        # 业务服务层
│   │   ├── mapper/         # MyBatis Mapper
│   │   ├── entity/         # 实体类
│   │   ├── repository/     # 数据仓库
│   │   ├── config/         # 配置类
│   │   ├── tool/           # 工具类
│   │   └── HeimaAiApplication.java
│   └── resources/
│       ├── mapper/         # MyBatis XML
│       └── application.yaml
└── test/                   # 测试类
```

## 快速开始

### 前置条件

1. **安装 Ollama**（用于本地模型）
   ```bash
   # 安装 Ollama
   curl -fsSL https://ollama.com/install.sh | sh
   
   # 拉取 DeepSeek R1 模型
   ollama pull deepseek-r1:1.5b
   
   # 启动 Ollama 服务
   ollama serve
   ```

2. **配置 MySQL 数据库**
   - 创建数据库 `course`
   - 确保数据库连接信息与 `application.yaml` 匹配

3. **配置环境变量**
   ```bash
   # 设置阿里云通义千问 API Key
   export OPENAI_API_KEY=your-api-key
   ```

### 运行项目

```bash
# 进入项目目录
cd heima-ai

# 使用 Maven 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/heima-ai-0.0.1-SNAPSHOT.jar
```

服务启动后访问: http://localhost:8080

## API 接口

### 智能聊天

**POST** `/ai/chat`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | 是 | 用户提问内容 |
| chatId | String | 是 | 会话ID |
| files | MultipartFile[] | 否 | 上传的图片文件 |

**响应**: 流式文本响应

### PDF问答

**POST** `/ai/pdf/upload/{chatId}`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| chatId | String | 是 | 会话ID（路径参数） |
| file | MultipartFile | 是 | PDF文件 |

**POST** `/ai/pdf/chat`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | 是 | 用户提问内容 |
| chatId | String | 是 | 会话ID |

**GET** `/ai/pdf/file/{chatId}`

下载已上传的PDF文件

### 客服服务

**POST** `/ai/service`

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | 是 | 用户提问内容 |
| chatId | String | 是 | 会话ID |
## 配置说明

### application.yaml 关键配置

```yaml
spring:
  ai:
    # Ollama 本地模型配置
    ollama:
      base-url: http://localhost:11434
      chat:
        model: deepseek-r1:1.5b
    
    # 阿里云通义千问配置
    openai:
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: qwen-max-latest
          temperature: 0.8
          max-tokens: 2000

  datasource:
    url: jdbc:mysql://localhost:3306/course?serverTimezone=Asia/Shanghai
    username: root
    password: your-password
```

## 核心组件

### ChatClient 配置
项目配置了多个 ChatClient 实例：
- `chatClient`: 通用聊天客户端
- `serviceChatClient`: 客服专用客户端
- `pdfChatClient`: PDF问答专用客户端（集成向量检索）

### 会话管理
使用 `InMemoryChatHistoryRepository` 管理会话历史，支持按会话类型分类存储。

### 文件存储
使用 `LocalPdfFileRepository` 存储上传的PDF文件，支持文件的保存和读取。

## 开发说明

### 添加新的 AI 工具

在 `tool/` 目录下创建新的工具类，实现特定业务功能，如 `CourseTools` 用于课程相关操作。

### 添加新的控制器

继承现有模式，在 `controller/` 目录下创建新的 REST 控制器，处理特定业务请求。

## 测试

```bash
# 运行单元测试
mvn test

# 运行特定测试类
mvn test -Dtest=HeimaAiApplicationTests
```

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！