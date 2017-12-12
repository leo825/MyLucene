package com.leo.core;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.util.NamedList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by LX on 2017/12/12.
 */
public class CreateCoreServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String message = "";
        String coreName = request.getParameter("coreName");
        if (coreName == null || "".equals(coreName)){
            message = "{\"result\":false,\"message\":\"coreName is null\"}";
        }else{
            try {
                if(createCore(coreName,"window")){
                    message = "{\"result\":true,\"message\":\""+coreName+" create success！\"}";
                }else{
                    message = "{\"result\":false,\"message\":\" create failed！\"}";
                }
            } catch (Exception e) {
               message = "{\"result\":false,\"message\":\""+e.getMessage()+"\"}";
            }
        }
        response.getWriter().print(message);
        response.getWriter().close();
    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        doPost(request,response);
    }

    public boolean createCore(String coreName,String osType) throws Exception {
        String fileSeparator = "";
        if("".equals(osType) || osType == null){
            throw new Exception("osType 不能为空");
        }else{
            if("window".equals(osType)){
                fileSeparator = "\\";
            }else{
                fileSeparator = "/";
            }
        }
        String url = "http://localhost:8983/solr";
        // / 连接solr服务器
        SolrClient server = new HttpSolrClient.Builder(url).build();
        String DEFAULT_CORE_NAME = "coreModel";
        // 获得solr.xml配置好的cores作为默认，获得默认core的路径
        NamedList<Object> list = CoreAdminRequest.getStatus(DEFAULT_CORE_NAME, server).getCoreStatus().get(DEFAULT_CORE_NAME);
        String path = (String) list.get("instanceDir") + fileSeparator;

        // 获得solrhome,也就是solr放置索引的主目录
        String solrHome = path.substring(0, path.indexOf(DEFAULT_CORE_NAME));

        // 建立新core所在文件夹
        File core = new File(solrHome + fileSeparator + coreName);
        if (!core.exists()) {
            core.mkdir();
        }
        // 将默认core下conf里的solrconfig.xml和schema.xml拷贝到新core的conf下。这步是必须的
        // 因为新建的core solr会去其conf文件夹下找这两个文件，如果没有就会报错，新core则不会创建成功
        FileUtils.copyDirectoryToDirectory(new File(path + "conf"), core);
        // 创建新core,同时会把新core的信息添加到solr.xml里
        CoreAdminResponse resp = CoreAdminRequest.createCore(coreName,core.getAbsolutePath(),server);
        if(resp.getStatus() == 0){
            return true;
        }
        return false;
    }
}
