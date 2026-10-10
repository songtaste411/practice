param(
    [Parameter(Mandatory = $true)][string]$Day,
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Title
)

$ErrorActionPreference = "Stop"
$dayNum = ($Day -replace "\D", "")
if (-not $dayNum) { throw "Day must contain digits, for example 01" }
$dayNum = $dayNum.PadLeft(2, "0")
$safeName = ($Name -replace "[^A-Za-z0-9]", "")
if (-not $safeName) { throw "Name should be English, for example Oversell" }
$switch = "day$dayNum"
$code = "DAY-$dayNum"
$classCase = "Day${dayNum}${safeName}Case"
$classStrategy = "Day${dayNum}${safeName}Strategy"
$root = Split-Path -Parent $PSScriptRoot
$caseDir = Join-Path $root "backend\src\main\java\com\clinic\incident\cases"
$strategyDir = Join-Path $root "backend\src\main\java\com\clinic\inventory\domain"
$casePath = Join-Path $caseDir "$classCase.java"
$strategyPath = Join-Path $strategyDir "$classStrategy.java"
if ((Test-Path $casePath) -or (Test-Path $strategyPath)) { throw "File already exists. Refuse to overwrite." }
$safeTitle = $Title -replace '"', "'"

$case = @"
package com.clinic.incident.cases;

import com.clinic.incident.IncidentCase;
import org.springframework.stereotype.Component;

import java.util.List;

/** $code $safeTitle */
@Component
public class $classCase implements IncidentCase {

    @Override
    public String code() {
        return "$code";
    }

    @Override
    public String title() {
        return "$safeTitle";
    }

    @Override
    public String symptom() {
        return "Write the symptom here";
    }

    @Override
    public String veteranPoint() {
        return "Write what this case trains. After the fix, write the root cause here";
    }

    @Override
    public String howToEnable() {
        return "On the case board, switch to $switch";
    }

    @Override
    public boolean mutatesBehavior() {
        return true;
    }

    @Override
    public ReproductionResult reproduce() {
        return new ReproductionResult(code(), "Add a reproduce script", List.of("Switch to $switch", "Use the fulfill desk"), null);
    }
}
"@

$strategy = @"
package com.clinic.inventory.domain;

import com.clinic.shared.kernel.BizException;
import com.clinic.shared.kernel.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Inventory strategy for $code. Switch name: $switch.
 * Put the broken behavior in these methods.
 * Leave HealthyReserveStrategy unchanged so you can switch back.
 */
@Component
public class $classStrategy implements ReserveStrategy {

    @Override
    public String switchName() {
        return "$switch";
    }

    @Override
    public String title() {
        return "$code $safeTitle";
    }

    @Override
    public void reserve(Long skuId, int quantity) {
        throw new BizException(ErrorCode.BAD_REQUEST, "$code reserve is still empty");
    }

    @Override
    public void confirmPaid(Long skuId, int quantity) {
        throw new BizException(ErrorCode.BAD_REQUEST, "$code confirmPaid is still empty");
    }

    @Override
    public void release(Long skuId, int quantity) {
        throw new BizException(ErrorCode.BAD_REQUEST, "$code release is still empty");
    }
}
"@

$utf8 = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($casePath, $case.Replace("`r`n", "`n") + "`n", $utf8)
[System.IO.File]::WriteAllText($strategyPath, $strategy.Replace("`r`n", "`n") + "`n", $utf8)
Write-Output "Created case: $casePath"
Write-Output "Created strategy: $strategyPath"
Write-Output "Next: edit the strategy, restart the API, switch to $switch on the case board."
