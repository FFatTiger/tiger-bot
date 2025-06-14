package com.fffattiger.wechatbot.api;

public interface Ordered extends Comparable<Ordered> {

    /**
     * 定义处理器的执行顺序。
     * <p>
     * 拥有较小值的处理器会更早被执行。
     *
     * @return order 值，默认为 1000。
     */
    default int getOrder() {
        return 1000;
    }
    
    @Override
    default int compareTo(Ordered other) {
        return Integer.compare(this.getOrder(), other.getOrder());
    }
}
