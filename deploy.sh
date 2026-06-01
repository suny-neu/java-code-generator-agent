#!/bin/bash
# Java 代码生成 Agent - 阿里云一键部署脚本
# 使用方式：bash deploy.sh

set -e

APP_NAME="java-code-generator-agent"
APP_DIR="/opt/$APP_NAME"

echo "===== 1. 安装 Node.js（如已安装跳过）====="
if command -v node &> /dev/null; then
    echo "Node.js 已安装: $(node --version)"
else
    echo "安装 Node.js 18.x..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo bash -
    sudo apt-get install -y nodejs
    echo "Node.js 安装完成: $(node --version)"
fi

echo ""
echo "===== 2. 部署应用 ====="
sudo mkdir -p $APP_DIR
sudo cp -r . $APP_DIR/
cd $APP_DIR

echo "安装依赖..."
npm install --production

echo ""
echo "===== 3. 配置环境变量 ====="
if [ ! -f .env ]; then
    cp .env.example .env
    echo "⚠️  请编辑 $APP_DIR/.env 填入你的 API Key："
    echo "   vi $APP_DIR/.env"
    echo ""
    echo "   需要填写："
    echo "   ANTHROPIC_API_KEY=你的智谱API_Key"
    echo ""
    read -p "填写完成后按回车继续..."
fi

echo ""
echo "===== 4. 安装 PM2 进程管理 ====="
if command -v pm2 &> /dev/null; then
    echo "PM2 已安装"
else
    sudo npm install -g pm2
fi

echo ""
echo "===== 5. 启动服务 ====="
cd $APP_DIR
pm2 delete $APP_NAME 2>/dev/null || true
pm2 start server.js --name $APP_NAME
pm2 save
pm2 startup

echo ""
echo "===== 部署完成！====="
echo ""
echo "访问地址："
echo "  http://$(curl -s ifconfig.me):3000"
echo ""
echo "常用命令："
echo "  pm2 status          # 查看状态"
echo "  pm2 logs $APP_NAME  # 查看日志"
echo "  pm2 restart $APP_NAME  # 重启"
echo "  pm2 stop $APP_NAME     # 停止"
