package bean;

import java.util.List;

public class Temperature {
  private List<String> time;
  private List<String> values;
  
  
  public List<String> getTime() {
    return time;
  }
  public void setTime(List<String> time) {
    this.time = time;
  }
  public List<String> getValues() {
    return values;
  }
  public void setValues(List<String> values) {
    this.values = values;
  }
}
