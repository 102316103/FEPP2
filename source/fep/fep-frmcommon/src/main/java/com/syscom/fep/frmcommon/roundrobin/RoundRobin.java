package com.syscom.fep.frmcommon.roundrobin;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundRobin輪詢
 *
 * @param <T>
 */
public class RoundRobin<T> {
    private final LogHelper logger = new LogHelper();
    /**
     * 數據列表
     */
    private final List<T> list = Collections.synchronizedList(new ArrayList<>());
    /**
     * 下一筆數據的index
     */
    private final AtomicInteger next = new AtomicInteger(0);
    /**
     * 遞增還是遞減
     */
    private RoundRobinAccumulate accumulate = RoundRobinAccumulate.Increment;
    /**
     * 是否已經完成了一次輪詢
     */
    private final AtomicBoolean roundRobinAll = new AtomicBoolean(false);

    public RoundRobin() {}

    public RoundRobin(final List<T> list) {
        this(list, RoundRobinAccumulate.Increment);
    }

    public RoundRobin(final List<T> list, final RoundRobinAccumulate accumulate) {
        this.addAll(list, accumulate);
    }

    /**
     * 增加一筆數據
     *
     * @param t
     */
    public void add(T t) {
        list.add(t);
        this.reset();
    }

    /**
     * 增加一個list, 並指定遞增還是遞減
     *
     * @param list
     * @param accumulate
     */
    public void addAll(final List<T> list, final RoundRobinAccumulate accumulate) {
        this.list.addAll(list);
        this.accumulate = accumulate;
        this.reset();
    }

    /**
     * 設定遞增還是遞減
     *
     * @param accumulate
     */
    public void setAccumulate(RoundRobinAccumulate accumulate) {
        this.accumulate = accumulate;
    }

    /**
     * 輪詢取出下一筆數據
     *
     * @return
     */
    public T select() {
        if (CollectionUtils.isEmpty(this.list))
            return null;
        T t = null;
        try {
            if (accumulate == RoundRobinAccumulate.Increment)
                t = this.list.get(this.next.getAndIncrement() % this.list.size());
            else
                t = this.list.get(this.next.getAndDecrement() % this.list.size());
        } catch (Exception e) {
            logger.warn(e, e.getMessage());
            t = this.list.get(new Random().nextInt(this.list.size()));
        } finally {
            // 遞增有完成一次輪詢
            if (this.next.get() >= this.list.size()) {
                this.next.set(0);
                this.roundRobinAll.set(true);
            }
            // 遞減有完成一次輪詢
            else if (next.get() <= -1) {
                this.next.set(this.list.size() - 1);
                this.roundRobinAll.set(true);
            } else {
                this.roundRobinAll.set(false);
            }
        }
        return t;
    }

    /**
     * 返回是否有完成一次輪詢
     *
     * @return
     */
    public boolean isRoundRobinAll() {
        return this.roundRobinAll.get();
    }

    /**
     * 重置
     */
    public void reset() {
        if (this.accumulate == RoundRobinAccumulate.Increment) {
            this.next.set(0);
        } else {
            this.next.set(this.list.size() - 1);
        }
        this.roundRobinAll.set(false);
    }

    /**
     * 取出指定index的值
     *
     * @param index
     * @return
     */
    public T get(int index) {
        if (CollectionUtils.isEmpty(this.list))
            return null;
        return this.list.get(index);
    }
}
