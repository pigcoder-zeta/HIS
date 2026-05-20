package com.smarthealthcare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarthealthcare.entity.BizDrug;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BizDrugMapper extends BaseMapper<BizDrug> {

    @Select("SELECT * FROM biz_drug WHERE stock_count <= safe_threshold AND status = 1")
    List<BizDrug> selectLowStockDrugs();

    @Select("SELECT * FROM biz_drug WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL 90 DAY) AND status = 1")
    List<BizDrug> selectNearExpiryDrugs();

    @Update("UPDATE biz_drug SET stock_count = stock_count - #{quantity}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND stock_count >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") Integer quantity,
                    @Param("version") Integer version);
}
