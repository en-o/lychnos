# Lychnos（书灯）
> 译文： 油灯 / 夜读之灯 / 古代哲人夜读时唯一的光源

在翻开书之前，先点一盏灯

# 快速开始

## WEB UI
访问地址：http://localhost:3000

### 技术栈

1. 核心框架
- **React 19** - UI 框架
- **TypeScript 5.9** - 类型安全
- **Vite 7** - 构建工具
- **React Router 7** - 路由管理

2. UI & 样式
- **Tailwind CSS 3** - 样式框架
- **Lucide React** - 图标库

3. 网络请求
- **Axios 1.13** - HTTP 客户端
- **vite-plugin-mock 3** - Mock 数据服务
- **mockjs 1.1** - Mock 数据生成

4. 开发工具
- **ESLint 9** - 代码规范
- **PostCSS + Autoprefixer** - CSS 处理

### 安装依赖
```bash
cd webui
npm install
```

### 开发模式
1. 使用 Mock 数据（推荐用于前端独立开发）
> Mock已停止维护,存在数据不一致问题
```bash
npm run dev:mock
```
2.  使用真实后端接口
```bash
npm run dev
```

### 构建生产版本

```bash
## 默认配置在 webui/.env
npm run build

## jar
RUN VITE_API_BASE_URL=/ VITE_USE_MOCK=false VITE_BASE_PATH=/ npm run build
```

## 后端接口

### 技术栈
JDK 17 + Spring Boot 3.4

### 开发模式
```bash
## 源码运行
mvn spring-boot:run 

## idea 运行
直接运行 LychnosApplication.main()
```
### 构建生产版本
```bash
mvn clean package -DskipTests
```


# build
> 构建docker项目，运行[docker-build.bat](docker-build.bat)进行构建镜像
> 运行：` .\docker-build.bat 0.0.2.2 `

## docker
> https://hub.docker.com/r/tannnn/lychnos

`docker run -d -p 1250:1250 -e MYSQL_URL=192.168.1.71:3306 --name lychnos tannnn/lychnos:0.0.2.2`

## docker compose
[docker-compose.yml](docker-compose.yml)

## 环境变量配置

以下是所有可配置的环境变量及其说明：

| 环境变量 | 说明 | 默认值 | 单位 |
|---------|------|--------|------|
| `TZ` | 时区设置 | `Asia/Shanghai` | - |
| `FILE_MAX_SIZE` | 单个文件最大上传大小 | `500MB` | - |
| `FILE_MAX_REQUEST` | 请求最大大小 | `500MB` | - |
| `MYSQL_PWD` | MySQL 数据库密码 | `root` | - |
| `MYSQL_UNM` | MySQL 数据库用户名 | `root` | - |
| `MYSQL_URL` | MySQL 数据库地址 | `localhost:3306` | - |
| `MYSQL_DB` | MySQL 数据库名称 | `db_lychnos` | - |
| `DOC_PASSWORD` | API 文档访问密码 | `tan` | - |
| `DOC_USERNAME` | API 文档访问用户名 | `tan` | - |
| `CONFIG_ENV` | 配置环境（dev/prod） | `prod` | - |
| `IMAGE_STORAGE_PATH` | 图片存储根目录 | `./data/images` | - |
| `AES_SECRET_KEY` | AES 加密密钥（16位字符）<br/>生成方式：`AESUtil.generateSecretKey("Lychnos2026SecretKey")`<br/>⚠️ 生产环境必须修改此密钥 | `kZXQiOjE3MzczOTY3` | - |
| `IMAGE_SIGNATURE_EXPIRY_MS` | 未登录用户图片签名有效期<br/>用于生成时效性签名 URL，允许未登录用户安全访问推荐书籍图片 | `120000`（2分钟） | 毫秒 |

### Docker Compose 示例

```yaml
environment:
  TZ: Asia/Shanghai
  FILE_MAX_SIZE: ${FILE_MAX_SIZE:-500MB}
  FILE_MAX_REQUEST: ${FILE_MAX_REQUEST:-500MB}
  MYSQL_PWD: ${MYSQL_PWD:-root}
  MYSQL_UNM: ${MYSQL_UNM:-root}
  MYSQL_URL: ${MYSQL_URL:-localhost:3306}
  MYSQL_DB: ${MYSQL_DB:-db_lychnos}
  DOC_PASSWORD: ${DOC_PASSWORD:-tan}
  DOC_USERNAME: ${DOC_USERNAME:-tan}
  CONFIG_ENV: ${CONFIG_ENV:-prod}
  IMAGE_STORAGE_PATH: ${IMAGE_STORAGE_PATH:-./data/images}
  AES_SECRET_KEY: ${AES_SECRET_KEY:-kZXQiOjE3MzczOTY3}
  IMAGE_SIGNATURE_EXPIRY_MS: ${IMAGE_SIGNATURE_EXPIRY_MS:-120000}
```

