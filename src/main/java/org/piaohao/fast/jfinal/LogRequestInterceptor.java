package org.piaohao.fast.jfinal;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HttpUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 请求详情拦截器
 *
 * @author piaohao
 */
@Slf4j
public class LogRequestInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String actionKey = inv.getActionKey();
        long start = System.currentTimeMillis();
        try {
            inv.invoke();
        } finally {
            Map<String, String[]> paraMap = controller.getParaMap();
            StringBuilder builder = new StringBuilder("{");
            boolean isFirst = true;
            for (Map.Entry<String, String[]> entry: paraMap.entrySet()) {
                String key = entry.getKey();
                String[] arr = entry.getValue();
                if (!isFirst) {
                    builder.append(",");
                } else {
                    isFirst = false;
                }
                builder.append(key).append(":");
                if (arr != null) {
                    if (key.equals("base64")) {
                        builder.append("[BASE64数据]");
                    } else {
                        int len = 0;
                        for (String a: arr) {
                            if (a != null) {
                                len += a.length();
                            }
                        }
                        if (len > 200) {
                            builder.append("[数据长度大于200]");
                        } else {
                            builder.append(arr[0]);
                        }
                    }
                } else {
                    builder.append("null");
                }
            }
            builder.append("}");
            log.info("[IP:{}],[route:{}],[params:{}],[共耗时:{}毫秒]",
                    HttpUtil.getClientIP(inv.getController().getRequest()),
                    actionKey,
                    builder.toString(),
                    System.currentTimeMillis() - start);
        }
    }

}