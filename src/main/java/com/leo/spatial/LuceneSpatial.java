package com.leo.spatial;

import com.google.common.collect.Lists;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.search.*;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Rectangle;
import org.locationtech.spatial4j.shape.Shape;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by LX on 2017/8/26.
 */
public class LuceneSpatial {

    /**
     * Spatial4j上下文
     * 1: SpatialContext初始化可由SpatialContextFactory配置
     * 2: SpatialContext属性
     * DistanceCalculator(默认使用GeodesicSphereDistCalc.Haversine,将地球视为标准球体)
     * ShapeFactory(默认使用ShapeFactoryImpl)
     * Rectangle(构建经纬度空间:RectangleImpl(-180, 180, -90, 90, this))
     * BinaryCodec()
     */
    private SpatialContext ctx;

    /**
     * 索引和查询模型的策略接口
     */
    private SpatialStrategy strategy;

    /**
     * 索引存储目录
     */
    private Directory directory;


    int maxLevels = 11;

    protected void init() {
        /**
         * SpatialContext也可以通过SpatialContextFactory工厂类来构建
         * */
        this.ctx = SpatialContext.GEO;

        /**
         * 网格最大11层或Geo Hash的精度
         * 1: SpatialPrefixTree定义的Geo Hash最大精度为24
         * 2: GeohashUtils定义类经纬度到Geo Hash值公用方法
         * */
        SpatialPrefixTree spatialPrefixTree = new GeohashPrefixTree(ctx, maxLevels);

        /**
         * 索引和搜索的策略接口,两个主要实现类
         * 1: RecursivePrefixTreeStrategy(支持任何Shape的索引和检索)
         * 2: TermQueryPrefixTreeStrategy(仅支持Point Shape)
         * 上述两个类继承PrefixTreeStrategy(有使用缓存)
         * */
        this.strategy = new RecursivePrefixTreeStrategy(spatialPrefixTree, "location");
        // 初始化索引目录
        this.directory = new RAMDirectory();
    }

    protected void createIndex(List<CityGeoInfo> cityGeoInfos) throws Exception {
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.addDocuments(newSampleDocument(ctx, strategy, cityGeoInfos));
        indexWriter.close();
    }

    /**
     * 创建Document索引对象
     */
    protected List<Document> newSampleDocument(SpatialContext ctx, SpatialStrategy strategy, List<CityGeoInfo> cityGeoInfos) {
        List<Document> documents = Lists.newLinkedList(cityGeoInfos.stream()
                .map(cgi -> {
                    Document doc = new Document();
                    doc.add(new StoredField("id", cgi.getCityId()));
                    doc.add(new NumericDocValuesField("id", cgi.getCityId()));
                    doc.add(new StringField("city", cgi.getName(), Field.Store.YES));
                    Shape shape = null;
                    /**
                     * 对小于MaxLevel的Geo Hash构建Field(IndexType[indexed,tokenized,omitNorms])
                     * */
                    Field[] fields = strategy.createIndexableFields((shape = ctx.getShapeFactory().pointXY(cgi.getLnt(), cgi.getLat())));
                    for (Field field : fields) {
                        doc.add(field);
                    }
                    Point pt = (Point) shape;
                    doc.add(new StoredField(strategy.getFieldName(), pt.getX() + "," + pt.getY()));
                    return doc;
                })
                .collect(Collectors.toList()));
        return documents;
    }

