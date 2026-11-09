$ErrorActionPreference = 'Stop'
$project = 'C:\Users\anast\Desktop\aizzak-android'
$outDir = Join-Path $project 'docs\delivery\evidence\design-qa-reference-3f3ab2bd'
New-Item -ItemType Directory -Path $outDir -Force | Out-Null
$reference = 'file:///C:/Users/anast/Desktop/aizzak-android/docs/delivery/tasks/data-search-agent/design-reference.html'
$configs = @(
  @{locale='en';theme='light';layout='compact';scale='1.0'},
  @{locale='ar';theme='dark';layout='compact';scale='1.0'},
  @{locale='en';theme='dark';layout='expanded';scale='1.0'},
  @{locale='ar';theme='light';layout='expanded';scale='1.3'}
)
foreach ($number in 1..10) {
  $screen = 'SCR-DS-{0:000}' -f $number
  foreach ($config in $configs) {
    $name = '{0}__{1}_{2}_{3}_fs{4}.png' -f $screen,$config.locale,$config.theme,$config.layout,$config.scale
    $output = Join-Path $outDir $name
    $url = '{0}#{1}|{2}|{3}|{4}|{5}|content' -f $reference,$screen,$config.locale,$config.theme,$config.layout,$config.scale
    $args = @('--headless=new','--disable-gpu','--hide-scrollbars','--no-first-run','--window-size=1200,1200','--virtual-time-budget=1000',"--user-data-dir=$outDir\browser-profile", "--screenshot=$output",$url)
    $process = Start-Process -FilePath 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe' -ArgumentList $args -WindowStyle Hidden -PassThru -Wait
    if ($process.ExitCode -ne 0 -or !(Test-Path -LiteralPath $output)) { throw "Reference rendering failed: $name" }
    Write-Output $name
  }
}
