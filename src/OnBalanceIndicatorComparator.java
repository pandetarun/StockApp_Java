import java.util.Comparator;

public class OnBalanceIndicatorComparator implements Comparator<OnBalanceVolumeIndicator> {

	@Override
	public int compare(OnBalanceVolumeIndicator obj1, OnBalanceVolumeIndicator obj2) {
		
		if(obj1.percentageChangeInLastDay > obj2.percentageChangeInLastDay) {
			return -1;
		} else if(obj1.percentageChangeInLastDay < obj2.percentageChangeInLastDay) {
			return 1;
		}
		if(obj1.percentageChangeInLastFewDay > obj2.percentageChangeInLastFewDay) {
			return -1;
		} else if(obj1.percentageChangeInLastFewDay < obj2.percentageChangeInLastFewDay) {
			return 1;
		}				
		return 0;
	}
}