### 安全说明

1. **AES_SECRET_KEY**：
   - 用于加密存储敏感信息（如 AI API Key）
   - 生产环境必须使用自己生成的密钥
   - 生成方法见下方 "AES 加密密钥配置" 章节

2. **IMAGE_SIGNATURE_EXPIRY_MS**：
   - 控制未登录用户访问图片的时效性
   - 使用 HMAC-SHA256 签名确保 URL 不可篡改
   - 建议根据实际需求调整（默认 2 分钟）
   - 时间过长可能存在安全风险，时间过短可能影响用户体验

## sql初始化
> 项目启动会自己创建仓库和表结构，你只需要初始化点数据就好了用来预置的分析

1. 执行 [init_book_analyse.sql](docs/init_book_analyse.sql)
2. 将 [images](data/images) 目录下的书籍相关文件上传到你的文件服务器，上传路径为你设置的`app.image.storage-path`路径



## nginx配置

```nginx

server {
        listen       80;
        server_name  lychnos.tannn.cn;

        #charset koi8-r;
        #access_log  logs/host.access.log  main;
        
		#rewrite ^(.*) https://$server_name$1 permanent;
	 # ✅ ACME 验证路径（使用 ^~ 优先匹配，必须在 rewrite 之前）
    location ^~ /.well-known/acme-challenge/ {
        root /var/www/html;
        try_files $uri =404;
    }

    # ✅ 其他请求重定向到 HTTPS（放在 location / 中）
    location / {
        rewrite ^(.*) https://$server_name$1 permanent;
    }
      
}


server {
    listen   443 ssl;
    server_name  lychnos.tannn.cn;
	
	  # SSL 证书配置
    ssl_certificate      /home/nginxconfig/https/lychnos.tannn.cn_nginx/lychnos.tannn.cn.pem;
    ssl_certificate_key  /home/nginxconfig/https/lychnos.tannn.cn_nginx/lychnos.tannn.cn.key;
	 # SSL 优化配置
	ssl_session_cache    shared:SSL_LYCHNOS:10m;
    ssl_session_timeout  10m;
    ssl_protocols        TLSv1.2 TLSv1.3;  # 只使用安全的 TLS 版本
    ssl_ciphers   'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers on;
	
	
	# 文件上传大小限制
    client_max_body_size 500M;
	
    # Gzip 压缩
    gzip on;
    gzip_disable "msie6";
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript application/json application/javascript application/xml+rss application/rss+xml font/truetype font/opentype application/vnd.ms-fontobject image/svg+xml;

    # 安全 Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    # HSTS（强制 HTTPS，慎用，确保 HTTPS 完全正常后再启用）
    # add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

	
	    # 错误页面
    error_page 404 /404.html;

     location / {
	 
	    # HTTP 方法白名单（拒绝 PROPFIND 等 WebDAV 方法）
        limit_except GET POST PUT DELETE PATCH OPTIONS HEAD {
            deny all;
        }
        proxy_pass http://localhost:1250/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
		proxy_set_header X-Forwarded-Proto $scheme;
		proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
		# HTTP/1.1 支持（WebSocket 等需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
		
       # proxy_ssl_server_name off;
       # proxy_ssl_name $proxy_host;
      
	    # 超时配置（AI 生图需要长时间处理）   # ❌ 这两行配置无效，因为上面用的是 http://
        proxy_connect_timeout 900s;  # 15分钟
        proxy_send_timeout 900s;
        proxy_read_timeout 900s;
		
		
		# 缓存控制
        add_header X-Cache-Status $upstream_cache_status;

        # 禁用缓冲（流式传输，适合大文件和 AI 流式响应）
        proxy_buffering off;
        proxy_request_buffering off;
     }
	 
	   # 静态资源缓存（可选）
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        proxy_pass http://localhost:1250;
        proxy_cache_valid 200 30d;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 配置说明

#### 安全性增强
- **现代 TLS 版本**：仅支持 TLSv1.2 和 TLSv1.3，移除不安全的旧版本
- **安全加密套件**：使用现代化的加密算法配置
- **安全响应头**：防止 XSS、点击劫持等攻击
- **HTTP 方法白名单**：拒绝 PROPFIND 等 WebDAV 扫描方法

#### 性能优化
- **HTTP/2 支持**：提升页面加载性能
- **Gzip 压缩**：压缩文本类资源，减少传输体积
- **静态资源缓存**：为图片、字体、CSS/JS 文件设置 30 天缓存
- **流式传输**：禁用缓冲，支持 AI 流式响应和大文件传输

#### AI 应用优化
- **900 秒超时**：支持 AI 生图等耗时操作（最长 15 分钟）
- **WebSocket 支持**：正确配置 Upgrade 和 Connection headers
- **大文件上传**：支持最大 500MB 文件上传

#### SSL 安全测试
配置后可使用 [SSL Labs](https://www.ssllabs.com/ssltest/) 测试 SSL 安全性评级



# 备注

## 错误码说明
> 接口结构返回的code,并非http state

### 系统级错误码（HTTP Status Code）

| HTTP Code | 业务 Code | 错误名称 | 说明 | 前端处理 |
|-----------|----------|---------|------|---------|
| 401 | TOKEN_ERROR | Token 校验失败 | Token 无效或解析失败 | 清除 token，跳转登录页 |
| 401 | REDIS_EXPIRED_USER | 登录失效 | Redis 中未找到对应登录信息 | 清除 token，跳转登录页 |
| 401 | REDIS_NO_USER | 非法登录 | Token 已被刷新或被其他登录覆盖 | 清除 token，跳转登录页 |
| 402 | SYS_AUTHORIZED_PAST | 授权过期 | 系统授权已过期 | 提示用户联系管理员 |
| 403 | UNAUTHENTICATED | 系统未授权 | 当前用户无访问权限 | 提示用户无权限 |
| 403 | UNAUTHENTICATED_PLATFORM | 非法令牌访问 | Token 与访问端不匹配 | 清除 token，跳转登录页 |

### 业务级错误码（BusinessErrorCode）

| 业务 Code | 错误名称 | 说明 | 前端处理 |
|----------|---------|------|---------|
| 1001 | BOOK_ALREADY_ANALYZED | 书籍已分析过 | 不显示 toast，跳转到历史记录页面 |
| 1002 | MODEL_NOT_CONFIGURED | 用户未配置 AI 模型 | 提示用户未配置模型，1.5秒后自动跳转到 AI 模型设置页面 |


## AES 加密密钥配置

为了安全地存储敏感信息（如 AI API Key），系统使用 AES 加密算法。需要在 `application.yaml` 中配置加密密钥。

### 生成密钥

可以使用以下两种方式生成密钥：

#### 方式1：使用代码生成（推荐）

在 Java 代码中调用：
```java
// 使用任意字符串作为种子生成 16 字节密钥
String secretKey = AESUtil.generateSecretKey("Lychnos2026SecretKey");
System.out.println(secretKey);  // 输出: kZXQiOjE3MzczOTY3

