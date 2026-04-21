# TURNS 部署到 `47.102.159.90`

这份文档按当前项目真实结构编写：

- 主仓库：`TURNS`
- 前端目录：`front/`
- 后端目录：`back/`
- 后端技术栈：Spring Boot + MySQL + WebSocket
- 前端技术栈：Vue 2 + Vue CLI

本文默认新服务器是 Ubuntu 22.04/24.04，且你希望：

- 远端 GitHub `master` 就是完整的 TURNS 项目
- 服务器上通过 Nginx 对外提供前端页面
- 后端 Jar 通过 `systemd` 常驻运行在 `8080`
- MySQL 与后端在同一台服务器

如果你的服务器不是 Ubuntu/Debian，我再给你改成对应发行版命令。

## 1. 先同步代码到 GitHub

本项目现在应该只有一个主仓库：`TURNS`。

本地确认：

```powershell
cd D:\dev-project\TURNS
git status
git branch
git remote -v
```

目标状态：

- 当前分支是 `master`
- 不再存在 `front` 子仓库
- `front/` 是主仓库里的普通目录

推送：

```powershell
git push origin master
```

## 2. 本地准备部署产物

推荐采用“本地打包，服务器只运行”的方式。对单人项目来说最稳，服务器不用装 Node 和 Maven。

### 2.1 前端打包

前端现在通过环境变量决定 API 和 WebSocket 地址。打生产包前，在 PowerShell 里设置：

```powershell
cd D:\dev-project\TURNS\front
$env:VUE_APP_BASE_API="http://47.102.159.90/api"
$env:VUE_APP_WS_API="ws://47.102.159.90/ws"
npm run build
```

打包完成后，产物在：

```text
D:\dev-project\TURNS\front\dist
```

### 2.2 后端 Jar

如果你本地能跑 Maven，建议重新打包一次：

```powershell
cd D:\dev-project\TURNS\back
mvn clean package -DskipTests
```

如果本地没有 Maven，但 `back\target\back-0.0.1-SNAPSHOT.jar` 已经是最新版本，也可以直接使用它。

## 3. 导出现有数据库

当前后端依赖 MySQL 数据库 `lumi2024`。因为项目里没有完整的数据库迁移体系，部署到新服务器时，最稳妥的方式是把你现在本地数据库整库导出，再导入到服务器。

本地执行：

```powershell
mysqldump -uroot -p --single-transaction --default-character-set=utf8mb4 lumi2024 > D:\dev-project\TURNS\lumi2024.sql
```

输入你本地 MySQL 密码后，会得到：

```text
D:\dev-project\TURNS\lumi2024.sql
```

## 4. 上传文件到服务器

建议服务器目录统一放在 `/opt/TURNS`。

本地上传：

```powershell
scp -r D:\dev-project\TURNS\front\dist root@47.102.159.90:/opt/TURNS/front/
scp D:\dev-project\TURNS\back\target\back-0.0.1-SNAPSHOT.jar root@47.102.159.90:/opt/TURNS/backend/
scp D:\dev-project\TURNS\lumi2024.sql root@47.102.159.90:/opt/TURNS/
```

如果服务器禁用了 root 直登，请把 `root` 改成你自己的登录用户。

## 5. 服务器初始化

SSH 登录服务器：

```bash
ssh root@47.102.159.90
```

创建目录：

```bash
mkdir -p /opt/TURNS/front
mkdir -p /opt/TURNS/backend/logs
mkdir -p /opt/TURNS/uploads
mkdir -p /opt/TURNS/nginx
```

安装运行环境：

```bash
apt update
apt install -y nginx mysql-server openjdk-17-jre-headless
```

说明：

- Spring Boot 2.6.13 运行在 Java 17 上没有问题
- 如果你更偏好 Java 8，也可以安装 Java 8，但 Ubuntu 上 Java 17 通常更省心

## 6. 初始化 MySQL

进入 MySQL：

```bash
mysql -uroot -p
```

执行：

