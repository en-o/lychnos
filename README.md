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
> 运行：` .\docker-build.bat 0.0.2 `

## docker
> https://hub.docker.com/r/tannnn/lychnos

`docker run -d -p 1250:1250 -e MYSQL_URL=192.168.1.71:3306 --name lychnos tannnn/lychnos:0.0.2`

## docker compose
[docker-compose.yml](docker-compose.yml)


## nginx配置
```nginx configuration

server {
        listen       80;
        server_name  lychnos.xx.cn;

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
    ssl_certificate      /home/nginxconfig/https/lychnos.xx.cn_nginx/lychnos.xx.cn.pem;
    ssl_certificate_key  /home/nginxconfig/https/lychnos.xx.cn_nginx/lychnos.xx.cn.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout  5m;
    client_max_body_size 500M;
    # SSL Settings
    ssl_ciphers  HIGH:!aNULL:!MD5;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    ssl_prefer_server_ciphers   on;
	error_page 404 /404.html;
    #  Gzip Settings
    gzip on;
    gzip_disable "msie6";

     location / {
        proxy_pass http://localhost:1250/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $http_connection;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        add_header X-Cache $upstream_cache_status;
        add_header Cache-Control no-cache;
        proxy_ssl_server_name off;
        proxy_ssl_name $proxy_host;
     }
}
```


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
