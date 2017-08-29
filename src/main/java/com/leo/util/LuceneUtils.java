package com.leo.util;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;


/**
 * lucene工具类，采用IKAnalyzer中文分词器
 *
 */
public class LuceneUtils {
    private static final Logger log=Logger.getLogger(LuceneUtils.class);
    /** 索引库路径 */
    private static final String indexPath ="D:\\Library\\lucence\\index1";

    /** 初始化indexWriter **/
    public static IndexWriter indexWriter = null;

    /** 初始化fsDirectory **/
    public static Directory fsDirectory = null;

    /** 初始化分词器 **/
    public static Analyzer analyzer = null;

    /** 初始化赋值 **/
    static {
        //索引库路径不存在则新建一个
        File indexFile=new File(indexPath);
        if(!indexFile.exists()) indexFile.mkdir();
        try {
            fsDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexPath));
            analyzer = new StandardAnalyzer();
            indexWriter =new IndexWriter(fsDirectory, new IndexWriterConfig(analyzer));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /** 获取directory对象 **/
    public static Directory getDirectory() throws IOException {
        if (fsDirectory == null) {
            fsDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexPath));
        }
        return fsDirectory;
    }


    /**
     * 获取indexWriter
     *
     * @return
     */
    public static IndexWriter getIndexWriter(){
        if(indexWriter == null){
            try {
                IndexWriterConfig confIndex = new IndexWriterConfig(getAnalyzer());
                confIndex.setOpenMode(OpenMode.CREATE_OR_APPEND);
                indexWriter =new IndexWriter(getDirectory(), confIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return indexWriter;
    }

    /**
     * 获取分词器,以后可以扩展成其他分词器。例如：ikAnalyzer或者paoding
     * 此处将ikAnalyzer整合进lucene
     *
     * StandardAnalyzer搜索反而比ikAnalyzer更快一些，可能是ikAnalyzer需要做分词处理消耗时间
     * @return
     */
    public static Analyzer getAnalyzer(){
        if(analyzer == null){
            analyzer = new StandardAnalyzer();
        }
        return analyzer;
    }

    /**
     * 创建索引
     *
     * @param doc
     * @throws Exception
     */
    public static boolean createIndex(Document doc) {
        List<Document> docs = new ArrayList<Document>();
        docs.add(doc);
        return createIndex(docs);
    }

    /**
     * 创建索引
     *
     * @param docs
     * @throws Exception
     */
    public static boolean createIndex(List<Document> docs) {
        try {
            getIndexWriter().addDocuments(docs);
            // 优化操作
            getIndexWriter().commit();
            getIndexWriter().forceMerge(1); // forceMerge代替optimize
            log.info("lucene create success.");
            return true;
        } catch (Exception e) {
            log.error("lucene create failure.", e);
            return false;
        } finally {
            if (getIndexWriter() != null) {
                try {
                    getIndexWriter().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新索引
     *
     * 例如：Term term = new Term("id","1234567");
     * 先去索引文件里查找id为1234567的Document，如果有就更新它(如果有多条，最后更新后只有一条)，如果没有就新增。
     * 数据库更新的时候，我们可以只针对某个列来更新，而lucene只能针对一行数据更新。
     *
     * @param field Document的Field(类似数据库的字段)
     * @param value Field中的一个关键词
     * @param doc
     * @return
     */
    public static boolean updateIndex(String field, String value, Document doc) {
        try {
            indexWriter.updateDocument(new Term(field, value), doc);
            log.info("lucene update success.");
            return true;
        } catch (Exception e) {
            log.error("lucene update failure.", e);
            return false;
        }finally{
            if(getIndexWriter()!=null){
                try {
                    getIndexWriter().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除索引
     *
     * @param field Document的Field(类似数据库的字段)
     * @param value Field中的一个关键词
     * @return
     */
    public static boolean deleteIndex(String field, String value) {
        try {
            indexWriter.deleteDocuments(new Term(field, value));
            log.info("lucene delete success.");
            return true;
        } catch (Exception e) {
            log.error("lucene delete failure.", e);
            return false;
        }finally{
            if(getIndexWriter()!=null){
                try {
                    indexWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除整个索引库
     *
     * @return
     */
    public static boolean deleteAllIndex() {
        try {
            indexWriter.deleteAll();
            log.info("lucene delete all success.");
            return true;
        } catch (Exception e) {
            log.error("lucene delete all failure.", e);
            return false;
        }finally{
            if(indexWriter != null){
                try {
                    indexWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 判断索引库是否已创建
     *
     * @return true:存在，false：不存在
     * @throws Exception
     */
    public static boolean existsIndex() throws Exception {
        File file = new File(indexPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String indexSufix = "/segments.gen";
        // 根据索引文件segments.gen是否存在判断是否是第一次创建索引
        File indexFile = new File(indexPath + indexSufix);
        return indexFile.exists();
    }


    /**
     * 获得本次创建/增量索引的原数据数量（条）
     * @return
     * @throws IOException
     */
    public static int getNumDocs() throws IOException {
        int numIndexed = 0;
        if (indexWriter != null) {
            numIndexed = indexWriter.numDocs();
        }
        return numIndexed;
    }

}