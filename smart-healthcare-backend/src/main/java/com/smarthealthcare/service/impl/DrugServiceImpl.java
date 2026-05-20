package com.smarthealthcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthealthcare.common.BusinessException;
import com.smarthealthcare.entity.BizDrug;
import com.smarthealthcare.entity.BizDrugTransaction;
import com.smarthealthcare.mapper.BizDrugMapper;
import com.smarthealthcare.mapper.BizDrugTransactionMapper;
import com.smarthealthcare.service.DrugService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final BizDrugMapper drugMapper;
    private final BizDrugTransactionMapper transactionMapper;

    @Override
    public List<BizDrug> listAll() {
        return drugMapper.selectList(new LambdaQueryWrapper<BizDrug>()
                .eq(BizDrug::getStatus, 1));
    }

    @Override
    public Page<BizDrug> page(int current, int size, String keyword) {
        LambdaQueryWrapper<BizDrug> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(BizDrug::getDrugName, keyword)
                    .or().like(BizDrug::getDrugCode, keyword);
        }
        wrapper.orderByDesc(BizDrug::getUpdateTime);
        return drugMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public BizDrug getById(Long id) {
        return drugMapper.selectById(id);
    }

    @Override
    @Transactional
    public void add(BizDrug drug) {
        // 校验药品编码唯一性
        LambdaQueryWrapper<BizDrug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDrug::getDrugCode, drug.getDrugCode());
        if (drugMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("药品编码已存在");
        }
        drug.setStockCount(0);
        drug.setVersion(0);
        drugMapper.insert(drug);
    }

    @Override
    @Transactional
    public void update(BizDrug drug) {
        drugMapper.updateById(drug);
    }

    @Override
    @Transactional
    public void stockIn(Long drugId, Integer quantity, String batchNo, Long operatorId) {
        BizDrug drug = drugMapper.selectById(drugId);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }

        int beforeStock = drug.getStockCount();
        drug.setStockCount(beforeStock + quantity);
        drugMapper.updateById(drug);

        // 记录入库流水
        BizDrugTransaction trans = new BizDrugTransaction();
        trans.setDrugId(drugId);
        trans.setType(1); // 入库
        trans.setQuantity(quantity);
        trans.setBeforeStock(beforeStock);
        trans.setAfterStock(drug.getStockCount());
        trans.setBatchNo(batchNo);
        trans.setOperatorId(operatorId);
        transactionMapper.insert(trans);

        log.info("药品[{}]入库{}, 库存{}->{}", drug.getDrugName(), quantity, beforeStock, drug.getStockCount());
    }

    @Override
    @Transactional
    public void stockOut(Long drugId, Integer quantity, Long operatorId, String remark) {
        BizDrug drug = drugMapper.selectById(drugId);
        if (drug == null) {
            throw new BusinessException("药品不存在");
        }
        if (drug.getStockCount() < quantity) {
            throw new BusinessException("库存不足，当前库存: " + drug.getStockCount());
        }

        int beforeStock = drug.getStockCount();
        drug.setStockCount(beforeStock - quantity);
        drugMapper.updateById(drug);

        BizDrugTransaction trans = new BizDrugTransaction();
        trans.setDrugId(drugId);
        trans.setType(2); // 出库
        trans.setQuantity(quantity);
        trans.setBeforeStock(beforeStock);
        trans.setAfterStock(drug.getStockCount());
        trans.setOperatorId(operatorId);
        trans.setRemark(remark);
        transactionMapper.insert(trans);

        log.info("药品[{}]出库{}, 库存{}->{}", drug.getDrugName(), quantity, beforeStock, drug.getStockCount());
    }

    @Override
    public List<BizDrug> getLowStockDrugs() {
        return drugMapper.selectLowStockDrugs();
    }

    @Override
    public List<BizDrug> getNearExpiryDrugs() {
        return drugMapper.selectNearExpiryDrugs();
    }

    @Override
    public List<BizDrugTransaction> getTransactions(Long drugId) {
        return transactionMapper.selectList(
                new LambdaQueryWrapper<BizDrugTransaction>()
                        .eq(BizDrugTransaction::getDrugId, drugId)
                        .orderByDesc(BizDrugTransaction::getCreateTime));
    }
}
