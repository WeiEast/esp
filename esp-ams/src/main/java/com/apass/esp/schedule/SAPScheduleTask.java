package com.apass.esp.schedule;

import com.apass.esp.sap.SAPService;
import com.apass.gfb.framework.utils.DateFormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;

/**
 * Created by jie.xu on 17/10/17.
 */
@Component
@RequestMapping("/sap/test")
public class SAPScheduleTask {

  @Value("${ftp.ip}")
  private String ip;

  @Value("${ftp.port}")
  private int port;

  @Value("${ftp.username}")
  private String username;

  @Value("${ftp.password}")
  private String password;

  @Value("${ftp.base.path}")
  private String basePath;

  @Autowired
  private SAPService sapService;

  @RequestMapping("/test1")
  public void exec(){
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE,-1);
    String caiWuPingZhengPath = basePath + "/" + DateFormatUtil.dateToString(cal.getTime(),"yyyy/MM/dd") + "/";
    sapService.sendCaiWuPingZhengCsv(ip,port,username,password,caiWuPingZhengPath);
  }
}
