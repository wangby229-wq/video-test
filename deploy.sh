#!/bin/bash

# ============================================
# 企业微信会议录音转写系统 - 一键部署脚本
# 适用于 CentOS 7
# ============================================

set -e

# 配置变量
APP_NAME="meeting-transcribe-1.0.0.jar"
APP_DIR="/opt/wechat_meeting"
APP_PORT="8090"
LOG_DIR="/var/log/weeting_meeting"
MYSQL_DB="meeting_recording"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 打印带颜色的信息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        print_error "请使用 root 用户运行此脚本"
        exit 1
    fi
}

# 检查Java环境
check_java() {
    print_info "检查Java环境..."
    if ! command -v java &> /dev/null; then
        print_error "Java未安装，正在安装..."
        yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
    fi
    java -version
}

# 创建目录
create_dirs() {
    print_info "创建目录结构..."
    mkdir -p $APP_DIR
    mkdir -p $APP_DIR/uploads
    mkdir -p $LOG_DIR
    chown -R root:root $APP_DIR
    chmod -R 755 $APP_DIR
    chmod 777 $APP_DIR/uploads
}

# 检查MySQL
check_mysql() {
    print_info "检查MySQL服务..."
    if ! systemctl is-active --quiet mysqld; then
        print_warn "MySQL服务未启动，正在启动..."
        systemctl start mysqld
        systemctl enable mysqld
    fi
    print_info "MySQL服务运行正常"
}

# 初始化数据库
init_database() {
    print_info "初始化数据库..."
    if [ -f "$APP_DIR/database_init.sql" ]; then
        mysql -u root << EOF
CREATE DATABASE IF NOT EXISTS $MYSQL_DB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE $MYSQL_DB;
EOF
        print_info "数据库 $MYSQL_DB 创建成功"
    else
        print_warn "database_init.sql 文件未找到，跳过数据库初始化"
    fi
}

# 配置Supervisor
config_supervisor() {
    print_info "配置Supervisor..."
    cat > /etc/supervisord.d/meeting-server.ini << EOF
[program:meeting-server]
directory=$APP_DIR
command=/usr/bin/java -Xms256m -Xmx512m -jar $APP_NAME --spring.config.location=$APP_DIR/application.yml
numprocs=1
autostart=true
autorestart=true
startsecs=10
startretries=3
exitcodes=0
stopsignal=TERM
stopwaitsecs=60
user=root
redirect_stderr=true
stdout_logfile=$LOG_DIR/meeting-server.log
stdout_logfile_maxbytes=50MB
stdout_logfile_backups=10
environment=JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk"
EOF
    print_info "Supervisor配置完成"
}

# 启动服务
start_service() {
    print_info "启动服务..."
    supervisorctl reread
    supervisorctl update
    supervisorctl start meeting-server

    print_info "等待服务启动..."
    sleep 10

    if supervisorctl status meeting-server | grep -q "RUNNING"; then
        print_info "服务启动成功！"
    else
        print_error "服务启动失败，请检查日志: $LOG_DIR/meeting-server.log"
        exit 1
    fi
}

# 重启服务
restart_service() {
    print_info "重启服务..."
    supervisorctl restart meeting-server

    print_info "等待服务重启..."
    sleep 10

    if supervisorctl status meeting-server | grep -q "RUNNING"; then
        print_info "服务重启成功！"
    else
        print_error "服务重启失败，请检查日志: $LOG_DIR/meeting-server.log"
        exit 1
    fi
}

# 重新加载supervisor（重启所有服务，不推荐）
reload_supervisor() {
    print_warn "此操作会重启所有supervisor管理的服务..."
    read -p "确认继续？(y/n): " confirm
    if [ "$confirm" = "y" ]; then
        print_info "重新加载supervisor..."
        supervisorctl reload
        sleep 5
        supervisorctl start meeting-server
        show_status
    else
        print_info "操作已取消"
    fi
}

# 检查端口
check_port() {
    print_info "检查端口 $APP_PORT ..."
    if netstat -tlnp | grep -q ":$APP_PORT "; then
        print_error "端口 $APP_PORT 已被占用！"
        netstat -tlnp | grep ":$APP_PORT "
        exit 1
    else
        print_info "端口 $APP_PORT 可用"
    fi
}

# 显示服务状态
show_status() {
    print_info "=========================================="
    print_info "服务状态："
    supervisorctl status meeting-server
    print_info "=========================================="
    print_info "访问地址：http://localhost:$APP_PORT"
    print_info "日志文件：$LOG_DIR/meeting-server.log"
    print_info "=========================================="
}

# 主函数
main() {
    print_info "=========================================="
    print_info "企业微信会议录音转写系统部署脚本"
    print_info "=========================================="

    check_root
    check_java
    create_dirs
    check_mysql
    init_database
    check_port
    config_supervisor
    start_service
    show_status

    print_info "部署完成！"
}

# 执行主函数
main