    /**
     * 地理位置搜索
     *
     * @throws Exception
     */
    public void search() throws Exception {
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        /**
         * 按照id升序排序
         * */
        Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

        /**
         * 搜索方圆100千米范围以内,以当前位置经纬度(120.33,36.07)青岛为圆心,其中半径为100KM
         * */
        SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, ctx.getShapeFactory().circle(120.33, 36.07, DistanceUtils.dist2Degrees(100, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
        Query query = strategy.makeQuery(args);
        TopDocs topDocs = indexSearcher.search(query, 10, idSort);
        /**
         * 输出命中结果
         * */
        printDocument(topDocs, indexSearcher, args.getShape().getCenter());

        System.out.println("==========================华丽的分割线=========================");

        /**
         * 定义坐标点(x,y)即(经度,纬度)即当前用户所在地点(烟台)
         * */
        Point pt = ctx.getShapeFactory().pointXY(121.39, 37.52);

        /**
         * 计算当前用户所在坐标点与索引坐标点中心之间的距离即当前用户地点与每个待匹配地点之间的距离,DEG_TO_KM表示以KM为单位
         * 对Field(name=location)字段检索
         * */
        ValueSource valueSource = strategy.makeDistanceValueSource(pt, DistanceUtils.DEG_TO_KM);

        /**
         * 根据命中点与当前位置坐标点的距离远近降序排,距离数字大的排在前面,false表示降序,true表示升序
         * */
        Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher);
        TopDocs topdocs = indexSearcher.search(new MatchAllDocsQuery(), 10, distSort);
        printDocument(topdocs, indexSearcher, pt);
        indexReader.close();
    }

    protected void printDocument(TopDocs topDocs, IndexSearcher indexSearcher, Point point) throws Exception {
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            int docId = scoreDoc.doc;
            Document document = indexSearcher.doc(docId);
            int cityId = document.getField("id").numericValue().intValue();
            String city = document.getField("city").stringValue();
            String location = document.getField(strategy.getFieldName()).stringValue();
            String[] locations = location.split(",");
            double xPoint = Double.parseDouble(locations[0]);
            double yPoint = Double.parseDouble(locations[1]);
            double distDEG = ctx.calcDistance(point, xPoint, yPoint);
            double juli = DistanceUtils.degrees2Dist(distDEG, DistanceUtils.EARTH_MEAN_RADIUS_KM);
            System.out.println("docId=" + docId + "\tcityId=" + cityId + "\tcity=" + city + "\tdistance=" + juli + "KM");
        }
    }


    /**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     * @param filePath
     */
    public static List<CityGeoInfo> readTxtFile(String filePath,String encoding){
        List<CityGeoInfo> list = new ArrayList<CityGeoInfo>();
        try {
            encoding = encoding == null || "".equals(encoding) ? "UTF-8":encoding;
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                int i = 0;
                while((lineTxt = bufferedReader.readLine()) != null){
                    i++;
                    String[] arr = lineTxt.replaceAll(" ","").split(":");
                    String[] xy = arr[1].split(",");
                    CityGeoInfo cityGeoInfo = new CityGeoInfo();
                    cityGeoInfo.setCityId(i);
                    cityGeoInfo.setName(arr[0]);
                    cityGeoInfo.setLnt(Double.parseDouble(xy[0]));
                    cityGeoInfo.setLat(Double.parseDouble(xy[1]));
                    list.add(cityGeoInfo);
                }
                read.close();

            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list;
    }


    public static double calcDistanceFromErrPct(Shape shape, double distErrPct, SpatialContext ctx) {
        if (distErrPct < 0 || distErrPct > 0.5) {
            throw new IllegalArgumentException("distErrPct " + distErrPct + " must be between [0 to 0.5]");
        }
        if (distErrPct == 0 || shape instanceof Point) {
            return 0;
        }
        Rectangle bbox = shape.getBoundingBox();
        //Compute the distance from the center to a corner.  Because the distance
        // to a bottom corner vs a top corner can vary in a geospatial scenario,
        // take the closest one (greater precision).
        Point ctr = bbox.getCenter();
        double y = (ctr.getY() >= 0 ? bbox.getMaxY() : bbox.getMinY());
        double diagonalDist = ctx.getDistCalc().distance(ctr, bbox.getMaxX(), y);
        return diagonalDist * distErrPct;
    }

    public int getLevelForDistance(double dist) {
        if (dist == 0)
            return maxLevels;//short circuit
        final int level = GeohashUtils.lookupHashLenForWidthHeight(dist, dist);
        return Math.max(Math.min(level, maxLevels), 1);
    }


    public static void main(String[] args) throws Exception {
        String textPath = System.getProperty("user.dir") + File.separator+"src"+File.separator +"main"+File.separator + "resources"+File.separator + "geo.txt";
        LuceneSpatial luceneSpatial = new LuceneSpatial();
        luceneSpatial.init();
        luceneSpatial.createIndex(readTxtFile(textPath,null));
        luceneSpatial.search();
    }

}