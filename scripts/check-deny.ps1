<#
.SYNOPSIS
  DENY 约束检查（对应 docs/requirements/15-AI开发约束.md §5）

.DESCRIPTION
  扫描代码，检测禁止模式：System.out、java.util.Date、SQL 字符串拼接、硬编码密钥、
  冻结表 ALTER、@PreAuthorize、@Scheduled、org.dromara 残留等。
  默认排除旧 health_* 模块；加 -IncludeLegacy 可一并扫描。

.PARAMETER Root
  扫描根目录，默认为仓库根目录。

.PARAMETER IncludeLegacy
  同时扫描旧 health_* 模块（默认排除）。

.PARAMETER IncludeFramework
  全量扫描底座 vendored 代码（xfjk-common/*、xfjk-system/*、xfjk-demo/* 等）。
  默认只扫业务代码白名单：xfjk-modules/xfjk-health-*、xfjk-web|app 的 src/mock、
  各级 .env 文件、docs/sql。框架代码含启动横幅 System.out、Entity 用 java.util.Date
  等既有写法，属铁律 9 豁免与「禁止顺手重构框架」范围，纳入默认扫描会恒报 ERROR。

.PARAMETER Format
  输出格式：table（默认）或 json。

.PARAMETER MaxPerRule
  每条规则最多输出多少条，默认 100。

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\scripts\check-deny.ps1

.EXAMPLE
  powershell -ExecutionPolicy Bypass -File .\scripts\check-deny.ps1 -IncludeLegacy -Format json
#>
[CmdletBinding()]
param(
    [string]$Root,
    [switch]$IncludeLegacy,
    [switch]$IncludeFramework,
    [ValidateSet('table', 'json')][string]$Format = 'table',
    [int]$MaxPerRule = 100
)

$ErrorActionPreference = 'Stop'

if (-not $Root) { $Root = Split-Path -Parent $PSScriptRoot }
if (-not $Root) { $Root = (Get-Location).Path }

if (-not (Test-Path -LiteralPath $Root)) {
    Write-Error "扫描根目录不存在: $Root"
    exit 2
}
$Root = (Resolve-Path -LiteralPath $Root).Path.TrimEnd('\', '/')

$excludeDirs = @(
    '.git', 'target', 'build', 'dist', 'out', 'node_modules',
    'docs', 'manage', 'weixin', 'scripts'
)
if (-not $IncludeLegacy) {
    $excludeDirs += @(
        'legacy',
        'health_backend', 'health_common', 'health_interface',
        'health_job', 'health_mobile', 'health_service_provider'
    )
}
$excludeRegex = '(?i)[\\/](' + (($excludeDirs | ForEach-Object { [regex]::Escape($_) }) -join '|') + ')[\\/]'

# docs 整体排除，但 docs/sql 是唯一受 DENY-008（冻结表 ALTER）约束的产物，必须扫。
$alwaysScan = '(?i)[\\/]docs[\\/]sql[\\/]'

# 业务代码白名单（默认扫描范围）。
# 框架 vendored 代码（xfjk-common/*、xfjk-system/*、xfjk-demo/*、xfjk-generator/* 等）
# 天然违反 DENY-001/003（启动横幅 System.out、BaseEntity/Entity 用 java.util.Date），
# 而 §1 铁律 9 明文「Entity 映射除外」、且禁止顺手重构框架代码 ⇒ 默认不扫，
# 需要全量审计时加 -IncludeFramework。
$businessRegex = '(?i)([\\/](xfjk-modules[\\/]xfjk-health-[a-z0-9_-]+|xfjk-web[\\/](src|mock)|xfjk-app[\\/](src|mock))[\\/])|([\\/]\.env[\w.]*$)'

$exts = @('.java', '.xml', '.yml', '.yaml', '.properties', '.ts', '.vue', '.js', '.sql')

# Vite 的 .env* 文件无常规扩展名（.env.production 的 Extension 是 .production），
# 单独按文件名纳入——DENY-020 依赖它检查前端加密开关。
$files = Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { ($exts -contains $_.Extension.ToLowerInvariant()) -or ($_.Name -match '(?i)^\.env') } |
    Where-Object { $_.FullName -notmatch $excludeRegex -or $_.FullName -match $alwaysScan } |
    Where-Object { $IncludeFramework -or $_.FullName -match $businessRegex } |
    Where-Object { $_.Name -notmatch '(?i)\.min\.(js|css)$' } |
    Where-Object { $_.Name -ine '.flattened-pom.xml' }

# 规则：Name / Pattern / Ext（空=全部类型） / Severity / Message
#      Scope（可选：路径必须匹配的正则）/ NotScope（可选：路径不得匹配的正则）
#      —— 前端规则一律 Scope 到工程化 src 树，避免误报仍待废弃的 xfjk-web 静态页存量。
$rules = @(
    @{ Name = 'DENY-001'; Pattern = '\bSystem\.(out|err)\b'; Ext = @('.java'); Severity = 'ERROR'; Message = '禁止 System.out/err，改用 SLF4J' }
    @{ Name = 'DENY-002'; Pattern = '\.printStackTrace\s*\('; Ext = @('.java'); Severity = 'ERROR'; Message = '禁止 printStackTrace，记录日志' }
    @{ Name = 'DENY-003'; Pattern = 'java\.util\.Date|\bnew\s+Date\s*\('; Ext = @('.java'); Severity = 'ERROR'; Message = '禁止 java.util.Date，改用 java.time' }
    @{ Name = 'DENY-004'; Pattern = '(?i)"[^"]*\b(select|insert|update|delete)\b[^"]*"\s*\+'; Ext = @('.java'); Severity = 'ERROR'; Message = '疑似字符串拼接 SQL，改用参数绑定' }
    @{ Name = 'DENY-005'; Pattern = '\$\{'; Ext = @('.xml'); Severity = 'ERROR'; Message = 'MyBatis XML 禁止 ${} 拼接（SQL 注入风险），改用 #{}' }
    @{ Name = 'DENY-006'; Pattern = '(?i)\b(password|passwd|pwd|secret|access[_-]?key|secret[_-]?key|token)\b\s*[:=]\s*["''][^"''\s]{3,}["'']'; Severity = 'ERROR'; Message = '疑似硬编码密钥/密码，改走环境变量' }
    @{ Name = 'DENY-007'; Pattern = '-----BEGIN\s+(RSA\s+|EC\s+)?PRIVATE\s+KEY-----'; Severity = 'ERROR'; Message = '禁止提交私钥' }
    @{ Name = 'DENY-008'; Pattern = '(?i)ALTER\s+TABLE\s+`?t_'; Ext = @('.sql'); Severity = 'ERROR'; Message = '禁止 ALTER 业务表（只允许新增表）' }
    @{ Name = 'DENY-009'; Pattern = '@PreAuthorize'; Ext = @('.java'); Severity = 'ERROR'; Message = '本项目用 Sa-Token，禁止 @PreAuthorize' }
    @{ Name = 'DENY-010'; Pattern = '@Scheduled'; Ext = @('.java'); Severity = 'ERROR'; Message = '定时任务用 SnailJob，禁止 @Scheduled' }
    @{ Name = 'DENY-011'; Pattern = '\borg\.dromara\b'; Ext = @('.java', '.xml'); Severity = 'ERROR'; Message = '包名应整体改为 com.xinfei，禁止 org.dromara 残留' }
    @{ Name = 'DENY-012'; Pattern = '<artifactId>\s*ruoyi-'; Ext = @('.xml'); Severity = 'WARN'; Message = 'artifact 前缀应为 xfjk-，检查 ruoyi- 残留' }
    @{ Name = 'DENY-013'; Pattern = '\bconsole\.(log|debug|info)\s*\('; Ext = @('.ts', '.vue', '.js'); Severity = 'WARN'; Message = '生产代码避免 console 输出' }
    @{ Name = 'DENY-014'; Pattern = '(?i)@CrossOrigin'; Ext = @('.java'); Severity = 'WARN'; Message = '统一走全局 CORS 配置，避免 @CrossOrigin 散落' }
    # ---- 前端（plus-ui 工程化 SPA，17 决策 47；约定见 03 §9.1、15 §5.1）----
    @{ Name = 'DENY-015'; Pattern = 'v-hasRole(?!s)'; Ext = @('.vue', '.ts', '.js'); Severity = 'ERROR';
       Scope = 'xfjk-web[\\/](src|mock)';
       Message = '指令注册的是复数 hasRoles，写单数 v-hasRole 不报错但被 Vue 忽略 = 按钮全显示的静默越权；统一用 v-hasPermi' }
    @{ Name = 'DENY-016'; Pattern = '(?i)^\s*import\s+axios\b|from\s+["'']axios["'']|axios\.create\s*\('; Ext = @('.vue', '.ts', '.js'); Severity = 'ERROR';
       Scope = 'xfjk-(web|app)[\\/](src|mock)'; NotScope = 'utils[\\/]request\.ts$';
       Message = '禁止页面/业务代码自建 axios 实例，统一走框架唯一的 src/utils/request.ts' }
    @{ Name = 'DENY-017'; Pattern = '(?i)\bnew\s+Vue\s*\(|from\s+["'']element-ui["'']|\bELEMENT\.'; Ext = @('.vue', '.ts', '.js'); Severity = 'ERROR';
       Scope = 'xfjk-(web|app)[\\/](src|mock)';
       Message = 'Vue 2 / Element-UI 2 形态已被决策 47 废弃（旧静态页参照走 git 历史，不落工作区）' }
    @{ Name = 'DENY-018'; Pattern = '#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})\b'; Ext = @('.vue'); Severity = 'WARN';
       Scope = 'xfjk-web[\\/]src[\\/](views|api)[\\/]health';
       Message = '业务页禁止硬编码色值，引用 19 §2.1 令牌（plus-ui 主题变量 / uno.config.ts）' }
    @{ Name = 'DENY-019'; Pattern = '[''"][^''"]*\.do(?:\?|[''"])'; Ext = @('.vue', '.ts', '.js'); Severity = 'ERROR';
       Scope = 'xfjk-(web|app)[\\/](src|mock)';
       Message = '禁止调用原系统 .do 存量接口，新接口一律 /health/**、/app/**、/system/**' }
    @{ Name = 'DENY-020'; Pattern = '(?i)^\s*VITE_APP_ENCRYPT\s*=\s*true\s*$'; Severity = 'ERROR';
       Scope = '[\\/]\.env';
       Message = '后端 api-decrypt.enabled=false 时前端必须 VITE_APP_ENCRYPT=false，否则 /auth/login 403（02 §5、17 决策 47）' }
)

$findings = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    $ext = $file.Extension.ToLowerInvariant()
    $applicable = @($rules | Where-Object {
        (($null -eq $_.Ext) -or ($_.Ext -contains $ext)) -and
        (($null -eq $_.Scope) -or ($file.FullName -match $_.Scope)) -and
        (($null -eq $_.NotScope) -or ($file.FullName -notmatch $_.NotScope))
    })
    if ($applicable.Count -eq 0) { continue }

    $lineNo = 0
    try {
        foreach ($line in [System.IO.File]::ReadLines($file.FullName, [System.Text.Encoding]::UTF8)) {
            $lineNo++
            foreach ($rule in $applicable) {
                # Maven pom 的属性占位符 ${...} 不是 SQL 拼接，排除
                if ($rule.Name -eq 'DENY-005' -and $file.Name -ieq 'pom.xml') { continue }
                # logback 配置中的 ${} 为日志变量占位符，非 SQL
                if ($rule.Name -eq 'DENY-005' -and $file.Name -match '(?i)^logback.*\.xml$') { continue }
                # MyBatis-Plus 条件构造器 ${ew.xxx} 为框架内置安全占位
                if ($rule.Name -eq 'DENY-005' -and $line -match '\$\{\s*ew\.') { continue }
                # §1 铁律 9：java.util.Date 在 Entity 映射层豁免（domain/ 根下的实体类）
                if ($rule.Name -eq 'DENY-003' -and $file.FullName -match '[\\/]domain[\\/][^\\/]+\.java$') { continue }
                # org.dromara.sms4j 为第三方短信 SDK 包名，不在改名范围
                if ($rule.Name -eq 'DENY-011' -and $line -match '\borg\.dromara\.sms4j\b') { continue }
                if ($line -match $rule.Pattern) {
                    $findings.Add([pscustomobject]@{
                            Severity = $rule.Severity
                            Rule     = $rule.Name
                            File     = $file.FullName.Substring($Root.Length).TrimStart([char[]]@('\', '/'))
                            Line     = $lineNo
                            Message  = $rule.Message
                            Text     = ($line.Trim() -replace '\s+', ' ')
                        })
                    break
                }
            }
        }
    }
    catch {
        $findings.Add([pscustomobject]@{
                Severity = 'WARN'
                Rule     = 'SCAN-ERR'
                File     = $file.FullName
                Line     = 0
                Message  = '文件读取失败（编码/权限）'
                Text     = $_.Exception.Message
            })
    }
}

$errorCount = @($findings | Where-Object { $_.Severity -eq 'ERROR' }).Count
$warnCount = @($findings | Where-Object { $_.Severity -eq 'WARN' }).Count

if ($Format -eq 'json') {
    $findings | Sort-Object Severity, Rule, File, Line | ConvertTo-Json -Depth 4
}
elseif ($findings.Count -gt 0) {
    foreach ($group in ($findings | Sort-Object Rule | Group-Object Rule)) {
        $group.Group | Select-Object -First $MaxPerRule |
            Format-Table -AutoSize Severity, Rule,
                @{ n = 'Location'; e = { "$($_.File):$($_.Line)" } },
                Message, Text
        if ($group.Count -gt $MaxPerRule) {
            Write-Host ("  ... {0} 规则另有 {1} 条未显示" -f $group.Name, ($group.Count - $MaxPerRule)) -ForegroundColor DarkYellow
        }
    }
    Write-Host ""
    Write-Host ("发现 {0} 处违规（ERROR {1}，WARN {2}）" -f $findings.Count, $errorCount, $warnCount) -ForegroundColor Yellow
}
else {
    Write-Host "DENY 检查通过：未发现违规。" -ForegroundColor Green
}

if ($errorCount -gt 0) { exit 1 } else { exit 0 }
