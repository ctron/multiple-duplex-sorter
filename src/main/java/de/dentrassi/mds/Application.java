package de.dentrassi.mds;

import java.util.Arrays;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

public class Application {

    public static void main(String[] args) throws Exception {

        if (args.length != 4) {
            System.err.println("Need 4 arguments: <input.pdf> <output.pdf> <num-x> <num-y>");
            System.exit(1);
        }

        var input = args[0];
        var output = args[1];
        var w = Integer.parseInt(args[2]);
        var h = Integer.parseInt(args[3]);

        try (
                var src = new PdfDocument(new PdfReader(input));
                var dst = new PdfDocument(new PdfWriter(output));
        ) {

            dst.initializeOutlines();

            transferSorted(src, dst, w, h);
        }

    }

    static void transferSorted(PdfDocument src, PdfDocument dst, int width, int height) {

        // page: a page in the source document
        // sheet: the printed sheet, containing width x height pages

        // total number of pages to copy
        var len = src.getNumberOfPages();
        System.out.format("Total Pages: %s%n", len);


        // number of pages on a single sheet
        var num = width * height;

        // number of sheets
        var sheets = Math.ceil(((double) len) / ((double) num)) / 2.0;
        System.out.format("Final Sheets: %s%n", sheets);

        //  apply target order to sheets
        for (var i = 0; i < sheets; i++) {
            var order = order(i * num * 2, width, height, len);

            System.out.format("%s: %s%n", i, Arrays.toString(order));

            for (var p : order) {
                if (p > 0) {
                    src.copyPagesTo(p, p, dst);
                } else {
                    // add blank page
                    dst.addNewPage();
                }
            }

        }

    }

    static int[] order(int start, int width, int height, int max) {
        // front and backside, each with width x height pages
        var result = new int[width * height * 2];

        // odd pages first, in order
        for (var i = 0; i < width * height; i++) {
            result[i] = orBlank(start + i * 2 + 1, max);
        }

        // even pages next, but with reversed rows

        var s = width * height;
        var i = 0;
        for (var y = 0; y < height; y++) {
            for (var x = width; x > 0; x--) {
                result[s + (y * width + (x - 1))] = orBlank(start + (i * 2) + 2, max);
                i++;
            }
        }

        return result;
    }

    // return the page index to copy from, or -1 if we need to insert a blank page
    static int orBlank(int page, int max) {
        return page <= max ? page : -1;
    }
}