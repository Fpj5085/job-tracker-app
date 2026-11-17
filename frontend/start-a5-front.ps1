Set-Location "K:\Fpj Project\A-job-tracker\frontend"
$env:PORT = "3102"
Start-Process -FilePath cmd -ArgumentList "/c","npm start -- -p 3102" -WindowStyle Hidden
Start-Sleep 12
curl.exe -s -o NUL -w "FRONT3102:%{http_code}" http://localhost:3102/login