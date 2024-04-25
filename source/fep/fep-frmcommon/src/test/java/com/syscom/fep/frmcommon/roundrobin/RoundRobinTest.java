package com.syscom.fep.frmcommon.roundrobin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class RoundRobinTest {
    private RoundRobin<String> roundRobin;

    @BeforeEach
    public void setup() {
        roundRobin = new RoundRobin<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"), RoundRobinAccumulate.Decrement);
    }

    @Test
    public void test() {
        int count = 0;
        int time = 0;
        while (count < 3) {
            System.out.println(roundRobin.select());
            if (time++ == 4) {
                roundRobin.reset();
                System.out.println("*********************************");
            }
            if (roundRobin.isRoundRobinAll()) {
                count++;
                System.out.println("=================================");
            }
        }
    }
}
