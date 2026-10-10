$ErrorActionPreference = "Stop"
$jdk = "C:\Program Files\BellSoft\LibericaJDK-21"
if (-not (Test-Path $jdk)) {
    Write-Output "JDK 21 not found at $jdk. Install Temurin or Liberica 21, then edit this script."
    exit 1
}
$env:JAVA_HOME = $jdk
$env:Path = "$jdk\bin;" + $env:Path
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
