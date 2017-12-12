package com.leo.util;

/**
 * Created by LX on 2017/9/4.
 */

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * solr工具类
 *
 */
public class SolrUtil {
    private static SolrClient client;
    private static String url = "http://10.1.106.77:8983/solr/articles";
    static {
        client = new HttpSolrClient.Builder(url).build();
    }

    /**
     * 保存或者更新solr数据
     *
     * @param solrEntity
     */
    public static <T> boolean saveSolrResource(T solrEntity) {

        DocumentObjectBinder binder = new DocumentObjectBinder();
        SolrInputDocument doc = binder.toSolrInputDocument(solrEntity);
        try {
            client.add(doc);
            client.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 批量保存或者更新solr数据
     *
     * @param solrEntitys
     */
    public static <T> boolean saveSolrResources(List<T> solrEntitys) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        ArrayList<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        for(T solrEntity : solrEntitys){
            SolrInputDocument doc = binder.toSolrInputDocument(solrEntity);
            docs.add(doc);
        }
        try {
            client.add(docs);
            client.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除solr 数据
     *
     * @param id
     */
    public static boolean removeSolrData(String id) {
        try {
            client.deleteById(id);
            client.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除solr 数据
     *
     * @param ids
     */
    public static boolean removeSolrDatas(List<String> ids) {
        try {
            client.deleteById(ids);
            client.commit();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 查询
     *
     * @param keywords
     */
    public static QueryResponse query(String keywords) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery();
        query.setQuery(keywords);
        QueryResponse rsp = client.query(query);
        return rsp;
    }

}