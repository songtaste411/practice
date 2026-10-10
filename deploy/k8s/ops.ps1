param(
    [ValidateSet("up", "down", "status")]
    [string]$Action = "status"
)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Output "kubectl not found. Install a local cluster first, then come back."
    exit 1
}

switch ($Action) {
    "up" {
        kubectl apply -f (Join-Path $here "namespace.yaml")
        kubectl apply -f (Join-Path $here "configmap.yaml")
        kubectl apply -f (Join-Path $here "postgres.yaml")
        kubectl apply -f (Join-Path $here "redis.yaml")
        kubectl apply -f (Join-Path $here "api.yaml")
        kubectl apply -f (Join-Path $here "web.yaml")
        Write-Output "Applied. Images must already exist on the cluster: prod-clinic-api:latest and prod-clinic-web:latest"
    }
    "down" {
        kubectl delete namespace prod-clinic --ignore-not-found
    }
    "status" {
        kubectl get all -n prod-clinic
    }
}
