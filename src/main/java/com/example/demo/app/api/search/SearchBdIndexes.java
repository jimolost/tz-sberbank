package com.example.demo.app.api.search;

import java.util.List;

public final class SearchBdIndexes {
    /**
     * Индекс MDM
     */
    public static final String CLIENT_MDM = "client_mdm";
    /**
     * Индекс ПКБ
     */
    public static final String CLIENT_PKB = "client_pkb";
    /**
     * Индекс ЦФТ
     */
    public static final String CLIENT_CFT = "client_cft";

    public static final List<String> ALL = List.of(CLIENT_MDM, CLIENT_PKB, CLIENT_CFT);
}
