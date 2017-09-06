import java.time.LocalDate;

public class SMAIndicatorDetails {// implements Comparable<SMAIndicatorDetails> {
	String stockCode;
	double stockPrice;
	boolean pricegrowth = false;
	float percentagePriceChange;
	String signalPriceToSMA; //"put" for options in case stock dropping down. "buy" in case price going up and crossed middleperiodSMA
	float priceToSMApercentageDeviation;
	String signalSMAToSMA; //"put" for options in case stock dropping down. "buy" in case lower SMA going up and crossed higherperiodSMA
	float SMAToSMApercentageDeviation;
	LocalDate signalDate;
	boolean PNSMAcrossover = false;
	boolean SMNSMcrossover = false;
	boolean PNSMcontinuousGrowth = false;
	boolean SMNSMcontinuousGrowth = false;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stockCode == null) ? 0 : stockCode.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SMAIndicatorDetails other = (SMAIndicatorDetails) obj;
		if (stockCode == null) {
			if (other.stockCode != null)
				return false;
		} else if (!stockCode.equals(other.stockCode))
			return false;
		return true;
	}
	
	
}
