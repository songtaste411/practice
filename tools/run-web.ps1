$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Set-Location (Join-Path $root "frontend")
if (-not (Test-Path "node_modules")) {
    npm config set registry https://registry.npmmirror.com
    npm install
}
Write-Output "Starting web on http://localhost:5173"
npm run dev
