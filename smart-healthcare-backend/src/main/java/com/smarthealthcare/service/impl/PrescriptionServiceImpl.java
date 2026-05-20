package com.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.dto.PrescriptionDTO;
import com.smarthealthcare.dto.PrescriptionItemDTO;
import com.smarthealthcare.entity.BizDrug;
import com.smarthealthcare.entity.BizPrescription;
import com.smarthealthcare.entity.BizPrescriptionItem;
import com.smarthealthcare.mapper.BizDrugMapper;
import com.smarthealthcare.mapper.BizPrescriptionItemMapper;
import com.smarthealthcare.mapper.BizPrescriptionMapper;
import com.smarthealthcare.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionItemMapper itemMapper;
    private final BizDrugMapper drugMapper;

    @Override
    @Transactional
    public BizPrescription create(Long doctorId, PrescriptionDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("处方明细不能为空");
        }

        BizPrescription prescription = new BizPrescription();
        prescription.setRecordId(dto.getRecordId());
        prescription.setPatientId(dto.getPatientId());
        prescription.setDoctorId(doctorId);
        prescription.setStatus(0); // 待审核
        prescription.setTotalAmount(BigDecimal.ZERO);
        prescriptionMapper.insert(prescription);

        BigDecimal total = BigDecimal.ZERO;
        for (PrescriptionItemDTO itemDTO : dto.getItems()) {
            BizDrug drug = drugMapper.selectById(itemDTO.getDrugId());
            if (drug == null || drug.getStatus() == 0) {
                throw new BusinessException("药品不存在或已停用: " + itemDTO.getDrugId());
            }
            if (drug.getStockCount() <= 0) {
                throw new BusinessException("药品[" + drug.getDrugName() + "]库存不足，无法开立处方");
            }
            if (drug.getIsHighRisk() == 1) {
                log.warn("高危药品[{}]被开立，需高级职称医师复核", drug.getDrugName());
            }

            BizPrescriptionItem item = new BizPrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setDrugId(drug.getId());
            item.setDrugName(drug.getDrugName());
            item.setDosage(itemDTO.getDosage());
            item.setUsageMethod(itemDTO.getUsageMethod());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(drug.getUnitPrice());
            itemMapper.insert(item);

            total = total.add(drug.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
        }

        prescription.setTotalAmount(total);
        prescriptionMapper.updateById(prescription);

        return prescription;
    }

    @Override
    @Transactional
    public BizPrescription audit(Long prescriptionId, Long pharmacistId, boolean approved, String opinion) {
        BizPrescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new BusinessException("处方不存在");
        }
        if (prescription.getStatus() != 0) {
            throw new BusinessException("该处方当前状态不可审核");
        }

        prescription.setPharmacistId(pharmacistId);
        prescription.setAuditOpinion(opinion);
        prescription.setStatus(approved ? 1 : 3); // 1-已审核, 3-已驳回
        prescriptionMapper.updateById(prescription);

        log.info("药师[{}]审核处方[{}], 结果: {}", pharmacistId, prescriptionId, approved ? "通过" : "驳回");
        return prescription;
    }

    @Override
    @Transactional
    public BizPrescription dispense(Long prescriptionId, Long pharmacistId) {
        BizPrescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null || prescription.getStatus() != 1) {
            throw new BusinessException("处方未审核，无法发药");
        }

        // 扣减库存
        List<BizPrescriptionItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionItem>()
                        .eq(BizPrescriptionItem::getPrescriptionId, prescriptionId));

        for (BizPrescriptionItem item : items) {
            BizDrug drug = drugMapper.selectById(item.getDrugId());
            if (drug == null) {
                throw new BusinessException("药品[" + item.getDrugName() + "]不存在");
            }
            int deducted = drugMapper.deductStock(drug.getId(), item.getQuantity(), drug.getVersion());
            if (deducted <= 0) {
                throw new BusinessException("药品[" + drug.getDrugName() + "]库存扣减失败，请检查库存");
            }
        }

        prescription.setStatus(2); // 已发药
        prescription.setPharmacistId(pharmacistId);
        prescriptionMapper.updateById(prescription);

        log.info("处方[{}]发药完成", prescriptionId);
        return prescription;
    }

    @Override
    public List<BizPrescription> listByPatient(Long patientId) {
        return prescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getPatientId, patientId)
                        .orderByDesc(BizPrescription::getCreateTime));
    }

    @Override
    public List<BizPrescription> listPendingAudit() {
        return prescriptionMapper.selectList(
                new LambdaQueryWrapper<BizPrescription>()
                        .eq(BizPrescription::getStatus, 0)
                        .orderByAsc(BizPrescription::getCreateTime));
    }
}
