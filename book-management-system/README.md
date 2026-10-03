# 图书管理系统（Java 版）

基于 **Spring Boot + MyBatis-Plus + MySQL + Thymeleaf** 的图书管理系统，支持在 **IntelliJ IDEA** 中一键运行。

## 功能

- 注册 / 登录：账号分为**管理员**与**读者**两种角色，密码 BCrypt 加密存储
- 图书查询：读者登录后可按书名 / 作者 / ISBN 搜索、按分类筛选、分页浏览、查看详情
- 图书管理：管理员可新增、编辑、删除图书
- 权限控制：未登录访问任意页面会跳转登录页；读者访问管理后台会被拦截

## 项目结构

```
book-management-system/
├── sql/
│   └── init.sql                       # MySQL 建库建表脚本（library_users 用户表 + books 图书表）
├── src/main/java/com/library/
│   ├── LibraryApplication.java        # 启动类（运行它即可）
│   ├── config/WebConfig.java          # 拦截器配置
│   ├── interceptor/LoginInterceptor.java  # 登录 / 角色权限拦截
│   ├── controller/                    # 登录注册、图书浏览、图书管理控制器
│   ├── entity/                        # User / Book 实体
│   ├── mapper/                        # MyBatis-Plus Mapper
│   ├── service/                       # 业务逻辑
│   └── common/                        # 业务异常
├── src/main/resources/
│   ├── application.yml                # 数据库连接等配置
│   ├── static/css/style.css           # 页面样式
│   └── templates/                     # 登录、注册、图书列表、详情、管理后台页面
└── pom.xml
```

## 环境要求

| 软件 | 版本 |
| ---- | ---- |
| JDK | 8 / 11 / 17 均可 |
| IntelliJ IDEA | 2020+（社区版 / 旗舰版均可） |
| MySQL | 8.0+ |
| Maven | IDEA 自带，无需单独安装 |

## 运行步骤

### 1. 初始化数据库

用 **Navicat**、**IDEA Database 面板**或**命令行**执行 `sql/init.sql`：

```bash
mysql -u root -p < sql/init.sql
```

脚本会自动创建 `library_db` 数据库、`library_users`（用户表）和 `books`（图书表）两张表，并写入示例账号和 11 本示例图书。

### 2. 修改数据库连接配置

打开 `src/main/resources/application.yml`，把数据库用户名 / 密码改成你本地的：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root          # 改成你的 MySQL 用户名
    password: 123456        # 改成你的 MySQL 密码
```

### 3. 用 IDEA 打开并运行

1. IDEA → `File` → `Open` → 选择本项目目录（`pom.xml` 所在位置），IDEA 会自动识别为 Maven 项目并下载依赖（首次较慢）
2. 等待依赖下载完成后，打开 `com.library.LibraryApplication`
3. 点击 main 方法左侧的绿色三角 **Run**（或右键 → Run）
4. 控制台出现 `Started LibraryApplication` 后，浏览器访问：**http://localhost:8080**

> 也可以不依赖 IDEA，在项目根目录执行 `mvn spring-boot:run` 启动。

## 默认账号

| 角色 | 用户名 | 密码 |
| ---- | ------ | ---- |
| 管理员 | admin | 123456 |
| 读者 | reader | 123456 |

也可以点「立即注册」自行注册账号（注册时可选择角色）。

## 数据库表

- `library_users`：用户账号表（用户名、BCrypt 密码、角色、显示名）
- `books`：图书表（书名、作者、ISBN、分类、简介、封面、出版社、库存等）

## 页面入口

- `/login` 登录页
- `/register` 注册页
- `/books` 图书列表（登录后访问，读者视图）
- `/books/{id}` 图书详情
- `/admin/books` 图书管理后台（仅管理员可访问）
