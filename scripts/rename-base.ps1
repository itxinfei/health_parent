# Base rename for RuoYi-Vue-Plus 5.6.2 -> xinfei
# 1) replace text: org.dromara -> com.xinfei, DromaraApplication/DromaraServletInitializer
# 2) move src/{main,test}/java/org/dromara  ->  src/{main,test}/java/com/xinfei
# 3) rename Dromara*.java -> Xinfei*.java
# Scope: excludes legacy/, docs/, scripts/, .git/, target/
[CmdletBinding()]
param(
    [string]$Root,
    [switch]$WhatIf
)

$ErrorActionPreference = 'Stop'
if (-not $Root) { $Root = Split-Path -Parent $PSScriptRoot }
if (-not $Root) { $Root = (Get-Location).Path }
$Root = (Resolve-Path -LiteralPath $Root).Path.TrimEnd('\', '/')

$excludeRegex = '(?i)[\\/](legacy|docs|scripts|\.git|target|node_modules|\.idea|\.vscode|out|build)[\\/]'
$exts = @('.java', '.xml', '.yml', '.yaml', '.properties', '.sql', '.json', '.vm', '.ftl', '.imports', '.factories', '.txt', '.tpl')
$extSet = @{}
foreach ($e in $exts) { $extSet[$e] = $true }

$files = @(Get-ChildItem -LiteralPath $Root -Recurse -File -Force -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notmatch $excludeRegex } |
        Where-Object { $extSet.ContainsKey($_.Extension.ToLowerInvariant()) })

$map = [ordered]@{
    'org.dromara'                 = 'com.xinfei'
    'DromaraApplication'          = 'XinfeiApplication'
    'DromaraServletInitializer'   = 'XinfeiServletInitializer'
}

$utf8 = New-Object System.Text.UTF8Encoding($false)
$scanned = 0; $changed = 0
foreach ($f in $files) {
    $scanned++
    $orig = [System.IO.File]::ReadAllText($f.FullName, $utf8)
    $text = $orig
    foreach ($k in $map.Keys) { $text = $text.Replace($k, $map[$k]) }
    if ($text -ne $orig) {
        $changed++
        if (-not $WhatIf) { [System.IO.File]::WriteAllText($f.FullName, $text, $utf8) }
    }
}
Write-Output ("text files scanned={0} changed={1}" -f $scanned, $changed)

$dromaraDirs = @(Get-ChildItem -LiteralPath $Root -Recurse -Directory -Force -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -eq 'dromara' -and $_.Parent.Name -eq 'org' -and $_.FullName -notmatch $excludeRegex })

$moved = 0
foreach ($d in $dromaraDirs) {
    $javaRoot = $d.Parent.Parent.FullName
    $target = Join-Path (Join-Path $javaRoot 'com') 'xinfei'
    if (-not $WhatIf) {
        New-Item -ItemType Directory -Force -Path $target | Out-Null
        Get-ChildItem -LiteralPath $d.FullName -Force | ForEach-Object {
            Move-Item -LiteralPath $_.FullName -Destination $target -Force
        }
        Remove-Item -LiteralPath $d.FullName -Recurse -Force
        $orgDir = $d.Parent.FullName
        if ((Get-ChildItem -LiteralPath $orgDir -Force | Measure-Object).Count -eq 0) {
            Remove-Item -LiteralPath $orgDir -Force
        }
    }
    $moved++
}
Write-Output ("org/dromara dirs moved={0}" -f $moved)

$renamed = 0
Get-ChildItem -LiteralPath $Root -Recurse -File -Force -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -notmatch $excludeRegex -and $_.Name -like 'Dromara*.java' } |
    ForEach-Object {
        $newName = $_.Name -replace '^Dromara', 'Xinfei'
        if (-not $WhatIf) { Rename-Item -LiteralPath $_.FullName -NewName $newName }
        $renamed++
    }
Write-Output ("class files renamed={0}" -f $renamed)
