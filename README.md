# 随便看 — Android TV 影视搜索播放器

在当贝 TV Box 上通过搜索引擎搜索影视资源并播放。

## 如何编译安装到当贝盒子

### 第一步：推送代码到 GitHub（一次性，5 分钟）

1. 浏览器打开 https://github.com → 登录你的 GitHub 账号（没有就注册一个）
2. 点击右上角 **+** → **New repository**
3. Repository name 填 `suibiankan` → **Create repository**（保持 Public）
4. 创建后 GitHub 会显示推送命令，在**本机命令行**中执行：

```bash
cd "C:\Users\许\Desktop\新建文件夹 (3)"
git add -A
git commit -m "随便看 TV App 初始版本"
git branch -M main
git remote add origin https://github.com/你的用户名/suibiankan.git
git push -u origin main
```

### 第二步：GitHub Actions 自动编译（全自动，约 5 分钟）

1. 代码推送后，打开 GitHub 仓库页面
2. 点击 **Actions** 标签页
3. 你会看到 **Build APK** 正在运行（黄色圆点）
4. 等待变绿色 ✓ → 点击进入
5. 页面底部 **Artifacts** 区域 → 点击 **随便看-debug-apk** 下载 APK

> 以后每次修改代码再推送，都会自动编译新 APK。也可以手动触发：Actions → Build APK → Run workflow。

### 第三步：当贝快传安装到盒子（1 分钟）

1. 确保当贝盒子和电脑连同一个 WiFi
2. 当贝盒子打开 **当贝市场** → 搜索 **当贝快传** → 安装并打开
3. 屏幕会显示一个网址（如 `192.168.1.100:12345`）
4. 电脑浏览器打开该网址 → 上传刚下载的 APK
5. 盒子端接收后自动弹出安装 → 安装完成
