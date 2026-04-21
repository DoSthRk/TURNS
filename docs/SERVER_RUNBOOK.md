# TURNS Server Runbook

## 1) Process Management

Use `systemd` instead of manual `nohup`, so the service can auto-restart and keep structured logs.

```ini
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

[Install]
WantedBy=multi-user.target
```

## 2) Baseline Capacity

- Keep at least `2G` swap when memory is limited.
- Keep JVM heap bounded (`-Xmx512m` is safer than unlimited on small VPS).
- Set MySQL + Java on one host only when total memory is sufficient.

## 3) Daily Health Checks

```bash
systemctl status mysql
systemctl status turns-backend
free -h
df -h
ss -lntp | grep 8080
```

## 4) Fast Incident Checklist

When users report downtime:

```bash
date
systemctl status turns-backend --no-pager
systemctl status mysql --no-pager
journalctl -u turns-backend -n 200 --no-pager
journalctl -u mysql -n 200 --no-pager
grep -i -E "Communications link failure|HikariPool|OutOfMemoryError|Too many open files|No space left" /opt/TURNS/backend/logs/turns-backend.log | tail -n 50
```

## 5) Log Retention

- Keep backend logs at least 30 days.
- Keep access logs enabled (`server.tomcat.accesslog.enabled=true`).
- Keep compressed rolling logs to control disk usage.

