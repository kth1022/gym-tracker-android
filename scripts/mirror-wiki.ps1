param(
  [string]$RepoUrl = "https://github.com/kth1022/gym-tracker-android.wiki.git",
  [string]$SourceDir = "docs/wiki",
  [string]$Message = "Update Gym Tracker wiki"
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$sourcePath = Resolve-Path (Join-Path $repoRoot $SourceDir)
$workRoot = Join-Path ([System.IO.Path]::GetTempPath()) "gym-tracker-wiki-mirror"
$wikiPath = Join-Path $workRoot "wiki"
$repoUserName = git -C $repoRoot config user.name
$repoUserEmail = git -C $repoRoot config user.email

if (Test-Path $workRoot) {
  Remove-Item -LiteralPath $workRoot -Recurse -Force
}

New-Item -ItemType Directory -Path $workRoot | Out-Null

git clone $RepoUrl $wikiPath

Get-ChildItem -Path $wikiPath -Filter "*.md" -File | Remove-Item -Force
Copy-Item -Path (Join-Path $sourcePath "*.md") -Destination $wikiPath

Push-Location $wikiPath
try {
  if ($repoUserName) {
    git config user.name $repoUserName
  }
  if ($repoUserEmail) {
    git config user.email $repoUserEmail
  }

  git add -- "*.md"

  $changes = git status --porcelain
  if (-not $changes) {
    Write-Host "Wiki already matches docs/wiki."
    exit 0
  }

  git commit -m $Message
  git push origin master
}
finally {
  Pop-Location
}
