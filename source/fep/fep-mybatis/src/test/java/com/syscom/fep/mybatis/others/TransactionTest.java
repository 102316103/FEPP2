package com.syscom.fep.mybatis.others;

import com.syscom.fep.mybatis.MybatisBaseTest;
import com.syscom.fep.mybatis.mapper.AccountMapper;
import com.syscom.fep.mybatis.mapper.AtmboxMapper;
import com.syscom.fep.mybatis.model.Account;
import com.syscom.fep.mybatis.model.Atmbox;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertNull;

//@ActiveProfiles({"integration", "mybatis", "taipei"})
public class TransactionTest extends MybatisBaseTest {
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private AtmboxMapper atmBoxMapper;
    @Autowired
    private PlatformTransactionManager fepdbTransactionManager; // SpringBeanFactoryUtil.getBean(DataSourceConstant.BEAN_NAME_TRANSACTION_MANAGER)
    @Autowired
    private TransactionTemplate fepdbTransactionTemplate;

    private Account[] accounts;
    private Atmbox atmbox;

    @BeforeEach
    public void setUp() {
        accounts = new Account[]{
                new Account(StringUtils.leftPad("0", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("1", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("2", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("3", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("4", 14, "0"), "2", "20210408"),
                new Account(StringUtils.leftPad("5", 14, "0"), "2", "20210408")
        };
        for (Account account : accounts) {
            accountMapper.deleteByPrimaryKey(account);
        }

        atmbox = new Atmbox();
        atmbox.setAtmboxAtmno("1");
        atmbox.setAtmboxBoxno((short) 1);
        atmbox.setAtmboxBrnoSt("2");
        atmbox.setAtmboxCur("TWD");
        atmbox.setAtmboxDeposit(10000);
        atmbox.setAtmboxPresent(200000);
        atmbox.setAtmboxRefill(500);
        atmbox.setAtmboxReject(100);
        atmbox.setAtmboxRwtSeqno(300);
        atmbox.setAtmboxSettle((short) 1);
        atmbox.setAtmboxTxDate("20210507");
        atmbox.setAtmboxUnit(900000);
        atmbox.setAtmboxUnknown(40);
        atmbox.setUpdateTime(Calendar.getInstance().getTime());
        atmbox.setUpdateUserid(100000);
        atmbox.setAtmboxCur("TWD99999999999999999999999999999999999999999999");
        atmBoxMapper.deleteByPrimaryKey(atmbox);
    }

    @Test
    public void test() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = fepdbTransactionManager.getTransaction(definition);
        try {
            accountMapper.insert(accounts[0]);
            accountMapper.insert(accounts[1]);
            atmBoxMapper.insert(atmbox);
            fepdbTransactionManager.commit(txStatus);
        } catch (Exception e) {
            fepdbTransactionManager.rollback(txStatus);
        }
    }

    @Test
    public void test1() {
        accountMapper.insert(accounts[0]);
        fepdbTransactionTemplate.execute(txStatus -> {
            accountMapper.insert(accounts[1]);
            return null;
        });
    }
}
