package com.leo.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
/**
 * Created by LX on 2017/8/24.
 *
 * 在lucene3.6.1中正常运行
 */
public class MyIkAnalyzerTest {

    public static void testIk(){
        //Lucene Document的域名
        String fieldName = "text";
        //检索内容
        String text = "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";

        //实例化IKAnalyzer分词器
        Analyzer analyzer = new IKAnalyzer(true);


        Directory directory = null;
        IndexWriter iwriter = null;
        IndexReader ireader = null;
        IndexSearcher isearcher = null;
        try {
            //建立内存索引对象
            directory = new RAMDirectory();

            //配置IndexWriterConfig
            IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_34 , analyzer);
            iwConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
            iwriter = new IndexWriter(directory , iwConfig);
            //写入索引
            Document doc = new Document();
            doc.add(new Field("ID", "10000", Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field(fieldName, text, Field.Store.YES, Field.Index.ANALYZED));
            iwriter.addDocument(doc);
            iwriter.close();


            //搜索过程**********************************
            //实例化搜索器
            ireader = IndexReader.open(directory);
            isearcher = new IndexSearcher(ireader);

            String keyword = "中文分词工具包";
            //使用QueryParser查询分析器构造Query对象
            QueryParser qp = new QueryParser(Version.LUCENE_34, fieldName, analyzer);
            qp.setDefaultOperator(QueryParser.AND_OPERATOR);
            Query query = qp.parse(keyword);

            //搜索相似度最高的5条记录
            TopDocs topDocs = isearcher.search(query , 5);
            System.out.println("命中：" + topDocs.totalHits);
            //输出结果
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (int i = 0; i < topDocs.totalHits; i++){
                Document targetDoc = isearcher.doc(scoreDocs[i].doc);
                System.out.println("内容：" + targetDoc.toString());
            }

        } catch (CorruptIndexException e) {
            e.printStackTrace();
        } catch (LockObtainFailedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (ireader != null) {
                try {
                    ireader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (directory != null) {
                try {
                    directory.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public  static void testIk2() throws IOException {
        String keyWord = "IKAnalyzer的分词效果到底怎么样呢，我们来看一下吧";
        //创建IKAnalyzer中文分词对象
        IKAnalyzer analyzer = new IKAnalyzer(true);
        // 使用智能分词
        //analyzer.setUseSmart(true);
        // 打印分词结果
        try {
            printAnalysisResult(analyzer, keyWord);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印出给定分词器的分词结果
     *
     * @param analyzer
     *            分词器
     * @param keyWord
     *            关键词
     * @throws Exception
     */
    public static void printAnalysisResult(Analyzer analyzer, String keyWord)
            throws Exception {
        System.out.println("["+keyWord+"]分词效果如下");
        TokenStream tokenStream = analyzer.tokenStream("content",
                new StringReader(keyWord));
        tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            CharTermAttribute charTermAttribute = tokenStream
                    .getAttribute(CharTermAttribute.class);
            System.out.println(charTermAttribute.toString());

        }
    }

    public static void main(String[] agrs) throws IOException {
        testIk();
        //testIk2();
    }
}
