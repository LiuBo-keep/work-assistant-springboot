package com.work.assistant.common.response;

import lombok.Getter;
import lombok.Setter;

/**
 * @author aidan.liu
 */
@Setter
@Getter
public class Pageable {

  /**
   * 前台传入的page参数，也就是页码数。前台传的0（页码显示1）。后台按0处理。
   * this.offset = page * size;
   */
  private Long pageIndex;

  /**
   * offset表示要跳过的数量
   */
  private Long offset;

  /**
   * 表示要取的的数量
   */
  private Long pageSize;

  public static Pageable of(Integer page, Integer size) {

    Pageable pageable = new Pageable();
    pageable.pageIndex = page.longValue();
    pageable.pageSize = size.longValue();
    pageable.offset = pageable.pageIndex * pageable.pageSize;
    return pageable;
  }
}
