param([switch]$Offline, [string]$JsonJar)
$ErrorActionPreference = 'Stop'
if(-not $env:JAVA_HOME){throw 'Set JAVA_HOME to JDK 17'}
if(-not $JsonJar){
 $JsonJar=Join-Path $PSScriptRoot 'work/test-deps/json-20240303.jar'
 if(-not (Test-Path -LiteralPath $JsonJar)){
  if($Offline){throw 'Offline test dependency missing. Pass -JsonJar or run once online.'}
  New-Item -ItemType Directory -Force -Path (Split-Path $JsonJar) | Out-Null
  Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar' -OutFile $JsonJar
 }
}
if((Get-FileHash -LiteralPath $JsonJar -Algorithm SHA256).Hash -ne '3CF6CD6892E32E2B4C1C39E0F52F5248A2F5B37646FDFBB79A66B46B618414ED'){throw 'JSON test dependency checksum mismatch'}
$JsonJar=(Resolve-Path -LiteralPath $JsonJar).Path
Push-Location $PSScriptRoot
try {
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d tests\classes app\src\main\java\app\loveu\Domain.java app\src\main\java\app\loveu\Analytics.java tests\CoreTest.java tests\AnalyticsTest.java tests\ImportCheck.java app\src\main\java\app\loveu\RecordSearch.java tests\RecordSearchTest.java app\src\main\java\app\loveu\RecordCoverage.java tests\RecordCoverageTest.java app\src\main\java\app\loveu\SessionTime.java tests\SessionTimeTest.java app\src\main\java\app\loveu\TapGesture.java tests\TapGestureTest.java app\src\main\java\app\loveu\CoupleCrypto.java app\src\main\java\app\loveu\CoupleStateGuard.java tests\CoupleCryptoTest.java tests\CoupleStateGuardTest.java
 if($LASTEXITCODE -ne 0){throw 'Core compilation failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.CoreTest
 if($LASTEXITCODE -ne 0){throw 'Core tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.AnalyticsTest
 if($LASTEXITCODE -ne 0){throw 'Analytics tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.RecordSearchTest
 if($LASTEXITCODE -ne 0){throw 'Search tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.RecordCoverageTest
 if($LASTEXITCODE -ne 0){throw 'Coverage tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.SessionTimeTest
 if($LASTEXITCODE -ne 0){throw 'Session time tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.TapGestureTest
 if($LASTEXITCODE -ne 0){throw 'Gesture tests failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.CoupleCryptoTest
 if($LASTEXITCODE -ne 0){throw "Couple crypto tests failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.CoupleStateGuardTest
 if($LASTEXITCODE -ne 0){throw "Couple state tests failed"}
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d tests\classes app\src\main\java\app\loveu\CoupleSyncFlow.java tests\CoupleSyncFlowTest.java
 if($LASTEXITCODE -ne 0){throw "Sync flow compilation failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.CoupleSyncFlowTest
 if($LASTEXITCODE -ne 0){throw "Sync flow tests failed"}
 $jsonTest=$JsonJar
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $jsonTest -d tests\classes app\src\main\java\app\loveu\CoupleProposal.java tests\CoupleProposalTest.java
 if($LASTEXITCODE -ne 0){throw "Proposal test compilation failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp "tests\classes;$jsonTest" app.loveu.CoupleProposalTest
 if($LASTEXITCODE -ne 0){throw "Proposal tests failed"}
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $jsonTest -d tests\classes app\src\main\java\app\loveu\SharedCoupleRecords.java app\src\main\java\app\loveu\CoupleCrypto.java tests\SharedCoupleRecordsTest.java tests\SharedEncryptedRoundTripTest.java
 if($LASTEXITCODE -ne 0){throw "Shared record compilation failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp "tests\classes;$jsonTest" app.loveu.SharedCoupleRecordsTest
 if($LASTEXITCODE -ne 0){throw "Shared record tests failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp "tests\classes;$jsonTest" app.loveu.SharedEncryptedRoundTripTest
 if($LASTEXITCODE -ne 0){throw "Shared encrypted round trip tests failed"}
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -d tests\classes app\src\main\java\app\loveu\PeriodAnalysis.java app\src\main\java\app\loveu\PeriodForecast.java tests\PeriodAnalysisTest.java tests\PeriodForecastTest.java
 if($LASTEXITCODE -ne 0){throw "Period compilation failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.PeriodAnalysisTest
 if($LASTEXITCODE -ne 0){throw "Period tests failed"}
 & "$env:JAVA_HOME\bin\java.exe" -cp tests\classes app.loveu.PeriodForecastTest
 if($LASTEXITCODE -ne 0){throw 'Period forecast tests failed'}
 & "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp "$jsonTest;tests\classes" -d tests\classes app\src\main\java\app\loveu\BulkShare.java app\src\main\java\app\loveu\CoupleDeliveryQueue.java app\src\main\java\app\loveu\PeriodRecords.java tests\BulkShareTest.java
 if($LASTEXITCODE -ne 0){throw 'Bulk sharing tests compilation failed'}
 & "$env:JAVA_HOME\bin\java.exe" -cp "tests\classes;$jsonTest" app.loveu.BulkShareTest
 if($LASTEXITCODE -ne 0){throw 'Bulk sharing tests failed'}
 $gradleArgs=@("--no-daemon","assembleDebug","assembleRelease","lintRelease")
 if($Offline){$gradleArgs += "--offline"}
 & "$PSScriptRoot/gradlew.bat" @gradleArgs
 if($LASTEXITCODE -ne 0){throw 'Android build failed'}
} finally { Pop-Location }


