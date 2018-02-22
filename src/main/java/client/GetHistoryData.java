package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetHistoryData
{
    private static String ip = "";
    private static String port = "";
    private static String deviceId = "";
    private static int limit = 1;
    private static String appName = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(GetHistoryData.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args){
        getRepMsg(ip,port,deviceId,limit,appName);
    }

    public static Map<String,String> getRepMsg(String ip, String port, String deviceId, Integer limit, String appName){

        Map<String,String> contents= new LinkedHashMap<>();

        StringBuffer url =new StringBuffer();
        StringBuffer param = new StringBuffer();

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")).toString();

        url.append("http://").append(ip).append(":").append(port).append("/in-cse/").append(deviceId).append("/AlarmReport");
        param.append("crb=").append(currentTime).append("&ty=3&lim=").append(limit).append("&rcn=1");

        String resp = sendGetByClient(url.toString(),param.toString(),appName);
        if (resp != "")
        {
            try
            {
                LOGGER.debug(resp);
                JsonNode node = objectMapper.readTree(resp);
//                int size = node.get("m2m:agr").get("m2m:rsp").get("pc").get("m2m:cin").size();
                Iterator<JsonNode> iterator = node.get("m2m:agr").get("m2m:rsp").get("pc").get("m2m:cin").iterator();
                while (iterator.hasNext())
                {
                    JsonNode jsonNode = iterator.next();
                    String time = jsonNode.get("lt").asText();
                    String content = jsonNode.get("con").asText();
                    content = hexToString(content);

                    contents.put(time, content);
                }

                LOGGER.debug("size : " + contents.size());

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return contents;
    }

    public static String sendGetByClient(String url, String param,String appName){
        String result = "";
        String urlNameString = url + "?" + param;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try
        {
            HttpGet request = new HttpGet(urlNameString);
            request.setHeader("Accept", "application/json");
            request.setHeader("X-M2M-RI", "20000000");
            request.setHeader("Content-Type","application/x-www-form-urlencoded");
            request.setHeader("Content-Length","0");
            request.setHeader("X-M2M-Origin","/in-cse/"+appName);


            CloseableHttpResponse response = httpClient.execute(request);
            LOGGER.debug(String.valueOf(response.getStatusLine()));
            try
            {
                HttpEntity entity = response.getEntity();
                if (entity != null){
                    result = EntityUtils.toString(entity);
                }
            } finally
            {
                response.close();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }finally
        {
            try
            {
                httpClient.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    // 转化十六进制编码为字符串
    private static String hexToString(String s)
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
                LOGGER.warn("HexString Error");
            }
        }
        try
        {
            s = new String(baKeyword, "utf-8");
        }
        catch (Exception e1)
        {
            LOGGER.warn("Hex to String Error");
        }
        return s;
    }

}
