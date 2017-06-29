import java.util.Date;

public class SMAIndicatorDetails implements Comparable<SMAIndicatorDetails> {
	String stockCode;
	float percentagePriceChange;
	String signalPriceToSMA; //"put" for options in case stock dropping down. "buy" in case price going up and crossed middleperiodSMA
	String signalSMAToSMA; //"put" for options in case stock dropping down. "buy" in case lower SMA going up and crossed higherperiodSMA
	Date signalDate;
	
	public int compareTo(SMAIndicatorDetails other)
	  {
	    //Handle buy cases
		if(signalSMAToSMA != null && signalSMAToSMA.equalsIgnoreCase("buy") && (other.signalSMAToSMA == null || !other.signalSMAToSMA.equalsIgnoreCase("buy"))) {
	    	//First object is first
	    	return 1;
	    } else if(!signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA.equalsIgnoreCase("buy")) {
	    	//argument object is first
	    	return -1;
	    } else if((!signalSMAToSMA.equalsIgnoreCase("buy") && !other.signalSMAToSMA.equalsIgnoreCase("buy")) || (signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA.equalsIgnoreCase("buy")) ) {
	    	//Both have buy or not buy in signalSMAToSMA
	    	if(signalPriceToSMA.equalsIgnoreCase("buy")  && !other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		return 1;
	    	} else if(!signalPriceToSMA.equalsIgnoreCase("buy")  && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		return -1;
	    	} else if(signalPriceToSMA.equalsIgnoreCase("buy")  && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		if(percentagePriceChange>= other.percentagePriceChange) {
	    			return 1;
	    		} else if(percentagePriceChange< other.percentagePriceChange) {
	    			return -1;
	    		}
	    	}
	    }
		
		// Handle put cases
		if(signalSMAToSMA.equalsIgnoreCase("put") && !other.signalSMAToSMA.equalsIgnoreCase("put"))	{
			// First object is last
			return -1;
		}else if(!signalSMAToSMA.equalsIgnoreCase("put")&&other.signalSMAToSMA.equalsIgnoreCase("put"))
		{
			// argument object is last
			return 1;
		}else if((!signalSMAToSMA.equalsIgnoreCase("put") && !other.signalSMAToSMA.equalsIgnoreCase("put")) || (signalSMAToSMA.equalsIgnoreCase("put") && other.signalSMAToSMA.equalsIgnoreCase("put"))) {
			// Both have put or not put in signalSMAToSMA
			if (signalPriceToSMA.equalsIgnoreCase("put") && !other.signalPriceToSMA.equalsIgnoreCase("put")) {
				return -1;
			} else if (!signalPriceToSMA.equalsIgnoreCase("put") && other.signalPriceToSMA.equalsIgnoreCase("put")) {
				return 1;
			} else if (signalPriceToSMA.equalsIgnoreCase("put") && other.signalPriceToSMA.equalsIgnoreCase("put")) {
				if (percentagePriceChange >= other.percentagePriceChange) {
					return -1;
				} else if (percentagePriceChange < other.percentagePriceChange) {
					return 1;
				}
			}
		}
	    return 0;
	  }	
	
	
}
