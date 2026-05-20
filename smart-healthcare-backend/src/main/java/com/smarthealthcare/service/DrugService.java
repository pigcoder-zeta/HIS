package com.smarthealthcare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarthealthcare.entity.BizDrug;
import com.smarthealthcare.entity.BizDrugTransaction;

import java.util.List;

public interface DrugService {

    List<BizDrug> listAll();

    Page<BizDrug> page(int current, int size, String keyword);

    BizDrug getById(Long id);

    void add(BizDrug drug);

    void update(BizDrug drug);

    void stockIn(Long drugId, Integer quantity, String batchNo, Long operatorId);

    void stockOut(Long drugId, Integer quantity, Long operatorId, String remark);

    List<BizDrug> getLowStockDrugs();

    List<BizDrug> getNearExpiryDrugs();

    List<BizDrugTransaction> getTransactions(Long drugId);
}
