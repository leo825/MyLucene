package com.leo.solr;

import com.leo.demo.IndexObject;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by LX on 2017/9/5.
 */
public class SolrDao {

    private final String DB_DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";
    private final String DB_URL = "jdbc:oracle:thin:@192.168.1.160:1521:ORCL";
    private final String DB_USER_NAME = "BFPICK";
    private final String DB_PASSWORD = "BFPICK";

    private final static int pageCount = 100000;

    /**
     * 获取一个数据库连接
     *
     * @return
     */
    public Connection getConnection() {
        Connection conn = null;// 创建一个数据库连接
        try {
            Class.forName(DB_DRIVER_NAME);// 加载Oracle驱动程序
            System.out.println("开始尝试连接数据库！");
            conn = DriverManager.getConnection(DB_URL, DB_USER_NAME, DB_PASSWORD);// 获取连接
            System.out.println("连接成功！");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 初始化数据库中的数据到索引文件中。例如将姓名作为关键字，将id作为内容
     *
     * @return
     */
    public List<Article> initSolrIndex() {
        Connection conn = getConnection();
        ResultSet rs = null;
        PreparedStatement psp = null;
        List<Article> list = new ArrayList<Article>();
        try{
            String sql = "select id,xm,ssjgid from CJ_SS_USER";
            psp = conn.prepareStatement(sql);
            rs = psp.executeQuery();
            while (rs.next()){
                String id = rs.getString("id");
                String name = rs.getString("xm");
                String content = rs.getString("ssjgid");
                Article object = new Article();

                if(name== null || "".equals(name)){
                    System.out.println(id + "这个name为空了");
                    name = "无名";
                }
                if(content== null || "".equals(content)){
                    System.out.println(id + "这个content为空了");
                    content = "无内容";
                }

                object.setId(id);
                object.setName(name);
                object.setContent(content);
                object.setCreateTime(new Date());
                list.add(object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            close(rs,psp,conn);
        }
        System.out.println(list.size());
        return  list;
    }


    /**
     * 初始化数据库中的数据到索引文件中。例如将姓名作为关键字，将id作为内容
     * 此处做了分页处理，以防止大数据量造成内存溢出
     *
     * @return
     */
    public List<Article> initSolrIndex(int start ,int end) {
        Connection conn = getConnection();
        ResultSet rs = null;
        PreparedStatement psp = null;
        List<Article> list = new ArrayList<Article>();
        try{
            String sql = "select u.* from (select id,xm,ssjgid,rownum rn from CJ_SS_USER) u where rn > " + start + " and rn <= " + end;
            psp = conn.prepareStatement(sql);
            rs = psp.executeQuery();
            while (rs.next()){
                Article object = new Article();
                object.setId(rs.getString("id"));
                object.setName(rs.getString("xm"));
                object.setContent(rs.getString("ssjgid"));
                object.setCreateTime(new Date());
                list.add(object);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            close(rs,psp,conn);
        }
        System.out.println(list.size());
        return  list;
    }

    /**
     * 根据表名来获取数据库中的数据量
     *
     * @param tableName
     * @return
     */
    public int getCountOfTable(String tableName){
        Connection conn = getConnection();
        ResultSet rs = null;
        PreparedStatement psp = null;
        int result = 0;

        if(conn != null){
            try{
                String sql = "select count(*) as count from "+tableName;
                psp = conn.prepareStatement(sql);
                rs = psp.executeQuery();
                while (rs.next()){
                    result = rs.getInt("count");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                close(rs,psp,conn);
            }
            return result;
        }else{
            return 0;
        }
    }

    /**
     * 根据表中数据的数量以及分页的大小来获取最大的分页数
     *
     * @param count
     * @return
     */
    public int getMaxPage(int count){
        return (int) Math.ceil((double)count/pageCount);
    }


    /**
     * 测试用来获取用户的个数
     * @return
     */
    public int getCountOfUser() {
        Connection conn = getConnection();
        ResultSet rs = null;
        PreparedStatement psp = null;
        int result = 0;

        if(conn != null){
            try{
                String sql = "select count(*) as count from CJ_SS_USER";
                psp = conn.prepareStatement(sql);
                rs = psp.executeQuery();
                while (rs.next()){
                    result = rs.getInt("count");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                close(rs,psp,conn);
            }
            return result;
        }else{
            return 0;
        }
    }

    /**
     * 关闭数据库连接
     * @param rs
     * @param conn
     */
    public void close(ResultSet rs,PreparedStatement psp,Connection conn){
        try{
            if(rs != null) {
                rs.close();
            }
            if(psp != null){
                psp.close();
            }
            if(conn != null){
                conn.close();
            }
            System.out.println("数据库连接已经关闭了。");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
