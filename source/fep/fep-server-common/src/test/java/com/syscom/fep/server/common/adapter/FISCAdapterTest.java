package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.FISCData;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.server.common.ServerCommonBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FISCAdapterTest extends ServerCommonBaseTest {
    private FISCAdapter adapter;

    @BeforeEach
    public void setup() {
        FISCData data = new FISCData();
        data.setRClientID("B006A0101");
        data.setLogContext(new LogData());
        adapter = new FISCAdapter(data);
        adapter.setStan("5602573");
        adapter.setTimeout(10);
    }

    @Test
    public void test() throws InterruptedException {
        adapter.setMessageToFISC("000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA");
        adapter.sendReceive();
        UnitTestLogger.info(adapter.getMessageFromFISC());
//        adapter.sendReceive();
//        UnitTestLogger.info(adapter.getMessageFromFISC());
//        adapter.sendReceive();
//        UnitTestLogger.info(adapter.getMessageFromFISC());
        Thread.sleep(Long.MAX_VALUE);
    }

    @Test
    public void test2() {
        new Thread("t1") {
            public void run() {
                FISCAdapter adapter = new FISCAdapter(new FISCData());
                adapter.setTimeout(70);
                adapter.setMessageToFISC("000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA");
                adapter.sendReceive();
                UnitTestLogger.info(adapter.getMessageFromFISC());
            }
        }.start();
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread("t2") {
            public void run() {
                FISCAdapter adapter = new FISCAdapter(new FISCData());
                adapter.setTimeout(70);
                adapter.setMessageToFISC("000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA");
                adapter.sendReceive();
                UnitTestLogger.info(adapter.getMessageFromFISC());
            }
        }.start();
        new Thread("t3") {
            public void run() {
                FISCAdapter adapter = new FISCAdapter(new FISCData());
                adapter.setTimeout(70);
                adapter.setMessageToFISC("000000303230303231333035363032353733393530303030303830373030303031303039303831353036343130303030E5905ED024008004000008012B303030303030303034393930303033393435303030303131303039303738303735333535363330AA7822AA");
                adapter.sendReceive();
                UnitTestLogger.info(adapter.getMessageFromFISC());
            }
        }.start();
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
