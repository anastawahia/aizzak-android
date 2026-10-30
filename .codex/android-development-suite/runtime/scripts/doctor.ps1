param([string]$Project = '.', [switch]$Devices)
$ErrorActionPreference = 'Stop'
$arguments = @((Join-Path $PSScriptRoot 'suite.py'), 'doctor', '--project', $Project)
if ($Devices) { $arguments += '--devices' }
& python @arguments
exit $LASTEXITCODE
