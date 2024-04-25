package com.syscom.fep.mybatis.ext.mapper;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.model.Allbank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class AllbankExtMapperTest extends MybatisBaseTest {
    @Autowired
    private AllbankExtMapper mapper;

    @Test
    public void testUpdateALLBANKByBKNO() {
        Allbank defAllbank = new Allbank();
        defAllbank.setAllbankBkno("999, 000");
        defAllbank.setAllbankSetCloseFlag("1");
        defAllbank.setAllbankRmflag("1");
        mapper.updateALLBANKByBKNO(defAllbank);
    }
}
