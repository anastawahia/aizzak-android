param(
    [string]$Distribution = 'Ubuntu-24.04',
    [string]$BackendPath = '/home/AIZZAK',
    [string]$AdbPath = 'adb',
    [switch]$SkipStackStart
)

$ErrorActionPreference = 'Stop'
if (-not $SkipStackStart) {
    & wsl.exe --distribution $Distribution --cd $BackendPath --exec docker compose up -d --scale app=1 nginx
    if ($LASTEXITCODE -ne 0) { throw 'The local API stack did not start.' }
}

$taskReadyDeadline = [DateTime]::UtcNow.AddSeconds(60)
do {
    $taskReady = $true
    foreach ($taskEndpoint in @('http://127.0.0.1/health', 'http://127.0.0.1/health/ready')) {
        try {
            $taskResponse = Invoke-WebRequest -Uri $taskEndpoint -TimeoutSec 3 -MaximumRedirection 0
            if ($taskResponse.StatusCode -ne 200) { $taskReady = $false }
        } catch {
            # Compose may return before the app has completed startup.
            $taskReady = $false
        }
        if (-not $taskReady) { break }
    }
    if ($taskReady) { break }
    if ([DateTime]::UtcNow -ge $taskReadyDeadline) {
        throw 'The local API did not become healthy and ready within 60 seconds.'
    }
    Start-Sleep -Seconds 1
} while ($true)

& $AdbPath get-state
if ($LASTEXITCODE -ne 0) { throw 'Connect and authorize one Android device through USB.' }
& $AdbPath reverse tcp:8080 tcp:80
if ($LASTEXITCODE -ne 0) { throw 'Could not forward the Android API connection.' }
Write-Output 'Local API ready for the debug app at http://127.0.0.1:8080/api/v1/.'
