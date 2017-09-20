package tarun.stockApp.TechnicalIndicator.Calculations;

public class OnBalanceVolumeIndicator {
	public String stockName;
	public String tradeddate;
	public Long volume;
	public double stockPrice;
	public Long volumeChangeInLastDay;
	public float percentageChangeInLastDay;
	public boolean continuousIncreasedVolume;
	public float percentageChangeInLastFewDay;
	@Override
	public String toString() {
		return "[volume=" + volume + ", percentageChangeInLastDay=" + percentageChangeInLastDay + ", continuousIncreasedVolume="
				+ continuousIncreasedVolume + ", percentageChangeInLastFewDay=" + percentageChangeInLastFewDay + "]";
	}
	

}
