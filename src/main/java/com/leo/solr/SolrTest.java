package com.leo.solr;

import com.leo.util.SolrUtil;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
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

    public static void main(String[] agrs) throws IOException, SolrServerException {
        //saveUserIndex();
        QueryResponse respone = SolrUtil.query("伏双利");
        SolrDocumentList docs = respone.getResults();
        System.out.println(docs);
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
        article.setId(UUIDGenerator.getUUID());
        article.setName("solr测试1");
        article.setContent("呼和浩特市长春药店");
        article.setCreateTime(new Date());
        SolrUtil.saveSolrResource(article);
        try {
            QueryResponse respone = SolrUtil.query("药店");
            SolrDocumentList docs = respone.getResults();
            System.out.println(docs);
            //SolrUtil.removeSolrData("10fb279b2c334c79a28b26ab5f651c94");
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
