[CmdletBinding()]
param([string] $ProjectRoot)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
    if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) { $ProjectRoot = 'C:\PhilcoOS' }
    else { $ProjectRoot = Split-Path -Parent $PSScriptRoot }
}
$ProjectRoot = [IO.Path]::GetFullPath($ProjectRoot)
$sourceApk = Join-Path $ProjectRoot 'source\philco-home\build\outputs\apk\release\philco-home-release.apk'
$stageApk = Join-Path $ProjectRoot 'build\system_stage_slim\app\PhilcoUltra777\PhilcoUltra777.apk'
$statePath = Join-Path $ProjectRoot 'analysis\slim_stage_state.json'
$reportPath = Join-Path $ProjectRoot 'analysis\slim_stage_report.md'
$archiveDir = Join-Path $ProjectRoot 'output\releases'
$zipalign = Join-Path $ProjectRoot '.toolchain\android-sdk\build-tools\35.0.0\zipalign.exe'
$aapt = Join-Path $ProjectRoot '.toolchain\android-sdk\build-tools\35.0.0\aapt.exe'
$apksigner = Join-Path $ProjectRoot '.toolchain\android-sdk\build-tools\35.0.0\apksigner.bat'
$javaHome = Join-Path $ProjectRoot '.toolchain\jdk17\jdk-17.0.21+9'
$oldHash = 'A56C3D7D370D83FC3AEC652F9AC14E024CBD9341466737291AB8983B59AA7374'
$newHash = '13BADB2D4A823E9D960D32E9721E634450E39953D34BE742BCC460A639928676'
$certHash = 'b52e3604deca330788c71cca8031b25b4208a7b9af6f521ff162634e14f2f9aa'
$oldArchive = Join-Path $archiveDir 'philco-home-A56-A56C3D7D.apk'
$newArchive = Join-Path $archiveDir 'philco-home-A57-13BADB2D.apk'

foreach ($required in @($sourceApk, $stageApk, $statePath, $zipalign, $aapt, $apksigner, (Join-Path $javaHome 'bin\java.exe'))) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Required file missing: $required" }
}
if (-not (Test-Path -LiteralPath $reportPath -PathType Leaf)) { throw "Slim report missing: $reportPath" }
$state = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
if ([IO.Path]::GetFullPath($state.target) -ne [IO.Path]::GetFullPath((Join-Path $ProjectRoot 'build\system_stage_slim'))) {
    throw 'State file does not identify build\system_stage_slim as its target.'
}

$sourceHash = (Get-FileHash -LiteralPath $sourceApk -Algorithm SHA256).Hash
$stageHash = (Get-FileHash -LiteralPath $stageApk -Algorithm SHA256).Hash
if ($sourceHash -ne $newHash) { throw "Current release is not the audited A57 artifact: $sourceHash" }
if ($stageHash -notin @($oldHash, $newHash)) { throw "Staged launcher is neither the audited A56 nor A57: $stageHash" }
if ($stageHash -eq $oldHash -and $state.release_sha256 -ne $oldHash) { throw 'Stage says a different release is current; refusing promotion.' }

& $zipalign -c -v 4 $sourceApk | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'A57 zipalign check failed.' }
$badging = (& $aapt dump badging $sourceApk 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $badging -notmatch "package: name='com\.philco\.ultra777\.home' versionCode='777' versionName='1\.0\.0-ultra777'") {
    throw 'A57 package identity/version check failed.'
}
$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"
$signingOutput = (& $apksigner verify --verbose --min-sdk-version 19 --print-certs $sourceApk 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $signingOutput -notmatch 'Verified using v1 scheme \(JAR signing\): true' -or
    $signingOutput -notmatch [regex]::Escape("Signer #1 certificate SHA-256 digest: $certHash")) {
    throw 'A57 v1 signature/certificate check failed.'
}

[IO.Directory]::CreateDirectory($archiveDir) | Out-Null
foreach ($archive in @(@{ Path = $oldArchive; Hash = $oldHash; Source = $stageApk },
                       @{ Path = $newArchive; Hash = $newHash; Source = $sourceApk })) {
    if (Test-Path -LiteralPath $archive.Path -PathType Leaf) {
        if ((Get-FileHash -LiteralPath $archive.Path -Algorithm SHA256).Hash -ne $archive.Hash) {
            throw "Release archive exists with unexpected content: $($archive.Path)"
        }
    } else {
        if ((Get-FileHash -LiteralPath $archive.Source -Algorithm SHA256).Hash -ne $archive.Hash) {
            throw "Archive source changed before copy: $($archive.Source)"
        }
        Copy-Item -LiteralPath $archive.Source -Destination $archive.Path
        if ((Get-FileHash -LiteralPath $archive.Path -Algorithm SHA256).Hash -ne $archive.Hash) {
            throw "Archived release hash mismatch: $($archive.Path)"
        }
    }
}

$promotionTime = [DateTime]::UtcNow.ToString('o')
if ($stageHash -eq $oldHash) {
    $temporaryStageApk = Join-Path (Split-Path -Parent $stageApk) 'PhilcoUltra777.A57.tmp.apk'
    if (Test-Path -LiteralPath $temporaryStageApk -PathType Leaf) {
        if ((Get-FileHash -LiteralPath $temporaryStageApk -Algorithm SHA256).Hash -ne $newHash) {
            throw "Promotion temp exists with unexpected content: $temporaryStageApk"
        }
    } else { Copy-Item -LiteralPath $newArchive -Destination $temporaryStageApk }
    if ((Get-FileHash -LiteralPath $temporaryStageApk -Algorithm SHA256).Hash -ne $newHash) {
        throw 'Temporary staged A57 failed SHA-256 verification.'
    }
    Move-Item -LiteralPath $temporaryStageApk -Destination $stageApk -Force
}
if ((Get-FileHash -LiteralPath $stageApk -Algorithm SHA256).Hash -ne $newHash) {
    throw 'Promoted stage APK does not match A57.'
}

$state | Add-Member -MemberType NoteProperty -Name prior_release_sha256 -Value $oldHash -Force
$state | Add-Member -MemberType NoteProperty -Name prior_release_backup -Value $oldArchive -Force
$state | Add-Member -MemberType NoteProperty -Name release_archive -Value $newArchive -Force
$state | Add-Member -MemberType NoteProperty -Name release_sha256 -Value $newHash -Force
$state | Add-Member -MemberType NoteProperty -Name promoted_at_utc -Value $promotionTime -Force
$state | ConvertTo-Json | Set-Content -LiteralPath $statePath -Encoding UTF8

$report = Get-Content -LiteralPath $reportPath -Raw
$section = @"

## Launcher promotion A57

- Previous staged A56 SHA-256: ``$oldHash``; preserved at ``$oldArchive``.
- A57 SHA-256: ``$newHash``; archived at ``$newArchive`` and promoted to ``build\system_stage_slim\app\PhilcoUltra777\PhilcoUltra777.apk``.
- Stage remains a working copy only; no firmware package was created and no partition was written.
"@
$report = [regex]::Replace($report, '(?s)\r?\n## Launcher promotion A57\r?\n.*$', '')
$report += $section
Set-Content -LiteralPath $reportPath -Value $report -Encoding UTF8

$stageInfo = Get-Item -LiteralPath $stageApk
Write-Output "A56 preserved: $oldArchive ($oldHash)"
Write-Output "A57 archived: $newArchive ($newHash)"
Write-Output "A57 staged: $($stageInfo.FullName), $($stageInfo.Length) bytes"
Write-Output "State/report updated: $statePath; $reportPath"
