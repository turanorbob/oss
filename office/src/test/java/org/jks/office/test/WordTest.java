package org.jks.office.test;

import org.jks.office.util.POIBuilder;

import java.io.IOException;

/**
 * Created by Administrator on 2017/6/11.
 */
public class WordTest {

    public static void main(String args[]) throws IOException {
        POIBuilder poi = new POIBuilder();
        poi.paragraph("SSSS").table("A")
                .cell(0, 0, "中文")
                .cell(1, 1, "hello")
                .cell(2,3,"ruyi")
                .save("liaojian");
    }
}
