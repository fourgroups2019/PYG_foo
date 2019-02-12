package cn.itcast.core.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 作用：封装分页的结果集以及总条数
 */
// 为什么要实现序列化接口：用于数据的网络传输
// 其他场景：orm框架的缓存 pojo（将数据写到磁盘，容灾-灾备）  数据共享
public class PageResult implements Serializable{

    private List rows;  // 结果集
    private Long total; // 总条数

    public PageResult(List rows, Long total) {
        this.rows = rows;
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
