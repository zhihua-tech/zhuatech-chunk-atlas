/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.chunkatlas;

import static cn.zhuatech.chunkatlas.Checks.*;

import java.util.*;

/**
 * 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 按长度预算和段落边界切片，生成原文坐标、重叠范围和内容摘要，可用坐标回查原文。
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> rows) {
    unique(rows);
    int size = integer(cfg, "size", 4, 4096), overlap = integer(cfg, "overlap", 0, size - 1);
    must(overlap <= size / 2, "重叠不得超过窗口一半");
    var out = new ArrayList<>();
    int units = 0;
    for (var r : rows) {
      String text = str(r, "text", false);
      int[] cp = text.codePoints().toArray();
      units += cp.length;
      int index = 0;
      for (int start = 0; start < cp.length; ) {
        int end = Math.min(start + size, cp.length);
        if (end < cp.length)
          for (int j = end - 1; j >= start + size / 2; j--) {
            if (cp[j] == '\n' || cp[j] == '。' || cp[j] == '！' || cp[j] == '？') {
              end = j + 1;
              break;
            }
          }
        String chunk = new String(cp, start, end - start);
        out.add(
            Map.of(
                "id",
                r.get("id"),
                "part",
                ++index,
                "start",
                start,
                "end",
                end,
                "overlap",
                index == 1 ? 0 : overlap,
                "text",
                chunk,
                "sha256",
                hash(chunk)));
        must(out.size() <= 3000, "超过 3000 个切片，请增大窗口或拆批");
        if (end == cp.length) break;
        start = end - overlap;
      }
    }
    return result(
        Map.of("documents", rows.size(), "chunks", out.size(), "sourceUnits", units), out);
  }
}
