# 数据库初始化指南

## 方法一：使用命令行初始化（推荐）

### 1. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行
CREATE DATABASE meeting_transcribe DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit
```

### 2. 执行初始化脚本

```bash
# Windows 系统
mysql -u root -p meeting_transcribe < c:\桌面\wechatPush\backend\src\main\resources\schema.sql

# Linux/Mac 系统
mysql -u root -p meeting_transcribe < /path/to/wechatPush/backend/src/main/resources/schema.sql
```

### 3. 验证初始化结果

```bash
mysql -u root -p
USE meeting_transcribe;
SHOW TABLES;
```

应该看到以下表：
- device
- device_binding
- meeting
- meeting_tag
- push_record
- transcript
- user


## 方法二：使用 MySQL Workbench 或 Navicat

### 1. 创建数据库

1. 打开 MySQL Workbench 或 Navicat
2. 连接到 MySQL 服务器
3. 创建新数据库 `meeting_transcribe`
4. 设置字符集为 `utf8mb4`，排序规则为 `utf8mb4_unicode_ci`

### 2. 执行 SQL 脚本

1. 选择 `meeting_transcribe` 数据库
2. 打开 SQL 编辑器
3. 复制 `schema.sql` 文件内容
4. 执行 SQL 脚本


## 方法三：让 Spring Boot 自动创建表

### 1. 创建数据库

```bash
mysql -u root -p
CREATE DATABASE meeting_transcribe DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit
```

### 2. 修改 application.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 自动创建和更新表结构
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
```

### 3. 启动 Spring Boot 应用

表结构会自动创建，但不会插入测试数据。


## 常见问题解决

### 1. 数据库连接失败

**错误信息**：`Access denied for user 'root'@'localhost'`

**解决方案**：
- 检查 MySQL 服务是否启动
- 检查用户名和密码是否正确
- 检查 MySQL 端口是否为 3306

### 2. 字符集错误

**错误信息**：`Unknown character set: 'utf8mb4'`

**解决方案**：
- 升级 MySQL 到 5.5.3 或更高版本
- 或将字符集改为 `utf8`

### 3. 表已存在错误

**错误信息**：`Table 'xxx' already exists`

**解决方案**：
```sql
DROP DATABASE meeting_transcribe;
CREATE DATABASE meeting_transcribe DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 外键约束错误

**错误信息**：`Cannot add foreign key constraint`

**解决方案**：
- 确保所有表使用相同的存储引擎（InnoDB）
- 确保字符集和排序规则一致
- 确保引用的表和字段存在


## 配置数据库连接

修改 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meeting_transcribe?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password  # 修改为您的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 验证数据库初始化

### 1. 检查表结构

```sql
USE meeting_transcribe;
SHOW TABLES;
DESC user;
DESC device;
DESC device_binding;
DESC meeting;
```

### 2. 检查测试数据

```sql
SELECT * FROM device;
```

应该看到两条测试数据：
- DEV_001: 会议室录音设备1
- DEV_002: 会议室录音设备2


## 下一步

数据库初始化完成后：

1. **配置企业微信**：修改 `application.yml` 中的企业微信配置
2. **启动后端**：`cd backend && mvn spring-boot:run`
3. **访问前端**：打开浏览器访问 `http://localhost:3000`
