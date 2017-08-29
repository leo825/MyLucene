package com.leo.demo;

import com.leo.util.LuceneUtil;
import com.leo.util.PinyinUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.*;

/**
 * 查询，这个是基于lucene6版本
 *
 * Created by LX on 2017/8/25.
 */
public class Searcher6 {

    private static LuceneDao luceneDao = new LuceneDao();

    /**
     * 通过条件查询出某个feild的数据
     *
     * @param feild 索引项
     * @param keywords 关键字
     * @param pageIndex 分页的页码
     * @param pageSize 分页的大小
     * @return
     * @throws Exception
     */
    public static List<IndexObject> searchIndex(String feild, String keywords, int pageIndex,int pageSize) throws Exception {
        List<IndexObject> indexObjectList = new ArrayList<IndexObject>();
        String[] feilds = {feild};
        if (keywords != null && !"".equals(keywords)) {
            DirectoryReader reader = null;
            IndexSearcher indexSearcher = null;
            Analyzer analyzer = LuceneUtil.getAnalyzer();

            /* 创建一个搜索，搜索刚才创建的目录下的索引 */
            try {
                // 1、第一步，创建搜索目录的reader
                reader = DirectoryReader.open(LuceneUtil.getDirectory());
                // 2、第二步，创建搜索器
                indexSearcher = new IndexSearcher(reader);
                // 3、第三步，类似SQL，进行关键字查询
                QueryParser parser = new MultiFieldQueryParser(feilds, analyzer);

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

                /** 多条件必备神器通过bulid构建多条件 **/
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                /** 词语搜索,完全匹配,搜索具体的域 **/
                Query termQuery = new TermQuery(new Term(feild, keywords));
                /** 通配符查询 **/
                Query wildqQuery = new WildcardQuery(new Term(feild, keywords));
                /** 字段前缀搜索 **/
                Query prefixQuery = new PrefixQuery(new Term(feild, keywords));
                /** 相似度查询,模糊查询比如OpenOffica，OpenOffice **/
                Query fuzzyQuery = new FuzzyQuery(new Term(feild, keywords));

//                builder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);//SHOULD表示或的意思
//                builder.add(termQuery, BooleanClause.Occur.SHOULD);//SHOULD表示或的意思
//                builder.add(wildqQuery, BooleanClause.Occur.SHOULD);//SHOULD表示或的意思
//                builder.add(prefixQuery, BooleanClause.Occur.SHOULD);//SHOULD表示或的意思

//                TopDocs topDocs = indexSearcher.search( builder.build(),  topNum);

                /** 使用QueryParser查询分析器构造Query对象,使用这种方式的优势是很准确匹配到所需关键字**/
                QueryParser qp = new QueryParser(feild, analyzer);
                qp.setDefaultOperator(QueryParser.AND_OPERATOR);
                Query query = qp.parse(keywords);
                TopDocs topDocs = indexSearcher.search(query, 10000);
                int count = topDocs.totalHits;// 总记录数
                System.out.println("总记录数为：" + topDocs.totalHits);// 总记录数
                ScoreDoc[] hits = topDocs.scoreDocs;// 第二个参数，指定最多返回前n条结果

                int start = (pageIndex-1)*pageSize <=0 ? 0 : (pageIndex-1)*pageSize;
                int end = pageIndex*pageSize <= hits.length ? pageIndex*pageSize : hits.length;

                for (int i = start; i < end; i++) {
                    Document doc = indexSearcher.doc(hits[i].doc);
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

        List<Document> docList = new ArrayList<Document>();
        // 得到初始化索引文件目录
        Directory directory = LuceneUtil.getDirectory();
        if (directory != null) {
            if (list != null && list.size() > 0) {
                for(IndexObject e : list){
                    Document doc = new Document();
                    doc.add(new Field("id", e.getId(), TextField.TYPE_STORED));
                    doc.add(new Field("name", e.getName(), TextField.TYPE_STORED));
                    doc.add(new Field("content", e.getContent(), TextField.TYPE_STORED));
                    docList.add(doc);
                }
                if(docList != null && !docList.isEmpty()){
                    LuceneUtil.createIndex(docList);
                }
            }
        } else { // 索引存放路径不存在
            throw new IOException(directory + " does not exist or is not a directory");
        }
        long end = new Date().getTime();
        System.out.println(" InitIndexed: took " + (end - start) + " milliseconds");
    }


    //根据关键字查询对应的记录： 思路是，同关键字去索引查询到对应的记录Id,然而再通过id查询记录
    public static List<IndexObject> searchBykeyWord(String feild, String keywords, int pageIndex,int pageSize) throws Exception {
        Map map = new HashMap();

        long start = new Date().getTime();
        List<IndexObject> indexObjects = null;

        // 得到初始化索引文件目录
        Directory directory = LuceneUtil.getDirectory();
        indexObjects = Searcher6.searchIndex(feild, keywords, pageIndex,pageSize);
        long end = new Date().getTime();
        System.out.println(" searchBykeyWord 《" + keywords + "》  共花费：" + (end - start) + " milliseconds");
        if (indexObjects != null && indexObjects.size() > 0) {
            for (IndexObject indexObject : indexObjects) {
                System.out.println(" 查询获得:  " + indexObject.getName());
            }
        }
        return indexObjects;
    }


    /**
     * 对单个Entity对象（自定义对应数据库数据对象）进行索引
     *
     * @param writer
     * @param e  自定义javaBean对象，
     * @throws IOException
     */
    public static void indexEntity(IndexWriter writer, IndexObject e) throws IOException {
        if (e == null) {
            return;
        }
        Document doc = new Document();
        doc.add(new Field("id", e.getId(), TextField.TYPE_STORED));
        doc.add(new Field("name", e.getName(), TextField.TYPE_STORED));
        doc.add(new Field("content", e.getContent(), TextField.TYPE_STORED));
        LuceneUtil.createIndex(doc);
    }



    public static void main(String[] args) throws Exception {
        //initLuceneIndex();
        String feild = "name";
        String keywords = "";
        int pageIndex = 0;//第一页
        int pageSize=10;//每页10个

        searchBykeyWord(feild,keywords,pageIndex,pageSize);// 调用searchIndex方法进行查询
        searchBykeyWord(feild,PinyinUtils.getPingYin(keywords),pageIndex,pageSize);// 调用searchIndex方法进行查询
    }
}
