import java.time.LocalDate;

public class SMAIndicatorDetails {// implements Comparable<SMAIndicatorDetails> {
	String stockCode;
	float percentagePriceChange;
	String signalPriceToSMA; //"put" for options in case stock dropping down. "buy" in case price going up and crossed middleperiodSMA
	String signalSMAToSMA; //"put" for options in case stock dropping down. "buy" in case lower SMA going up and crossed higherperiodSMA
	LocalDate signalDate;
	
	/*public int compareTo(SMAIndicatorDetails other)
	  {
		if(signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("buy") && (other.signalSMAToSMA==null || !other.signalSMAToSMA.equalsIgnoreCase("buy"))){
			return 1;
		}

		if(signalSMAToSMA!=null && signalSMAToSMA=="buy" && (other.signalSMAToSMA!=null || other.signalSMAToSMA.equalsIgnoreCase("put"))) {
			return 1;
		}

		if(signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("buy") && (other.signalSMAToSMA!=null || other.signalSMAToSMA.equalsIgnoreCase("buy"))) {
			if(signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("buy") && (other.signalPriceToSMA==null || other.signalPriceToSMA.equalsIgnoreCase("put"))) {
				return 1;
			}
		}
		
		if(signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("buy")) {
			if(signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("buy") && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
				if(percentagePriceChange>other.percentagePriceChange) {
					return 1;
				}
			}
		}
		
		if(signalSMAToSMA==null && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("put")) {
			return 1;
		}
		
		if(signalSMAToSMA==null && signalPriceToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA==null && (other.signalPriceToSMA == null || !other.signalPriceToSMA.equalsIgnoreCase("buy"))) {
			return 1;
		}
		
		if(signalSMAToSMA==null && signalPriceToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA==null && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
			if(percentagePriceChange>other.percentagePriceChange) {
				return 1;
			}
		}
		
		return -1;

			

			
				


		
		//Handle buy cases
		if(signalSMAToSMA != null && signalSMAToSMA.equalsIgnoreCase("buy") && (other.signalSMAToSMA == null || !other.signalSMAToSMA.equalsIgnoreCase("buy"))) {
	    	//First object is first
	    	return 1;
	    } else if(signalSMAToSMA!=null && !signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("buy")) {
	    	//argument object is first
	    	return -1;
	    } else if((signalSMAToSMA!=null && !signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA!=null && !other.signalSMAToSMA.equalsIgnoreCase("buy")) || (signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("buy") && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("buy")) ) {
	    	//Both have buy or not buy in signalSMAToSMA
	    	if(signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("buy")  && other.signalPriceToSMA!=null && !other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		return 1;
	    	} else if(signalPriceToSMA!=null && !signalPriceToSMA.equalsIgnoreCase("buy") && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		return -1;
	    	} else if(signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("buy") && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("buy")) {
	    		if(percentagePriceChange>= other.percentagePriceChange) {
	    			return 1;
	    		} else if(percentagePriceChange< other.percentagePriceChange) {
	    			return -1;
	    		}
	    	}
	    }
		
		// Handle put cases
		if(signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("put") && other.signalSMAToSMA!=null && !other.signalSMAToSMA.equalsIgnoreCase("put"))	{
			// First object is last
			return -1;
		}else if(signalSMAToSMA!=null && !signalSMAToSMA.equalsIgnoreCase("put") && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("put"))
		{
			// argument object is last
			return 1;
		}else if((signalSMAToSMA!=null && !signalSMAToSMA.equalsIgnoreCase("put") && other.signalSMAToSMA!=null && !other.signalSMAToSMA.equalsIgnoreCase("put")) || (signalSMAToSMA!=null && signalSMAToSMA.equalsIgnoreCase("put") && other.signalSMAToSMA!=null && other.signalSMAToSMA.equalsIgnoreCase("put"))) {
			// Both have put or not put in signalSMAToSMA
			if (signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("put") && other.signalPriceToSMA!=null && !other.signalPriceToSMA.equalsIgnoreCase("put")) {
				return -1;
			} else if (signalPriceToSMA!=null && !signalPriceToSMA.equalsIgnoreCase("put") && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("put")) {
				return 1;
			} else if (signalPriceToSMA!=null && signalPriceToSMA.equalsIgnoreCase("put") && other.signalPriceToSMA!=null && other.signalPriceToSMA.equalsIgnoreCase("put")) {
				if (percentagePriceChange >= other.percentagePriceChange) {
					return -1;
				} else if (percentagePriceChange < other.percentagePriceChange) {
					return 1;
				}
			}
		}
	    return 0;
	  }	*/
	
	
}
