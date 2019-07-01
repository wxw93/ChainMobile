package cn.sheep.cms.servlet;

import cn.sheep.cms.beans.PerMinuteInfo;
import cn.sheep.cms.beans.ProvinceInfo;
import cn.sheep.cms.service.ReportService;
import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by ThinkPad on 2017/8/17.
 */
//@WebServlet(name = "Servlet", urlPatterns = {"/test.do"})
public class ReportServlet extends BaseServlet {
    private ReportService reportService = new ReportService();


    public void getProvInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception{

        String day = req.getParameter("day");
        System.out.println("day = " + day);

        List<ProvinceInfo> chargeFailProv = reportService.provinceReChargeFail(day);

        resp.getWriter().write(JSONObject.toJSONString(chargeFailProv));
    }


    public void getPerMinuteInfo(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        SimpleDateFormat format = new SimpleDateFormat("HHmm");
        String key = req.getParameter("k");
        Date date = new Date();
        key += format.format(date);
        System.out.println("key = " + key);

        PerMinuteInfo pmi = reportService.perMinuteReChargeAndMoney(key);
        resp.getWriter().write(JSONObject.toJSONString(pmi));
    }
}
