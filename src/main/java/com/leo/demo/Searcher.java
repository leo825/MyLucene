package com.leo.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

/**
 * 查询
 *
 * Created by LX on 2017/8/25.
 */
public class Searcher {

    private static String K_FEILD = "name"; // 关键词 对应 索引的域

    private static int TOP_NUM = 5;// 显示前5条结果

    private static LuceneDao luceneDao = new LuceneDao();

    /***
     * 多种匹配词--查询
     *
     * @param keywords
     * @throws Exception
     */
    public static List<IndexObject> searchIndex(Directory diretory, String keywords) throws Exception {
        List<IndexObject> indexObjectList = new ArrayList<IndexObject>();
        if (keywords != null && !"".equals(keywords)) {
            IndexSearcher indexSearcher = null;
            MultiSearcher searcher = null;

            /* 创建一个搜索，搜索刚才创建的目录下的索引 */
            try {
                indexSearcher = new IndexSearcher(diretory, true);
                // read-only
                /* 在这里我们只需要搜索一个目录 */
                IndexSearcher indexSearchers[] = { indexSearcher };
                /* 我们需要搜索两个域ArticleTitle, ArticleText里面的内容 */
                String[] fields = { K_FEILD };
                /*
                 * 下面这个表示要同时搜索这两个域，而且只要一个域里面有满足我们搜索的内容就行
                 * BooleanClause.Occur[]数组,它表示多个条件之间的关系
                 * ,BooleanClause.Occur.MUST表示and,
                 * BooleanClause.Occur.MUST_NOT表示not
                 * ,BooleanClause.Occur.SHOULD表示or. 1、MUST和MUST表示“与”的关系，即“交集”。
                 * 2、MUST和MUST_NOT前者包含后者不包含。 3、MUST_NOT和MUST_NOT没意义
                 * 4、SHOULD与MUST表示MUST，SHOULD失去意义；
                 * 5、SHOUlD与MUST_NOT相当于MUST与MUST_NOT。 6、SHOULD与SHOULD表示“或”的概念
                 */
                BooleanClause.Occur[] clauses = { BooleanClause.Occur.SHOULD };
                /*
                 * MultiFieldQueryParser表示多个域解析， 同时可以解析含空格的字符串，如果我们搜索"上海 中国"
                 */
                Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT); // 创建一个语法分析器
                // ，Lucene3.0之后
                // 有变化的地方
                Query multiFieldQuery = MultiFieldQueryParser.parse(Version.LUCENE_CURRENT, keywords, fields, clauses, analyzer);
                Query termQuery = new TermQuery(new Term(K_FEILD, keywords));// 词语搜索,完全匹配,搜索具体的域

                Query wildqQuery = new WildcardQuery(new Term(K_FEILD, keywords));// 通配符查询

                Query prefixQuery = new PrefixQuery(new Term(K_FEILD, keywords));// 字段前缀搜索

                Query fuzzyQuery = new FuzzyQuery(new Term(K_FEILD, keywords));// 相似度查询,模糊查询比如OpenOffica，OpenOffice

                /* Multisearcher表示多目录搜索，在这里我们只有一个目录 */
                searcher = new MultiSearcher(indexSearchers);
                // 多条件搜索
                BooleanQuery multiQuery = new BooleanQuery();

                multiQuery.add(wildqQuery, BooleanClause.Occur.SHOULD);
                multiQuery.add(multiFieldQuery, BooleanClause.Occur.SHOULD);
                multiQuery.add(termQuery, BooleanClause.Occur.SHOULD);
                multiQuery.add(prefixQuery, BooleanClause.Occur.SHOULD);
                multiQuery.add(fuzzyQuery, BooleanClause.Occur.SHOULD);

                /* 开始搜索 */
                TopScoreDocCollector collector = TopScoreDocCollector.create(TOP_NUM, false);// Lucene3.0之后 有变化的地方

                searcher.search(multiQuery, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                for (int i = 0; i < hits.length; i++) {

                    Document doc = searcher.doc(hits[i].doc);// new method
                    IndexObject object = new IndexObject();
                    object.setId(doc.getField("id").stringValue());
                    object.setName(doc.getField("name").stringValue());
                    object.setContent(doc.getField("content").stringValue());
                    indexObjectList.add(object);
                }
            } catch (CorruptIndexException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } finally {
                if (searcher != null) {
                    try {
                        /* 关闭 */
                        searcher.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (indexSearcher != null) {
                    try {
                        indexSearcher.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return indexObjectList;
    }

    /**
     * 将数据库中的关键字初始化成索引值
     * @throws Exception
     */
    public static void initLuceneIndex() throws Exception {
        Map map = new HashMap();
        long start = new Date().getTime();
        //这里 我是调用了Dao持久层数据方法，使用了MVC的朋友应该知道.
        List<IndexObject> list = luceneDao.initLuceneIndex();

        // 得到初始化索引文件目录
        Directory directory = new DBDataIndexer().initLuceneDirctory();
        if (directory != null) {

            IndexWriter writer = DBDataIndexer.initLuceneObj(directory, true);
            if (list != null && list.size() > 0) {
                for (IndexObject object : list) {
                    DBDataIndexer.indexEntity(writer, object);
                }
            }
            DBDataIndexer.closeIndexWriter(writer, directory);
        } else { // 索引存放路径不存在
            throw new IOException(directory + " does not exist or is not a directory");
        }
        long end = new Date().getTime();
        System.out.println(" InitIndexed: took " + (end - start) + " milliseconds");
    }


    //根据关键字查询对应的记录： 思路是，同关键字去索引查询到对应的记录Id,然而再通过id查询记录
    public static List<IndexObject> searchBykeyWord(String keywords) throws Exception {
        Map map = new HashMap();

        long start = new Date().getTime();
        List<IndexObject> indexObjects = null;

        // 得到初始化索引文件目录
        Directory directory = new DBDataIndexer().initLuceneDirctory();
        indexObjects = Searcher.searchIndex(directory, keywords);
        long end = new Date().getTime();
        System.out.println(" rearcherBykeyWord 《 " + keywords + "》  共花费：" + (end - start) + " milliseconds");
        if (indexObjects != null && indexObjects.size() > 0) {
            for (IndexObject indexObject : indexObjects) {
                System.out.println(" 查询获得:  " + indexObject.getName());
            }
        }
        return indexObjects;
    }




    public static void main(String[] args) throws Exception {
        initLuceneIndex();
        searchBykeyWord("伏双利");// 调用searchIndex方法进行查询
    }
}
