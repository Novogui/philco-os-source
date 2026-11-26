[CmdletBinding()]
param([string] $ProjectRoot = 'C:\PhilcoOS')

$ErrorActionPreference = 'Stop'
$ProjectRoot = [IO.Path]::GetFullPath($ProjectRoot)
$candidate = Join-Path $ProjectRoot 'output\releases\philco-home-visual-polish-EB256CA3.apk'
$sourceBuild = Join-Path $ProjectRoot 'source\philco-home\build\outputs\apk\release\philco-home-release.apk'
$stageApk = Join-Path $ProjectRoot 'build\system_stage_slim\app\PhilcoUltra777\PhilcoUltra777.apk'
$statePath = Join-Path $ProjectRoot 'analysis\slim_stage_state.json'
$reportPath = Join-Path $ProjectRoot 'analysis\slim_stage_report.md'
$a57 = Join-Path $ProjectRoot 'output\releases\philco-home-A57-13BADB2D.apk'
$candidateHash = 'EB256CA3E7A7131E212EA3CF9C1700581A4AC3EDEF95438582ECD54EB38628E2'
$a57Hash = '13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676'

foreach ($file in @($candidate, $sourceBuild, $stageApk, $statePath, $reportPath, $a57)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { throw "Required file missing: $file" }
}
if ((Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash -ne $candidateHash) { throw 'EB256 archive hash mismatch.' }
if ((Get-FileHash -LiteralPath $sourceBuild -Algorithm SHA256).Hash -ne $candidateHash) { throw 'Current release build is not the validated EB256 candidate.' }
if ((Get-FileHash -LiteralPath $a57 -Algorithm SHA256).Hash -ne $a57Hash) { throw 'A57 rollback archive hash mismatch.' }
$stageHash = (Get-FileHash -LiteralPath $stageApk -Algorithm SHA256).Hash
if ($stageHash -notin @($a57Hash, $candidateHash)) { throw "Unexpected stage APK; refusing replacement: $stageHash" }

$state = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
$expectedTarget = [IO.Path]::GetFullPath((Join-Path $ProjectRoot 'build\system_stage_slim'))
if ([IO.Path]::GetFullPath($state.target) -ne $expectedTarget) { throw 'State target is not build\system_stage_slim.' }

$temp = Join-Path (Split-Path -Parent $stageApk) 'PhilcoUltra777.EB256.tmp.apk'
if (Test-Path -LiteralPath $temp) {
    if ((Get-FileHash -LiteralPath $temp -Algorithm SHA256).Hash -ne $candidateHash) { throw "Unexpected promotion temp exists: $temp" }
} else { Copy-Item -LiteralPath $candidate -Destination $temp }
if ((Get-FileHash -LiteralPath $temp -Algorithm SHA256).Hash -ne $candidateHash) { throw 'Temporary EB256 copy hash mismatch.' }
Move-Item -LiteralPath $temp -Destination $stageApk -Force
if ((Get-FileHash -LiteralPath $stageApk -Algorithm SHA256).Hash -ne $candidateHash) { throw 'Promoted stage hash mismatch.' }

$state | Add-Member -MemberType NoteProperty -Name prior_release_sha256 -Value $a57Hash -Force
$state | Add-Member -MemberType NoteProperty -Name prior_release_backup -Value $a57 -Force
$state | Add-Member -MemberType NoteProperty -Name release_archive -Value $candidate -Force
$state | Add-Member -MemberType NoteProperty -Name release_sha256 -Value $candidateHash -Force
$state | Add-Member -MemberType NoteProperty -Name promoted_at_utc -Value ([DateTime]::UtcNow.ToString('o')) -Force
$state | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8

$report = Get-Content -LiteralPath $reportPath -Raw
$section = @"

## Launcher promotion EB256

- EB256 passed visual validation on the physical TV and is archived at ``$candidate`` (SHA-256 ``$candidateHash``).
- A57 remains preserved for rollback at ``$a57`` (SHA-256 ``$a57Hash``).
- EB256 was promoted into ``build\system_stage_slim\app\PhilcoUltra777\PhilcoUltra777.apk``; the staged hash was verified after copy.
- This updates only the local working stage. No firmware package, TV `/system`, partition, or bootloader was changed.
"@
$report = [regex]::Replace($report, '(?s)\r?\n## Launcher promotion EB256\r?\n.*$', '')
$report += $section
Set-Content -LiteralPath $reportPath -Value $report -Encoding UTF8

Write-Output "EB256 staged: $stageApk ($candidateHash)"
Write-Output "A57 rollback preserved: $a57 ($a57Hash)"
Write-Output "State and report updated: $statePath; $reportPath"
