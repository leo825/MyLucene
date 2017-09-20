package com.leo;

import com.leo.spatial.CityGeoInfo;
import org.junit.Test;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LX on 2017/9/3.
 */
public class test {

    String insertPath = System.getProperty("user.dir") + File.separator+"src"+File.separator +"main"+File.separator + "resources"+File.separator + "insert.sql";
    String txtPath = System.getProperty("user.dir") + File.separator+"src"+File.separator +"main"+File.separator + "resources"+File.separator + "insert.txt";
    @Test
    public void getValues() throws IOException {
        exchange(insertPath,txtPath);
    }

    /**
     * 将sql文件转成txt
     * @param sqlPath sql文件全路径
     * @param txtPath txt文件全路径
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void exchange(String sqlPath,String txtPath) throws FileNotFoundException, UnsupportedEncodingException {
        StringBuffer sb= new StringBuffer("");
        File file=new File(insertPath);
        if(file.isFile() && file.exists()){ //判断文件是否存在
            InputStreamReader read = new InputStreamReader(new FileInputStream(sqlPath),"UTF-8");//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            try {
                while((lineTxt = bufferedReader.readLine()) != null){
                    String newStr  = lineTxt.replaceAll("to_date\\(","").replaceAll(",'yyyymmddHH24miss'\\)","");
                    Pattern pattern = Pattern.compile("(?<=values\\().*(?=\\);)");
                    Matcher matcher = pattern.matcher(newStr);
                    while(matcher.find()){
                        String[] arr = matcher.group(0).split(",");
                        System.out.println(matcher.toString());
                        String s = "";
                        for(int i = 0; i < arr.length; i++){
                            String temp = arr[i].replaceAll("'","");
                            if(temp == null || "".equals(temp) || "null".equals(temp) ||"''".equals(temp)){
                                s += "\"\""+",";
                            }else{
                                s += "\"" + temp +"\""+",";
                            }
                        }
                        //此处是为了换行
                        sb.append(s.substring(0,s.length()-1)+System.getProperty("line.separator"));
                    }
                }
                bufferedReader.close();
                read.close();

                //将字符串写入到txt文件中
                FileWriter writer = new FileWriter(txtPath);
                BufferedWriter bw = new BufferedWriter(writer);
                bw.write(sb.toString());
                bw.close();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
