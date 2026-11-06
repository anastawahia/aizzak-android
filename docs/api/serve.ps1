param(
    [int]$Port = 8088
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "Serving AIZZAK API documentation from $root"
Write-Host "ReDoc:  http://localhost:$Port/index.html"
Write-Host "Swagger: http://localhost:$Port/swagger.html"

Push-Location $root
try {
    if (Get-Command py -ErrorAction SilentlyContinue) {
        py -m http.server $Port
    } elseif (Get-Command python -ErrorAction SilentlyContinue) {
        python -m http.server $Port
    } elseif (Get-Command wsl.exe -ErrorAction SilentlyContinue) {
        $drive = [char]::ToLowerInvariant($root[0])
        $rest = $root.Substring(2).Replace("\", "/")
        $wslRoot = "/mnt/$drive$rest"
        wsl.exe -d Ubuntu-24.04 -- python3 -m http.server $Port --directory $wslRoot
    } else {
        throw "Python or WSL was not found. Install Python or use any static HTTP server in this folder."
    }
} finally {
    Pop-Location
}
