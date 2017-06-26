
public class SMAIndicatorDetails {
	String stockCode;
	float percentagePriceChange;
	String signalPriceToSMA; //"put" for options in case stock dropping down. "buy" in case price going up and crossed middleperiodSMA
	String signalSMAToSMA; //"put" for options in case stock dropping down. "buy" in case lower SMA going up and crossed higherperiodSMA
	
}
