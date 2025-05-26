# Define the destination folder name
$destinationFolderName = "ai"

# Get the current directory
$currentDirectory = Get-Location

# Construct the full path for the destination folder
$destinationPath = Join-Path -Path $currentDirectory.Path -ChildPath $destinationFolderName

# Check if the destination folder exists, create it if not
if (-not (Test-Path -Path $destinationPath -PathType Container)) {
    New-Item -ItemType Directory -Path $destinationPath
    Write-Host "Created destination folder: $destinationPath"
} else {
    Write-Host "Destination folder already exists: $destinationPath"
}

# Find all .java and .yaml files in subfolders and copy them
Get-ChildItem -Path $currentDirectory.Path -Recurse -Include *.java, *.yml | ForEach-Object {
    try {
        Copy-Item -Path $_.FullName -Destination $destinationPath -Force
        Write-Host "Copied: $($_.FullName) to $destinationPath"
    }
    catch {
        Write-Host "Error copying $($_.FullName): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nScript complete."