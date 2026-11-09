param(
  [Parameter(Mandatory = $true)][string]$CurrentDirectory,
  [Parameter(Mandatory = $true)][string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = 'C:\Users\anast\Desktop\aizzak-android'
$base = Join-Path $project 'docs\delivery\evidence'
$current = (Resolve-Path -LiteralPath $CurrentDirectory).Path
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$output = (Resolve-Path -LiteralPath $OutputDirectory).Path
$font = New-Object System.Drawing.Font 'Arial',12
try {
  foreach ($number in 1..10) {
    $screen = 'SCR-DS-{0:000}' -f $number
    foreach ($layout in @('compact','expanded')) {
      $width = if ($layout -eq 'compact') { 390 } else { 840 }
      $configs = if ($layout -eq 'compact') { @(@('en','light','1.0'),@('ar','dark','1.0')) } else { @(@('en','dark','1.0'),@('ar','light','1.3')) }
      $canvas = New-Object System.Drawing.Bitmap ($width*2),2060
      $g = [System.Drawing.Graphics]::FromImage($canvas)
      try {
        $g.Clear([System.Drawing.Color]::White)
        for ($row=0;$row -lt 2;$row++) {
          $c=$configs[$row]
          $referenceFile=Join-Path $base ('design-qa-reference-3f3ab2bd\{0}__{1}_{2}_{3}_fs{4}.png' -f $screen,$c[0],$c[1],$layout,$c[2])
          $currentFile=Join-Path $current ('{0}__{1}_{2}_w{3}_fs{4}.png' -f $screen,$c[0],$c[1],$width,$c[2])
          $ref=[System.Drawing.Image]::FromFile($referenceFile)
          $cur=[System.Drawing.Image]::FromFile($currentFile)
          try {
            $y=$row*1030
            $g.DrawString("REFERENCE $screen $($c -join '/')",$font,[System.Drawing.Brushes]::Black,0,$y)
            $g.DrawString("COMPOSE $screen $($c -join '/')",$font,[System.Drawing.Brushes]::Black,$width,$y)
            $crop=if($layout -eq 'compact') { New-Object System.Drawing.Rectangle 539,148,390,844 } else { New-Object System.Drawing.Rectangle 304,148,860,760 }
            $dest=New-Object System.Drawing.Rectangle 0,($y+30),$width,([int]($crop.Height*$width/$crop.Width))
            $g.DrawImage($ref,$dest,$crop,[System.Drawing.GraphicsUnit]::Pixel)
            $g.DrawImage($cur,$width,($y+30),$width,1000)
          } finally { $ref.Dispose(); $cur.Dispose() }
        }
        $canvas.Save((Join-Path $output "$screen-$layout.png"),[System.Drawing.Imaging.ImageFormat]::Png)
      } finally { $g.Dispose(); $canvas.Dispose() }
    }
  }
  $variants = Get-ChildItem -LiteralPath $current -File -Filter '*.png' -Recurse |
    Where-Object { $_.DirectoryName -ne $current } |
    Group-Object { ($_.BaseName -replace '__.*$','') + '--' + $_.Directory.Name }
  foreach ($variant in $variants) {
    $files = @($variant.Group | Sort-Object Name)
    $canvas = New-Object System.Drawing.Bitmap 1680,2060
    $g = [System.Drawing.Graphics]::FromImage($canvas)
    try {
      $g.Clear([System.Drawing.Color]::White)
      for ($index=0; $index -lt $files.Count; $index++) {
        $file=$files[$index]
        $picture=[System.Drawing.Image]::FromFile($file.FullName)
        try {
          $column=$index%2; $row=[int][Math]::Floor($index/2)
          $x=$column*840; $y=$row*1030
          $g.DrawString($file.BaseName,$font,[System.Drawing.Brushes]::Black,$x,$y)
          $drawWidth = if ($file.Name -match '_w390_') { 390 } else { 840 }
          $g.DrawImage($picture,$x,($y+30),$drawWidth,1000)
        } finally { $picture.Dispose() }
      }
      $canvas.Save((Join-Path $output ($variant.Name + '-states.png')),[System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $canvas.Dispose() }
  }
} finally { $font.Dispose() }