```sql
CREATE DATABASE IF NOT EXISTS lumi2024 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'turns'@'localhost' IDENTIFIED BY '请改成你自己的强密码';
GRANT ALL PRIVILEGES ON lumi2024.* TO 'turns'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

导入数据：

```bash
mysql -uroot -p lumi2024 < /opt/TURNS/lumi2024.sql
```

## 7. 配置后端 systemd 服务

新建服务文件：

```bash
cat >/etc/systemd/system/turns-backend.service <<'EOF'
[Unit]
Description=TURNS Backend Service
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/TURNS/backend
ExecStart=/usr/bin/java -Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/TURNS/backend/logs -XX:ErrorFile=/opt/TURNS/backend/logs/hs_err_pid%p.log -jar /opt/TURNS/backend/back-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5
LimitNOFILE=65535
Environment=TZ=Asia/Shanghai
Environment=SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/lumi2024?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8
Environment=SPRING_DATASOURCE_USERNAME=turns
Environment=SPRING_DATASOURCE_PASSWORD=请改成你自己的强密码
Environment=FILE_UPLOAD_PATH=/opt/TURNS/uploads/
Environment=FILE_ACCESS_PATH=http://47.102.159.90/uploads/
Environment=SERVER_TOMCAT_ACCESSLOG_DIRECTORY=/opt/TURNS/backend/logs

[Install]
WantedBy=multi-user.target
EOF
```

说明：

- `allowPublicKeyRetrieval=true` 很重要
- 这是因为当前项目连接 MySQL 8 时，否则很容易报 `Public Key Retrieval is not allowed`
- 上传目录已改成 Linux 路径 `/opt/TURNS/uploads/`

启动后端：

```bash
systemctl daemon-reload
systemctl enable turns-backend
systemctl restart turns-backend
systemctl status turns-backend --no-pager
```

## 8. 配置 Nginx

新建 Nginx 站点配置：

```bash
cat >/etc/nginx/sites-available/turns <<'EOF'
server {
    listen 80;
    server_name 47.102.159.90;

    root /opt/TURNS/front/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /ws {
        proxy_pass http://127.0.0.1:8080/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /uploads/ {
        proxy_pass http://127.0.0.1:8080/uploads/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF
```

启用站点：

```bash
ln -sf /etc/nginx/sites-available/turns /etc/nginx/sites-enabled/turns
nginx -t
systemctl restart nginx
systemctl enable nginx
```

## 9. 开放安全组和防火墙

至少开放：

- `22/tcp`：SSH
- `80/tcp`：HTTP

如果你后面要上 HTTPS，再开放：

- `443/tcp`

如果服务器启用了 `ufw`：

```bash
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw reload
```

## 10. 部署后检查

服务器执行：

```bash
systemctl status turns-backend --no-pager
systemctl status nginx --no-pager
systemctl status mysql --no-pager
ss -lntp | grep -E '80|8080'
```

访问检查：

- 前端首页：`http://47.102.159.90/`
- 后端接口：`http://47.102.159.90/api/`
- Swagger：`http://47.102.159.90/api/swagger-ui.html`
- 上传目录：`http://47.102.159.90/uploads/`

日志检查：

```bash
journalctl -u turns-backend -n 200 --no-pager
tail -n 200 /opt/TURNS/backend/logs/*.log
tail -n 200 /var/log/nginx/error.log
```

## 11. 后续更新流程

以后你每次更新项目，推荐这样做：

### 本地

```powershell
cd D:\dev-project\TURNS
git pull

cd front
$env:VUE_APP_BASE_API="http://47.102.159.90/api"
$env:VUE_APP_WS_API="ws://47.102.159.90/ws"
npm run build

cd ..\back
mvn clean package -DskipTests
```

### 上传

```powershell
scp -r D:\dev-project\TURNS\front\dist root@47.102.159.90:/opt/TURNS/front/
scp D:\dev-project\TURNS\back\target\back-0.0.1-SNAPSHOT.jar root@47.102.159.90:/opt/TURNS/backend/
```

### 服务器

```bash
systemctl restart turns-backend
systemctl reload nginx
```

## 12. 可选优化

后面你如果继续维护这个项目，我建议再做三件事：

1. 给项目绑定域名，再上 HTTPS
2. 把数据库密码从服务文件里挪到单独的环境文件
3. 给 MySQL 做定时备份，例如每天一份 `mysqldump`
