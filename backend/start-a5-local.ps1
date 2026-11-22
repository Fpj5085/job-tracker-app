# A5 local demo launcher — demo values are loaded from gitignored ..\.env.local
# Copy .env.local.example to .env.local (repo root) and fill in your local demo values.
$envFile = Join-Path $PSScriptRoot "..\.env.local"
if (-not (Test-Path $envFile)) {
  Write-Error "Missing $envFile - copy .env.local.example to .env.local (repo root) and fill in local demo values."
  exit 1
}
Get-Content $envFile | ForEach-Object {
  if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*?)\s*$') {
    [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], "Process")
  }
}
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep 2
$ok = 0
for ($i = 0; $i -lt 30 -and $ok -lt 3; $i++) {
  if ((Test-NetConnection 127.0.0.1 -Port 55432 -WarningAction SilentlyContinue).TcpTestSucceeded) { $ok++ } else { $ok = 0 }
  Start-Sleep 3
}
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:SERVER_PORT = "8082"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3102,http://localhost:3101"
Start-Process -FilePath java -ArgumentList "-jar","target\job-tracker-0.0.1-SNAPSHOT.jar" -RedirectStandardOutput "a5-backend.log" -RedirectStandardError "a5-backend-err.log" -NoNewWindow
Start-Sleep 18
curl.exe -s -o NUL -w "BACK8082:%{http_code}" -X POST http://localhost:8082/auth/login -H "Content-Type: application/json" -d "{}"
