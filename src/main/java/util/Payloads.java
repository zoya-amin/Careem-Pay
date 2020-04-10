package util;

public class Payloads {
    // Entertainment Vouchers
    public static String  generateInvoice(){
        return "{\"promoCode\":\"\"}";
    }
    public static String purchaseVoucher(String invoiceId) {
        return  "{\"paymentInstrument\":{\"useBalance\":true},\"invoiceId\":\""+ invoiceId+"\"}";    }

}
