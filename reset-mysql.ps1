# MySQL Password Reset Script - Run as Administrator
$ErrorActionPreference = "Stop"
$mysqlDir = "C:\Program Files\MySQL\MySQL Server 8.0"
$dataDir = "C:\ProgramData\MySQL\MySQL Server 8.0\Data"

Write-Host "1. Stopping MySQL service..."
net stop MySQL80
Start-Sleep -Seconds 2

Write-Host "2. Creating password reset SQL..."
$sql = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;
"@
$sql | Out-File -FilePath "$env:TEMP\mysql-reset.sql" -Encoding ASCII

Write-Host "3. Starting MySQL with --init-file for password reset..."
$mysqld = "$mysqlDir\bin\mysqld.exe"
$proc = Start-Process -FilePath $mysqld -ArgumentList "--init-file=`"$env:TEMP\mysql-reset.sql`" --console" -PassThru -NoNewWindow

Start-Sleep -Seconds 5

Write-Host "4. Stopping temporary MySQL process..."
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

Write-Host "5. Starting MySQL service..."
net start MySQL80
Start-Sleep -Seconds 2

Write-Host "6. Testing connection..."
& "$mysqlDir\bin\mysql.exe" -u root -p123456 -e "SELECT 'Password reset successful!' AS result;"

Remove-Item "$env:TEMP\mysql-reset.sql" -Force -ErrorAction SilentlyContinue
Write-Host "Done!"
