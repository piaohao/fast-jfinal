package org.piaohao.fast.jfinal;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.IoUtil;
import com.jfinal.render.ErrorRender;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class StaticServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURI = req.getRequestURI();
        if (!requestURI.startsWith("/" + DefaultConfig.staticPath)) {
            new ErrorRender(404, null).setContext(req, resp).render();
            return;
        }
        StaticManager.StaticInfo staticInfo = StaticManager.get(requestURI);
        if (staticInfo == null) {
            new ErrorRender(404, null).setContext(req, resp).render();
            return;
        }
        String timeStr = req.getHeader("If-Modified-Since");
        if (timeStr == null) {
            renderStatic(requestURI, staticInfo, req, resp);
            return;
        }
        DateTime time = new DateTime(Long.valueOf(timeStr));
        if (!new DateTime(staticInfo.getLastModifyTime()).after(time)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        renderStatic(requestURI, staticInfo, req, resp);
    }

    private void renderStatic(String requestURI, StaticManager.StaticInfo staticInfo,
                              HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InputStream inputStream = Util.getInputStream(requestURI);
        resp.setHeader("ETag", String.valueOf(staticInfo.getSize()));
        resp.setHeader("Last-Modified", String.valueOf(staticInfo.getLastModifyTime()));
        if (inputStream == null) {
            new ErrorRender(404, null).setContext(req, resp).render();
            return;
        }
        IoUtil.copy(inputStream, resp.getOutputStream());
    }

}