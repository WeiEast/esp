package com.apass.esp.mapper;

import com.apass.esp.domain.entity.Kvattr;
import com.apass.esp.domain.entity.bill.TxnInfoEntity;
import com.apass.gfb.framework.mybatis.GenericMapper;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by jie.xu on 17/4/28.
 */
public interface TxnInfoMapper extends GenericMapper<TxnInfoEntity, Long>{

  /**
   * 根据订单id查询流水
   */
  List<TxnInfoEntity> selectByOrderId(String orderId);

   /**
     * 根据orderId和交易类型(t01,505)查询去交易流水表(OrigOryid:原始消费交易的queryId)
	 * @param orderId:订单id
	 * @return
	 */
  TxnInfoEntity queryTxnInfoByOrderidAndTxntypeInSql(String orderId);

  void updateTime(@Param("orderId") String orderId,@Param("date") Date date);

   /**    
	 * @param orderId订单id
	 * @param typeCode交易类型
	 * @return
	 */
  TxnInfoEntity queryOrigTxnIdByOrderidAndstatus(@Param("orderId")String orderId, @Param("typeCode")String typeCode);


}
