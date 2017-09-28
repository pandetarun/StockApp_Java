package tarun.stockApp.TechnicalIndicator.Data;

import java.util.Comparator;

public class StochasticIndicatorComparator implements Comparator<StochasticeIndicatorCalculationData> {

	@Override
	public int compare(StochasticeIndicatorCalculationData obj1, StochasticeIndicatorCalculationData obj2) {
		if(obj1.closePrice > obj2.closePrice) {
			return 1;
		} else if(obj1.closePrice < obj2.closePrice) {
			return -1;
		}
		return 0;
	}	
}
