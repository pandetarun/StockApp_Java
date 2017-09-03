
public class OnBalanceVolumeIndicator {
	public String stockName;
	public String tradeddate;
	public Long volume;
	public double stockPrice;
	public Long volumeChangeInLastDay;
	float percentageChangeInLastDay;
	public boolean continuousIncreasedVolume;
	float percentageChangeInLastFewDay;
	@Override
	public String toString() {
		return "[volume=" + volume + ", percentageChangeInLastDay=" + percentageChangeInLastDay + ", continuousIncreasedVolume="
				+ continuousIncreasedVolume + ", percentageChangeInLastFewDay=" + percentageChangeInLastFewDay + "]";
	}
	

}
