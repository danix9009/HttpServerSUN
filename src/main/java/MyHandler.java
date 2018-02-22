import bean.WeatherData;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyHandler implements HttpHandler
{
    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        String requestMethod = httpExchange.getRequestMethod();
        logger.debug("处理新请求:{}",requestMethod);
//        System.out.println("处理新请求:"+requestMethod);
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            httpExchange.sendResponseHeaders(200, 0);

            OutputStream responseBody = httpExchange.getResponseBody();
            Headers requestHeaders = httpExchange.getRequestHeaders();
            printReqHeaders(requestHeaders);
            responseBody.close();
        } else if (requestMethod.equalsIgnoreCase("POST"))
        {

            Headers h=httpExchange.getResponseHeaders();
            h.set("Content-Type", "text/plain");
            httpExchange.sendResponseHeaders(200,0);

            OutputStream responseBody=httpExchange.getResponseBody();
            //获取post请求Header信息
            Headers requestHeader=httpExchange.getRequestHeaders();
            //获取post请求Body信息
            InputStream requestBody=httpExchange.getRequestBody();

            printReqHeaders(requestHeader);

            //将请求的Body信息输出
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100]; //buff用于存放循环读取的临时数据
            int rc = 0;
            while ((rc = requestBody.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] bytes = swapStream.toByteArray();

//            System.out.println("\nPost Body:\n"+swapStream.toString()+"\n");

            parseXmlStr(swapStream.toString());
            //返回响应信息（Body）
//            responseBody.write(bytes);
            swapStream.close();
            responseBody.close();
        }
    }

    /**
     * 打印请求的Header信息
     * @param requestHeaders
     */
    private void printReqHeaders(Headers requestHeaders)
    {
        Set<String> keySet = requestHeaders.keySet();
        Iterator<String> iter = keySet.iterator();
        Map<String,String> m = new HashMap<>();
        String headerJson="";

        while (iter.hasNext()) {
            String key = iter.next();
            List values = requestHeaders.get(key);
            m.put(key,values.toString());
//            String s = key + " = " + values.toString();
//            logger.info(s);
//            System.out.println(s);
//                responseBody.write(s.getBytes());
        }

        ObjectMapper objectMapper=new ObjectMapper();
        try
        {
            headerJson=objectMapper.writeValueAsString(m);
            logger.debug("Header Info:{}",headerJson);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * xml转化为json格式
     * @param xmlStr
     */
    private void parseXmlStr(String xmlStr){
        StringWriter w = new StringWriter();
        try
        {
            JsonParser jp = xmlMapper.getFactory().createParser(xmlStr);
            JsonGenerator jg = objectMapper.getFactory().createGenerator(w);
            while (jp.nextToken() != null) {
                jg.copyCurrentEvent(jp);
            }
            jp.close();
            jg.close();

//            logger.info("Body Info:{}",w.toString());
            getContent(w.toString());
        } catch (IOException e)
        {
            logger.warn("Body`s Xml is Error");
        }

    }

    /**
     * json中获取content值
     * @param jsonStr
     */
    private void getContent(String jsonStr){
        String content="";
        try
        {
            JsonNode node=objectMapper.readTree(jsonStr);
            content=node.get("nev").get("rep").get("cin").get("con").toString();
        } catch (IOException e)
        {
            logger.warn("Get Content Error");
        }
//        logger.info("{} : {}",new Date(),hexToString(content));
//        logger.info(formatMsg(hexToString(content)));
        assembleJson(hexToString(content));
    }


    private String formatMsg(String content){
        String[] data=content.split("\\|");
        List<String> list = new ArrayList<>();
        for(String m:data){
            String[] s = m.split(":");
            String key="";
            switch (s[0]){
                case "AG":
                    key = "设备角度";
                    break;
                case "TP":
                    key = "温度";
                    break;
                case "HM":
                    key = "湿度";
                    break;
                case "PV":
                    key = "PM2.5";
                    break;
                case "PX":
                    key = "PM10";
                    break;
                case "NS":
                    key = "噪声值";
                    break;
            }
            list.add(key+" : "+s[1]);
        }

        String result="当前站点检测值：";
        for (String s :list){
            result += "\n" + s;
        }
        return result;
    }

    private void assembleJson(String content){
        WeatherData w=new WeatherData();
        Map<String,String> mdata= new HashMap<String, String>();
        String[] data=content.split("\\|");
        for(String m:data){
            String[] s = m.split(":");
            switch (s[0]){
                case "AG":

                    break;
                case "TP":
                    mdata.put("temperature", s[1]);
                    break;
                case "HM":
                    mdata.put("humidity", s[1]);
                    break;
                case "PV":
                    mdata.put("pm25", s[1]);
                    break;
                case "PX":
                    mdata.put("pm10", s[1]);
                    break;
                case "NS":
                    mdata.put("noise", s[1]);
                    break;
            }
        }

        mdata.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        mdata.put("radiation", "");
        mdata.put("rainfallDaily", "");
        mdata.put("windDirection", "");
        mdata.put("windSpeed", "");

        w.setRealTime(mdata);


        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        try {
            logger.info(objectMapper.writeValueAsString(w));
        } catch (JsonProcessingException e) {
            logger.error("Assemble Json Error");
        }

    }


    // 转化十六进制编码为字符串
    private String hexToString(String s)
    {
        if (s== null || s.equals("")){
            return null;
        }
        s=s.replace("\"","");
        s= s.replace(" ","");

        byte[] baKeyword = new byte[s.length()/2];
        for(int i = 0; i < baKeyword.length; i++)
        {
            try
            {
                baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
            }
            catch(Exception e)
            {
                logger.warn("HexString Error");
            }
        }
        try
        {
            s = new String(baKeyword, "utf-8");
        }
        catch (Exception e1)
        {
            logger.warn("Hex to String Error");
        }
        return s;
    }



    private static ObjectMapper objectMapper= new ObjectMapper();
    private static XmlMapper xmlMapper= new XmlMapper();
    private final static Logger logger = LoggerFactory.getLogger(MyHandler.class);
}
