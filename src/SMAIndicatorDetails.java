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
	
}
