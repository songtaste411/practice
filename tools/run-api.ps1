$ErrorActionPreference = "Stop"

# Java 21 writes logs as UTF-8. A default Windows console is GBK.
# Switch this window to UTF-8 before starting, or Chinese becomes mojibake.
& cmd.exe /c "chcp 65001 >nul"
$utf8 = New-Object System.Text.UTF8Encoding $false
[Console]::InputEncoding = $utf8
[Console]::OutputEncoding = $utf8
$OutputEncoding = $utf8

$jdk = "C:\Program Files\BellSoft\LibericaJDK-21"
if (-not (Test-Path $jdk)) {
    Write-Output "JDK 21 not found at $jdk. Install Temurin or Liberica 21, then edit this script."
    exit 1
}
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;" + $env:Path
# Keep the app process stdout on UTF-8 so it matches the console above.
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
$bundled = "D:\sofeware\ideaIU-2024.3.win\plugins\maven\lib\maven3\bin\mvn.cmd"
if (Test-Path $bundled) {
    $mvn = $bundled
} else {
    $mvn = "mvn"
}
$root = Split-Path -Parent $PSScriptRoot
Set-Location (Join-Path $root "backend")
Write-Output "JAVA_HOME=$env:JAVA_HOME"
Write-Output "Starting API on http://localhost:8080"
& $mvn spring-boot:run
