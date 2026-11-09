$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$project = 'C:\Users\anast\Desktop\aizzak-android'
$base = Join-Path $project 'docs\delivery\evidence'
$output = Join-Path $base 'design-qa-contact-3f3ab2bd'
New-Item -ItemType Directory -Path $output -Force | Out-Null
foreach ($number in 1..10) {
  $screen = 'SCR-DS-{0:000}' -f $number
  foreach ($layout in @('compact','expanded')) {
    $width = if ($layout -eq 'compact') { 390 } else { 840 }
    $configs = if ($layout -eq 'compact') { @(@('en','light','1.0'),@('ar','dark','1.0')) } else { @(@('en','dark','1.0'),@('ar','light','1.3')) }
    $canvas = New-Object System.Drawing.Bitmap ($width*2),2060
    $g = [System.Drawing.Graphics]::FromImage($canvas)
    $g.Clear([System.Drawing.Color]::White)
    $font = New-Object System.Drawing.Font 'Arial',12
    for ($row=0;$row -lt 2;$row++) {
      $c=$configs[$row]
      $referenceFile=Join-Path $base ('design-qa-reference-3f3ab2bd\{0}__{1}_{2}_{3}_fs{4}.png' -f $screen,$c[0],$c[1],$layout,$c[2])
      $currentFile=Join-Path $base ('screenshots\data-search-agent-3f3ab2bd\{0}__{1}_{2}_w{3}_fs{4}.png' -f $screen,$c[0],$c[1],$width,$c[2])
      $ref=[System.Drawing.Image]::FromFile($referenceFile)
      $cur=[System.Drawing.Image]::FromFile($currentFile)
      $y=$row*1030
      $g.DrawString("REFERENCE $screen $($c -join '/')",$font,[System.Drawing.Brushes]::Black,0,$y)
      $g.DrawString("COMPOSE $screen $($c -join '/')",$font,[System.Drawing.Brushes]::Black,$width,$y)
      $crop=if($layout -eq 'compact') { New-Object System.Drawing.Rectangle 539,148,390,844 } else { New-Object System.Drawing.Rectangle 304,148,860,760 }
      $dest=New-Object System.Drawing.Rectangle 0,($y+30),$width,([int]($crop.Height*$width/$crop.Width))
      $g.DrawImage($ref,$dest,$crop,[System.Drawing.GraphicsUnit]::Pixel)
      $g.DrawImage($cur,$width,($y+30),$width,1000)
      $ref.Dispose(); $cur.Dispose()
    }
    $canvas.Save((Join-Path $output "$screen-$layout.png"),[System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose();$canvas.Dispose();$font.Dispose()
  }
}
