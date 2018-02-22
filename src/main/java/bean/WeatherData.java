package bean;

import java.util.Map;

public class WeatherData {
  
  private Map<String,String> realTime;
  private History history;
  
  
  public Map<String, String> getRealTime() {
    return realTime;
  }
  public void setRealTime(Map<String, String> realTime) {
    this.realTime = realTime;
  }
  
  public History getHistory() {
    return history;
  }
  public void setHistory(History history) {
    this.history = history;
  }
  
  
}