// 或者直接打印配置示例
AESUtil.printConfigExample("Lychnos2026SecretKey");
```

#### 方式2：使用环境变量

在 Docker 或生产环境中，可以通过环境变量配置：
```bash
docker run -e AES_SECRET_KEY=your-secret-key ...
```

### 配置文件

在 `application.yaml` 中添加配置：
```yaml
app:
  security:
    # AES 加密密钥（用于 API Key 等敏感信息加密存储）
    # 生成方式：AESUtil.generateSecretKey("YourSeedString")
    # 注意：生产环境请务必修改此密钥，并妥善保管
    aes-secret-key: ${AES_SECRET_KEY:kZXQiOjE3MzczOTY3}
```

### 注意事项

1. **生产环境必须修改**：默认密钥仅用于开发测试，生产环境必须使用自己生成的密钥
2. **妥善保管密钥**：密钥泄露会导致所有加密数据不安全
3. **定期更换密钥**：建议定期更换密钥以提高安全性
4. **使用环境变量**：生产环境建议通过环境变量 `AES_SECRET_KEY` 配置，不要硬编码在配置文件中



## poster_url 格式说明

`tb_book_analyse` 表中的 `poster_url` 字段采用统一格式：**协议:鉴权:路径**

### 格式规范

```
协议代码:鉴权标识:访问路径
```

#### 协议代码

| 代码 | 说明 | 备注 |
|------|------|------|
| `h` | HTTP/HTTPS | 公共或私有 HTTP(S) 资源 |
| `ali` | 阿里云 OSS | 需配置 endpoint、bucket 等 |
| `qiniu` | 七牛云 | 支持公开 URL 或私有空间 |
| `s3` | AWS S3 | 需配置 region、bucket 等 |
| `f` | FTP | FTP 文件服务器 |
| `l` | 本地存储 | 服务器本地文件系统 |

#### 鉴权标识

| 值 | 说明 | 处理方式 |
|----|------|---------|
| `0` | 无需鉴权 | 公开访问，前端可直接访问完整 URL |
| `1` | 需要鉴权 | 需配置凭证，通过后端代理访问 |

### 格式示例

#### 1. HTTP/HTTPS
```
# 无鉴权（公开 CDN、图床）
h:0:https://images.unsplash.com/photo-1419242902214-272b3f66ee7a?w=800

