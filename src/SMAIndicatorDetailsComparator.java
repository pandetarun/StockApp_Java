import java.util.Comparator;

public class SMAIndicatorDetailsComparator implements Comparator<SMAIndicatorDetails> {

	@Override
	public int compare(SMAIndicatorDetails obj1, SMAIndicatorDetails obj2) {
		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("buy") && (obj2.signalSMAToSMA==null || !obj2.signalSMAToSMA.equalsIgnoreCase("buy"))){
			return -1;
		}

		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA=="buy" && (obj2.signalSMAToSMA!=null || obj2.signalSMAToSMA.equalsIgnoreCase("put"))) {
			return -1;
		}

		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("buy") && (obj2.signalSMAToSMA!=null || obj2.signalSMAToSMA.equalsIgnoreCase("buy"))) {
			if(obj1.signalPriceToSMA!=null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && (obj2.signalPriceToSMA==null || obj2.signalPriceToSMA.equalsIgnoreCase("put"))) {
				return -1;
			}
		}
		
		if(obj1.signalSMAToSMA!=null && obj1.signalSMAToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("buy")) {
			if(obj1.signalPriceToSMA!=null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalPriceToSMA!=null && obj2.signalPriceToSMA.equalsIgnoreCase("buy")) {
				if(obj1.percentagePriceChange>obj2.percentagePriceChange) {
					return -1;
				}
			}
		}
		
		if(obj1.signalSMAToSMA==null && obj2.signalSMAToSMA!=null && obj2.signalSMAToSMA.equalsIgnoreCase("put")) {
			return -1;
		}
		
		if(obj1.signalSMAToSMA==null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA==null && (obj2.signalPriceToSMA == null || !obj2.signalPriceToSMA.equalsIgnoreCase("buy"))) {
			return -1;
		}
		
		if(obj1.signalSMAToSMA==null && obj1.signalPriceToSMA.equalsIgnoreCase("buy") && obj2.signalSMAToSMA==null && obj2.signalPriceToSMA!=null && obj2.signalPriceToSMA.equalsIgnoreCase("buy")) {
			if(obj1.percentagePriceChange>obj2.percentagePriceChange) {
				return -1;
			}
		}
		
		return 1;
	}

}
