package util;

public class Payloads {
    // Entertainment Vouchers
    public static String  generateInvoice(String promoCode){
        return "{\"promoCode\":\""+promoCode+"\"}";
    }
    public static String purchaseVoucher(String invoiceId) {
        return  "{\"paymentInstrument\":{\"useBalance\":true},\"invoiceId\":\""+ invoiceId+"\"}";    }

    public static String topUpCard(int amount, String instrumentationId){
        return " {\"paymentInstrument\":{\"useBalance\":false,\"id\":\""+instrumentationId+"\"},\"total\":{\"amount\":"+amount+",\"currency\":\"AED\"}}";
    }

}