# 有鉴权（需要 Token 或签名的私有 CDN）
h:1:https://private-cdn.com/books/三体.png
```

#### 2. 阿里云 OSS
```
# 需鉴权（标准用法）
ali:1:/my-bucket/books/三体.png

# 无鉴权（公开 bucket）
ali:0:https://my-bucket.oss-cn-hangzhou.aliyuncs.com/books/三体.png
```

#### 3. 七牛云
```
# 无鉴权（公开空间）
qiniu:0:https://cdn.qiniu.com/books/三体.png

# 有鉴权（私有空间）
qiniu:1:/my-space/books/三体.png
```

#### 4. AWS S3
```
# 需鉴权
s3:1:/my-bucket/books/三体.png
```

#### 5. FTP
```
# 无鉴权（匿名访问）
f:0:ftp://192.168.1.100/public/books/三体.png

# 有鉴权（需用户名密码）
f:1:ftp://192.168.1.100/private/books/三体.png
```

#### 6. 本地存储
```
# 无鉴权（公开目录）
l:0:/public/20240115/三体.png

# 有鉴权（私有目录，AI 生成图片）
l:1:/20240115/三体.png
```

### 凭证配置

需要鉴权（标识为 `1`）的协议需在 `application.yaml` 中配置凭证：

```yaml
app:
  image:
    storage-path: ./data/images  # 本地存储根目录
    credentials:
      # 阿里云 OSS
      ali:
        endpoint: https://oss-cn-hangzhou.aliyuncs.com
        access-key-id: ${ALI_OSS_ACCESS_KEY_ID}
        access-key-secret: ${ALI_OSS_ACCESS_KEY_SECRET}

      # 七牛云
      qiniu:
        access-key: ${QINIU_ACCESS_KEY}
        secret-key: ${QINIU_SECRET_KEY}

      # AWS S3
      s3:
        region: us-east-1
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}

      # FTP
      f:
        host: 192.168.1.100
        port: 21
        username: ${FTP_USERNAME}
        password: ${FTP_PASSWORD}

      # 有鉴权的 HTTP（需要 Bearer Token）
      h:
        auth-type: bearer  # 或 basic
        token: ${HTTP_AUTH_TOKEN}
```

### 访问处理逻辑

#### 前端（webui/src/utils/imageUrl.ts）
```typescript
export function getImageUrl(posterUrl: string): string {
  const [protocol, auth, path] = posterUrl.split(':', 3);

  // 无鉴权 - 直接访问完整 URL
  if (auth === '0') {
    return path;  // path 本身就是完整 URL
  }

  // 有鉴权 - 通过后端代理
  return `/api/image?path=${encodeURIComponent(posterUrl)}`;
}
```

#### 后端（ImageStorageService）
```java
public InputStream getImage(String posterUrl) {
    String[] parts = posterUrl.split(":", 3);
    String protocol = parts[0];
    String auth = parts[1];
    String path = parts[2];

    switch (protocol) {
        case "h" -> return getHttpImage(auth, path);
        case "ali" -> return getAliOssImage(auth, path);
        case "qiniu" -> return getQiniuImage(auth, path);
        case "s3" -> return getS3Image(auth, path);
        case "f" -> return getFtpImage(auth, path);
        case "l" -> return getLocalImage(auth, path);
        default -> throw new IllegalArgumentException("Unsupported protocol: " + protocol);
    }
}
```


## 注意事项

1. **Mock 数据仅在开发环境生效**：生产构建会自动禁用 Mock (ps: Mock已停止维护,存在数据不一致问题)
3. **测试两种模式**：开发时建议在 Mock 和真实接口两种模式下都测试功能
4. **前端环境变量修改后需重启**：修改 `.env` 文件后需要重启开发服务器
5. 当前项目以书名作为唯一，如果同名不同内容以先上传为准，后上传的会提示已分析过
