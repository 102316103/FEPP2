package com.syscom.fep.mybatis.ext.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.ext.model.RmoutExt;

public class RmoutExtMapperTest extends MybatisBaseTest {

	@Autowired
	private RmoutExtMapper mapper;

	@Test
	public void test() {
		RmoutExt recoder = new RmoutExt();
		mapper.getRMOUTByCheckORGData(recoder);
		mapper.getSingleRMOUT(recoder);
		mapper.getRMOUTForCheckOutData(recoder);
		mapper.getRmoutByDef(recoder);
		mapper.getRMOUTUnionRMOUTEByDef(recoder,false);
		mapper.queryByPrimaryKeyWithUpdLock(null, null, null, null);
		mapper.getRmoutSummaryCnt(null, null);
		mapper.get1172SummaryCnt(null, null, null, null);
		mapper.getAutoBackSummaryCnt(null, null, null, null);
		mapper.getAutoBackBank();
		mapper.getTopRMOUTByApdateSenderBank(null, null, null);
		mapper.getTopRMOUTByApdateSenderBank1(null, null, null);
		mapper.getRMOUTbyTXAMT(null, null);
		mapper.getRMOUTbyReceiverBANK(null, null);
		mapper.getRMOUTForUI028230(null);
//        mapper.insertSelective(recoder);
//        mapper.updateByPrimaryKey(recoder);
//        RmoutExt aa = (RmoutExt) mapper.getSingleRMOUT(recoder);
//        mapper.deleteByPrimaryKey(recoder);

		
	}
}
