package com.work.assistant.common.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author aidan.liu
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResponse<T> {

  PageDto page;

  List<T> data;

  public PageResponse(Pageable pageable, long count, List<T> data) {

    page = new PageDto(pageable, count, data.size());
    this.data = data;
  }

  public PageResponse(Pageable pageable, long count, long loadCount) {

    page = new PageDto(pageable, count, loadCount);
  }

  @Data
  public static class PageDto {

    /**
     * 加载出来的数据条数.
     */
    private long loadCount = 0;
    /**
     * 一共有多少条数.
     */
    private long totalCount = 0;
    /**
     * 一共有多少页.
     */
    private long totalPage = 0;
    /**
     * 每个分页的数据条数.
     */
    private long pageSize = 0;
    /**
     * 当前一共有多少分页.
     */
    private long pageIndex = 0;

    public PageDto() {
    }

    public PageDto(Pageable pageable, long count, long loadCount) {

      this.setTotalPage(
          pageable.getPageSize() == 0 ? 1 : (int) Math.ceil((double) count / (double) pageable.getPageSize()));
      this.setPageIndex(pageable.getPageIndex());
      this.setPageSize(pageable.getPageSize());

      this.setTotalCount(count);
      this.setLoadCount(loadCount);
    }
  }
}

