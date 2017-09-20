package com.leo.solr;

import com.leo.util.SolrUtil;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

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

    public static void main(String[] agrs) throws IOException, SolrServerException, IntrospectionException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
//        saveUserIndex();
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
//        saveUserIndex();

        String bat = "D:\\Library\\solr-6.1.0\\bin\\solr.cmd create -c 1123";
        String strcmd = "cmd /c "+bat;  //调用我们在项目目录下准备好的bat文件，如果不是在项目目录下，则把“你的文件名.bat”改成文件所在路径。
        runCmd(strcmd);  //调用上面的run_cmd方法执行操作

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
        article.setName("solr测试222");
        article.setContent("呼和浩特市长春药店");
        article.setCreateTime(new Date());
        //SolrUtil.saveSolrResource(article);
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
}
