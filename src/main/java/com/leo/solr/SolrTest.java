package com.leo.solr;

import com.leo.util.SolrUtil;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by LX on 2017/9/4.
 */


/**
 *http://localhost:8983/solr/articles/schema
 *
 *  //通过一下方式添加索引字段
 *  {
        "add-field" : {
            "name" : "name",
            "type" : "text_ik",
            "stored" : "true"
        },
        "add-field" : {
            "name" : "content",
            "type" : "text_ik",
            "stored" : "true"
        }
    }

    //通过下面方式删除索引字段
    {
        "delete-field" : {
            "name" : "name"
        },
        "delete-field" : {
            "name" : "content"
        }
    }

 *
 *
 */

public class SolrTest {

    public static ConcurrentLinkedQueue<ThreadTest> threadMap = new ConcurrentLinkedQueue<ThreadTest>();

    public static void main(String[] agrs) throws Exception {
//        QueryResponse respone = SolrUtil.query("\"侯跃龙\"");
//        long start = new Date().getTime();
//        SolrDocumentList docs = respone.getResults();
//        System.out.println(docs);
//        long end = new Date().getTime();
//        System.out.println("查询共耗时: "+(end-start)+"ms ");
//        UUIDGenerator uu = new UUIDGenerator();
//        Method create = uu.getClass().getDeclaredMethod("getUUID");
//        Object value = create.invoke(uu);
//        System.out.println((String)value);

        //test();



//        String bat = "D:\\Library\\solr-6.1.0\\bin\\solr.cmd create -c 1123";
//        String strcmd = "cmd /c "+bat;  //调用我们在项目目录下准备好的bat文件，如果不是在项目目录下，则把“你的文件名.bat”改成文件所在路径。
//        runCmd(strcmd);  //调用上面的run_cmd方法执行操作

//        ThreadTest test1 = new ThreadTest("线程1");
//        //test1.start();
//        ThreadTest test2 = new ThreadTest("线程2");
//        //test2.start();
//
//        threadMap.add(test1);
//        threadMap.add(test2);
//
//        while (true){
//            if (!threadMap.isEmpty()){
//                ThreadTest test = threadMap.poll();
//                test.start();
//                while (test.isAlive()){
//                    System.out.println(test.getName()+": 还么有执行完");
//                    Thread.sleep(2000);
//                }
//                System.out.println(test.getName()+": 执行完毕了！！！！！！！");
//            }
//        }


        createCore("nmqys_1009","window");

    }



    public static void saveUserIndex(){
        SolrDao dao = new SolrDao();
        List<Article> articles = dao.initSolrIndex();
        long start = new Date().getTime();
        SolrUtil.saveSolrResources(articles);
        long end = new Date().getTime();
        System.out.println("创建索引共耗时: "+(end-start)+"ms ");
    }

    public static void test(){
        Article article = new Article();
        article.setId("1114272fb6e449e2b5bcec10f7998839");
        article.setName("solr测试222222222222222222222");
        article.setContent("呼和浩特市金山药店");
        article.setCreateTime(new Date());
        SolrUtil.saveSolrResource(article);
        try {
            QueryResponse respone = SolrUtil.query("solr测试");
            SolrDocumentList docs = respone.getResults();
            System.out.println(docs);
            //SolrUtil.removeSolrData("10fb279b2c334c79a28b26ab5f651c94");
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行window下的命令
     */
    public static void runCmd(String strcmd) throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime(); //Runtime.getRuntime()返回当前应用程序的Runtime对象
        Process ps = null;  //Process可以控制该子进程的执行或获取该子进程的信息。
        ps = rt.exec(strcmd);   //该对象的exec()方法指示Java虚拟机创建一个子进程执行指定的可执行程序，并返回与该子进程对应的Process对象实例。
        ps.waitFor();  //等待子进程完成再往下执行。
        int i = ps.exitValue();  //接收执行完毕的返回值
        if (i == 0) {
            System.out.println("执行完成.");
        } else {
            System.out.println("执行失败.");
        }
        ps.destroy();  //销毁子进程
        ps = null;
    }

    public static boolean createCore(String coreName,String osType) throws Exception {
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
