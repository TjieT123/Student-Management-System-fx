package cn.edu.sdu.sms.fx.smsfx.models;

import java.util.List;

/**
 * 通用分页结果包装类
 */
public class PageResult<T> {
    private Integer total;
    private Integer page;
    private Integer pageSize;
    private List<T> list;

    public PageResult() {}

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
}
