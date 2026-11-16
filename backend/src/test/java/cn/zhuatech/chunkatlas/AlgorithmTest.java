/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.chunkatlas;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@SuppressWarnings("unchecked")
class AlgorithmTest {
  Map<String, Object> cfg;
  List<Map<String, Object>> data;

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @BeforeEach
  void load() throws Exception {
    var fixture =
        new ObjectMapper()
            .readValue(getClass().getResourceAsStream("/static/catalog.json"), Map.class);
    cfg = (Map<String, Object>) fixture.get("settings");
    data = (List<Map<String, Object>>) fixture.get("rows");
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void boundaryCases() {
    for (int n = 1; n < 90; n++) {
      String text = "甲😀\n。".repeat(n);
      var x =
          Algorithm.compute(
              Map.of("size", 8, "overlap", 4), List.of(Map.of("id", "s", "text", text)));
      int end = 0;
      for (var r : (List<Map<String, Object>>) x.get("rows")) {
        assertTrue((int) r.get("start") <= end);
        assertTrue((int) r.get("end") > end);
        end = (int) r.get("end");
      }
      assertEquals(text.codePointCount(0, text.length()), end);
    }
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void enterpriseEvidenceAndBoundaries() {
    var result=Algorithm.compute(Map.of("size",4,"overlap",2),List.of(Map.of("id","x","text","😀abc😀abc")));
    var m=(Map<?,?>)result.get("metrics");assertEquals(8,m.get("sourceUnits"));
    assertEquals(12,m.get("indexedUnits"));assertEquals(4,m.get("overlapUnits"));assertEquals(1,m.get("repeatChunks"));
    var rows=(List<Map<String,Object>>)result.get("rows");assertEquals(true,rows.get(2).get("repeatContent"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void knownResults() {
    var result = Algorithm.compute(cfg, data);
    var m = (Map<String, Object>) result.get("metrics");
    var out = (List<Map<String, Object>>) result.get("rows");
    var first = out.getFirst();
    assertEquals(1, ((Number) m.get("documents")).intValue());
    String src = (String) data.getFirst().get("text");
    for (var row : out) {
      int a = (int) row.get("start"), b = (int) row.get("end");
      assertEquals(
          src.substring(src.offsetByCodePoints(0, a), src.offsetByCodePoints(0, b)),
          row.get("text"));
    }
    assertEquals(src.codePointCount(0, src.length()), out.getLast().get("end"));
    var x =
        Algorithm.compute(
            Map.of("size", 4, "overlap", 1), List.of(Map.of("id", "u", "text", "甲😀乙丙丁戊")));
    assertEquals("甲😀乙丙", ((List<Map<String, Object>>) x.get("rows")).getFirst().get("text"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsInvalidInput() {
    cfg.put("overlap", 25);
    assertThrows(Model.Failure.class, () -> Algorithm.compute(cfg, data));
  }
}
