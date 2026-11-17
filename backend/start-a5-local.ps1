Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep 2
$ok = 0
for ($i = 0; $i -lt 30 -and $ok -lt 3; $i++) {
  if ((Test-NetConnection 127.0.0.1 -Port 55432 -WarningAction SilentlyContinue).TcpTestSucceeded) { $ok++ } else { $ok = 0 }
  Start-Sleep 3
}
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:DB_URL = "jdbc:postgresql://127.0.0.1:55432/job_tracker"
$env:DB_USERNAME = "jt"
$env:DB_PASSWORD = "jtdemo-local-2026"
$env:JWT_SECRET = "a5-local-jwt-secret-key-32-characters!"
$env:ENCRYPTION_SECRET = "a5-local-encryption-secret-key-32c!"
$env:SERVER_PORT = "8082"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3102,http://localhost:3101"
Start-Process -FilePath java -ArgumentList "-jar","target\job-tracker-0.0.1-SNAPSHOT.jar" -RedirectStandardOutput "a5-backend.log" -RedirectStandardError "a5-backend-err.log" -NoNewWindow
Start-Sleep 18
curl.exe -s -o NUL -w "BACK8082:%{http_code}" -X POST http://localhost:8082/auth/login -H "Content-Type: application/json" -d "{}"