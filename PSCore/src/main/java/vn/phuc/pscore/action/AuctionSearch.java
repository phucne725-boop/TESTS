package vn.phuc.pscore.auction;

import java.text.Normalizer;
import java.util.Locale;

public final class AuctionSearch {
    private AuctionSearch() {}
    public static String normalize(String input) {
        if (input == null) return "";
        String value = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT)
                .replace('đ', 'd').trim();
        return value.replaceAll("[^a-z0-9 _-]", "").replaceAll("\\s+", " ");
    }
    public static boolean matches(String query, String material, String seller) {
        String q = normalize(query);
        return q.isBlank() || normalize(material).contains(q) || normalize(seller).contains(q);
    }
}
