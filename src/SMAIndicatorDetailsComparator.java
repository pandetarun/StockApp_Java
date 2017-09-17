import java.util.Comparator;

public class SMAIndicatorDetailsComparator implements Comparator<SMAIndicatorDetails> {

	@Override
	public int compare(SMAIndicatorDetails obj1, SMAIndicatorDetails obj2) {
		//Comaprison for first object SMAtoSMA not null and Buy and second object SMAtoSMA null or not buy
		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("buy") && (obj2.signalSMAToSMA==null || !obj2.signalSMAToSMA.equalsIgnoreCase("buy"))){
			return -1;
		} else if(obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("buy") && (obj1.signalSMAToSMA==null || !obj1.signalSMAToSMA.equalsIgnoreCase("buy"))) {
			return 1;
		}
		//Comaprison for first object SMAtoSMA not null and  Buy and second object SMAtoSMA not null and put
		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA=="buy" && (obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("put"))) {
			return -1;
		} else if (obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA=="buy" && (obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("put"))) {
			return 1;
		}
		//Comaprison for first object SMAtoSMA not null and  Buy and second object SMAtoSMA not null and buy
		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("buy")) {
			if(obj1.SMNSMcrossover && !obj2.SMNSMcrossover) {
				return -1;
			} else if (obj2.SMNSMcrossover && !obj1.SMNSMcrossover) {
				return 1;
			}
			if(obj1.SMNSMcontinuousGrowth && !obj2.SMNSMcontinuousGrowth) {
				return -1;
			} else if (obj2.SMNSMcontinuousGrowth && !obj1.SMNSMcontinuousGrowth) {
				return 1;
			}
			if(obj1.SMAToSMApercentageDeviation > obj2.SMAToSMApercentageDeviation) {
				return -1;
			} else if (obj2.SMAToSMApercentageDeviation > obj1.SMAToSMApercentageDeviation) {
				return 1;
			}
			if(obj1.signalPriceToSMA!=null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && (obj2.signalPriceToSMA==null || obj2.signalPriceToSMA.equalsIgnoreCase("put"))) {
				return -1;
			} else if (obj2.signalPriceToSMA!=null && obj2.signalPriceToSMA.equalsIgnoreCase("buy") && (obj1.signalPriceToSMA==null || obj1.signalPriceToSMA.equalsIgnoreCase("put"))) {
				return 1;
			}
			if(obj1.signalPriceToSMA!=null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalPriceToSMA!=null && obj2.signalPriceToSMA.equalsIgnoreCase("buy")) {
				if(obj1.PNSMAcrossover && !obj2.PNSMAcrossover) {
					return -1;
				} else if(obj2.PNSMAcrossover && !obj1.PNSMAcrossover) {
					return 1;
				}
				if(obj1.PNSMcontinuousGrowth && !obj2.PNSMcontinuousGrowth) {
					return -1;
				} else if(obj2.PNSMcontinuousGrowth && !obj1.PNSMcontinuousGrowth) {
					return 1;
				}
				if(obj1.priceToSMApercentageDeviation > obj2.priceToSMApercentageDeviation) {
					return -1;
				} else if(obj1.priceToSMApercentageDeviation > obj1.priceToSMApercentageDeviation) {
					return 1;
				}
				if(obj1.percentagePriceChange>obj2.percentagePriceChange) {
					return -1;
				} else if(obj2.percentagePriceChange>obj1.percentagePriceChange) {
					return 1;
				}
			}
		}
		
		if(obj1.signalSMAToSMA==null && obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("put")) {
			return -1;
		} else if(obj2.signalSMAToSMA==null && obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("put")) {
			return 1;
		}
		
		if(obj1.signalSMAToSMA==null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA==null && (obj2.signalPriceToSMA == null || !obj2.signalPriceToSMA.equalsIgnoreCase("buy"))) {
			return -1;
		} else if(obj2.signalSMAToSMA==null && obj2.signalPriceToSMA.equalsIgnoreCase("buy") && obj1.signalSMAToSMA==null && (obj1.signalPriceToSMA == null || !obj1.signalPriceToSMA.equalsIgnoreCase("buy"))) {
			return 1;
		}
		
		if(obj1.signalSMAToSMA==null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA==null && obj2.signalPriceToSMA!=null && obj2.signalPriceToSMA.equalsIgnoreCase("buy")) {
			if(obj1.priceToSMApercentageDeviation > obj2.priceToSMApercentageDeviation) {
				return -1;
			} else if(obj2.priceToSMApercentageDeviation > obj1.priceToSMApercentageDeviation) {
				return 1;
			}
			if(obj1.percentagePriceChange>obj2.percentagePriceChange) {
				return -1;
			} else if(obj2.percentagePriceChange>obj1.percentagePriceChange) {
				return 1;
			}
		}
		
		return 0;
	}
	
}
