package com.leo.demo;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 针对数据库表数据-索引生成 工具类
 *
 * Created by LX on 2017/8/25.
 */
public class DBDataIndexer {

    public static String INDEX_DIR = "D:\\Library\\lucence\\index1";// 索引存放目录

    private Directory directory = null;

    /***
     * 初始化索引文件目录
     *
     * @return
     * @throws Exception
     */
    public Directory initLuceneDirctory() throws Exception {
        if (directory == null) {
            File indexDir = new File(INDEX_DIR);
            // 文件目录
            // 把索引文件存储到磁盘目录
            // 索引文件可放的位置：索引可以存放在两个地方1.硬盘，2.内存；
            // 放在硬盘上可以用FSDirectory()，放在内存的用RAMDirectory()不过一关机就没了
            directory = FSDirectory.open(indexDir);
        }
        return directory;
    };

    /***
     * 初始化 Lucene 创建、增量索引的对象
     *
     * @param cOra
     *            true:表示创建索引，false表示在原有的索引基础上增量
     * @return
     * @throws IOException
     */
    public static IndexWriter initLuceneObj(Directory directory, boolean cOra) {
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT); // 创建一个语法分析器
        IndexWriter writer = null;
        try {

            // 创建一个IndexWriter(存放索引文件的目录,分析器,Field的最大长度)
            System.out.println(IndexWriter.MaxFieldLength.UNLIMITED);
            // 可见构造它需要一个索引文件目录，一个分析器(一般用标准的这个)，一个参数是标识是否清空索引目录
            writer = new IndexWriter(directory, analyzer, cOra, IndexWriter.MaxFieldLength.UNLIMITED);
            // 索引合并因子
            // 一、SetMergeFactor（合并因子）
            // SetMergeFactor是控制segment合并频率的，其决定了一个索引块中包括多少个文档，当硬盘上的索引块达到多少时，
            // 将它们合并成一个较大的索引块。当MergeFactor值较大时，生成索引的速度较快。MergeFactor的默认值是10，建议在建立索引前将其设置的大一些。
            writer.setMergeFactor(100);
            // 二、SetMaxBufferedDocs（最大缓存文档数）
            // SetMaxBufferedDocs是控制写入一个新的segment前内存中保存的document的数目，
            // 设置较大的数目可以加快建索引速度，默认为10。
            writer.setMaxMergeDocs(1000);
            // 三、SetMaxMergeDocs（最大合并文档数）
            // SetMaxMergeDocs是控制一个segment中可以保存的最大document数目，值较小有利于追加索引的速度，默认Integer.MAX_VALUE，无需修改。
            // 在创建大量数据的索引时，我们会发现索引过程的瓶颈在于大量的磁盘操作，如果内存足够大的话，
            // 我们应当尽量使用内存，而非硬盘。可以通过SetMaxBufferedDocs来调整，增大Lucene使用内存的次数。
            return writer;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对单个Entity对象（自定义对应数据库数据对象）进行索引
     *
     * @param writer
     * @param e  自定义javaBean对象，
     * @throws IOException
     */
    public static void indexEntity(IndexWriter writer, IndexObject e)
            throws IOException {
        if (e == null) {
            return;
        }
        Document doc = new Document();
        doc.add(new Field("id", e.getId(), Field.Store.YES, Field.Index.NO));
        doc.add(new Field("name", e.getName(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("content", e.getContent(), Field.Store.YES, Field.Index.NO));
        writer.addDocument(doc);
    }

    /***
     *
     * 获得本次创建/增量索引的原数据数量（条）
     *
     * @param writer
     * @return
     */
    public static int getNumDocs(IndexWriter writer) throws IOException {
        int numIndexed = 0;
        if (writer != null) {
            numIndexed = writer.numDocs();
        }
        return numIndexed;
    }

    /***
     * 关闭Lucene相关Io对象
     */
    public static void closeIndexWriter(IndexWriter writer, Directory directory) {
        if (writer != null) {
            try {
                writer.close(); // 关闭IndexWriter时,才把内存中的数据写到文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}