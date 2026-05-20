package com.smarthealthcare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthealthcare.common.Result;
import com.smarthealthcare.entity.BizDrug;
import com.smarthealthcare.entity.BizDrugTransaction;
import com.smarthealthcare.entity.BizPrescription;
import com.smarthealthcare.service.DrugService;
import com.smarthealthcare.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "药房端接口", description = "处方审核、发药、库存管理")
@RestController
@RequestMapping("/api/v1/pharmacy")
@RequiredArgsConstructor
public class PharmacyController {

    private final PrescriptionService prescriptionService;
    private final DrugService drugService;

    // ========== 处方管理 ==========

    @Operation(summary = "待审核处方列表")
    @GetMapping("/prescription/pending")
    public Result<List<BizPrescription>> pendingPrescriptions() {
        return Result.success(prescriptionService.listPendingAudit());
    }

    @Operation(summary = "审核处方")
    @PutMapping("/prescription/{id}/audit")
    public Result<BizPrescription> auditPrescription(@PathVariable Long id,
                                                      @RequestParam Long pharmacistId,
                                                      @RequestParam boolean approved,
                                                      @RequestParam(required = false) String opinion) {
        return Result.success(prescriptionService.audit(id, pharmacistId, approved, opinion));
    }

    @Operation(summary = "发药")
    @PutMapping("/prescription/{id}/dispense")
    public Result<BizPrescription> dispensePrescription(@PathVariable Long id,
                                                         @RequestParam Long pharmacistId) {
        return Result.success("发药成功", prescriptionService.dispense(id, pharmacistId));
    }

    // ========== 药品库存管理 ==========

    @Operation(summary = "药品列表（分页）")
    @GetMapping("/drug/page")
    public Result<Page<BizDrug>> drugPage(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String keyword) {
        return Result.success(drugService.page(current, size, keyword));
    }

    @Operation(summary = "新增药品")
    @PostMapping("/drug")
    public Result<Void> addDrug(@RequestBody BizDrug drug) {
        drugService.add(drug);
        return Result.success("药品添加成功");
    }

    @Operation(summary = "更新药品信息")
    @PutMapping("/drug")
    public Result<Void> updateDrug(@RequestBody BizDrug drug) {
        drugService.update(drug);
        return Result.success("药品更新成功");
    }

    @Operation(summary = "药品入库")
    @PostMapping("/drug/stock-in")
    public Result<Void> stockIn(@RequestParam Long drugId, @RequestParam Integer quantity,
                                 @RequestParam String batchNo, @RequestParam Long operatorId) {
        drugService.stockIn(drugId, quantity, batchNo, operatorId);
        return Result.success("入库成功");
    }

    @Operation(summary = "药品出库")
    @PostMapping("/drug/stock-out")
    public Result<Void> stockOut(@RequestParam Long drugId, @RequestParam Integer quantity,
                                  @RequestParam Long operatorId, @RequestParam(required = false) String remark) {
        drugService.stockOut(drugId, quantity, operatorId, remark != null ? remark : "");
        return Result.success("出库成功");
    }

    @Operation(summary = "低库存预警列表")
    @GetMapping("/drug/low-stock")
    public Result<List<BizDrug>> lowStockDrugs() {
        return Result.success(drugService.getLowStockDrugs());
    }

    @Operation(summary = "临期药品列表")
    @GetMapping("/drug/near-expiry")
    public Result<List<BizDrug>> nearExpiryDrugs() {
        return Result.success(drugService.getNearExpiryDrugs());
    }

    @Operation(summary = "药品出入库流水")
    @GetMapping("/drug/{drugId}/transactions")
    public Result<List<BizDrugTransaction>> transactions(@PathVariable Long drugId) {
        return Result.success(drugService.getTransactions(drugId));
    }
}
