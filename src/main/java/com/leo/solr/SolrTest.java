package com.leo.solr;

import com.leo.util.SolrUtil;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.util.Date;

/**
 * Created by LX on 2017/9/4.
 */
public class SolrTest {

    public static void main(String[] agrs){
        Article article = new Article();
        article.setId(UUIDGenerator.getUUID());
        article.setName("solr测试1");
        article.setContent("吉林市长春药店");
        article.setCreateTime(new Date());
        //SolrUtil.saveSolrResource(article);
        try {
            QueryResponse respone = SolrUtil.query("药店");
            SolrDocumentList docs = respone.getResults();
            System.out.println(docs);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
