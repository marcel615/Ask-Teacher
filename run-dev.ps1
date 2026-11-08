$ErrorActionPreference = 'Stop'
$envFile = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) {
    throw 'Create .env from .env.example before starting the dev profile.'
}

$required = @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD')
$values = @{}
foreach ($line in Get-Content -LiteralPath $envFile -Encoding UTF8) {
    if ($line -match '^\s*(#|$)') { continue }
    $separator = $line.IndexOf('=')
    if ($separator -lt 1) { throw 'Invalid .env entry: expected KEY=value.' }
    $key = $line.Substring(0, $separator).Trim()
    if ($key -in $required) { $values[$key] = $line.Substring($separator + 1) }
}
foreach ($key in $required) {
    if (-not $values.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($values[$key])) {
        throw "Set $key in .env before starting the dev profile."
    }
}

$previous = @{}
try {
    foreach ($key in $required) {
        $previous[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
        [Environment]::SetEnvironmentVariable($key, $values[$key], 'Process')
    }
    Push-Location $PSScriptRoot
    try {
        & .\gradlew.bat bootRun --args='--spring.profiles.active=dev'
        $exitCode = $LASTEXITCODE
    } finally {
        Pop-Location
    }
} finally {
    foreach ($key in $previous.Keys) {
        [Environment]::SetEnvironmentVariable($key, $previous[$key], 'Process')
    }
}
if ($exitCode -ne 0) { throw "Gradle bootRun failed with exit code $exitCode." }
