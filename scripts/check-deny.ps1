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

$exts = @('.java', '.xml', '.yml', '.yaml', '.properties', '.ts', '.vue', '.js', '.sql')

$files = Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object { $exts -contains $_.Extension.ToLowerInvariant() } |
    Where-Object { $_.FullName -notmatch $excludeRegex } |
    Where-Object { $_.Name -notmatch '(?i)\.min\.(js|css)$' } |
    Where-Object { $_.Name -ine '.flattened-pom.xml' }

# 规则：Name / Pattern / Ext（空=全部类型） / Severity / Message
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
)

$findings = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    $ext = $file.Extension.ToLowerInvariant()
    $applicable = @($rules | Where-Object { ($null -eq $_.Ext) -or ($_.Ext -contains $ext) })
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
