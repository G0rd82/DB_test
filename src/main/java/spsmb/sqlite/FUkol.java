package spsmb.sqlite;
// Převeďte data z https://onemocneni-aktualne.mzcr.cz/api/v2/covid-19
// (COVID-19: Přehled osob s prokázanou nákazou dle hlášení krajských
// hygienických stanic (v2)).
// K nalezení zde: X:\stemberk\verejne_zaci\osoby.csv
// do databáze (můžete si vybrat mezi SQLite, či MySQL).
// Totéž proveďte pro soubor X:\stemberk\verejne_zaci\staty.csv a zobrazte
// v TableView, případně v nějakém grafu JavaFX.

// Vytvořte další TableView, které bude ze stejných dat zobrazovat následující souhrn
// po dnech: datum, průměrný věk, počet mužů, počet žen, celkový počet
// SELECT datum, AVG(vek) as vekovy_prumer,
// (SELECT COUNT(*) FROM Covid as C2 where pohlavi like 'M' AND C2.datum = C1.datum) as pocet_muzu,
// (SELECT COUNT(*) FROM Covid as C2 where pohlavi like 'Z' AND C2.datum = C1.datum) as pocet_zen,
// COUNT(*) as celkovy_pocet FROM Covid as C1 GROUP BY datum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/**
     * Struktura tabulky:
     * id,datum,vek,pohlavi,kraj_nuts_kod,okres_lau_kod,nakaza_v_zahranici,nakaza_zeme_csu_kod,reportovano_khs
     * 1ea976a2-896a-40b2-b617-b780a713323d,2020-03-01,43,M,CZ042,CZ0421,1,IT,1
     */
public class FUkol {
    public static void createTable() {
        Connection c = null;
        Statement stmt = null;
        try {
            //Class.forName("org.w32.sqlite.JDBC");
            //c = DriverManager.getConnection("jdbc:w32.sqlite:SqliteJavaDB.db");
            c = AMainDBConn.connect();
            System.out.println("Database Opened...\n");
            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Covid " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " id2 varchar(50), " +
                    " datum varchar (50), " +
                    " vek int, " +
                    " pohlavi varchar (25), " +
                    " kraj_nuts_kod varchar (10), " +
                    " okres_lau_kod varchar (20), " +
                    " nakaza_v_zahranici bit , " +
                    " nakaza_zeme_csu_kod varchar(10) , " +
                    " reportovano_khs bit)";
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table Product Created Successfully!!!");
    }
    public static  void insert(String id2, String datum, Integer vek, String pohlavi, String kraj_nuts_kod, String okres_lau_kod, Boolean nakaza_v_zahranici, String nakaza_zeme_csu_kod, Boolean reportovano_khs) {
        String sql = "INSERT INTO Covid(id2, datum, vek, pohlavi, kraj_nuts_kod, okres_lau_kod, nakaza_v_zahranici, nakaza_zeme_csu_kod, reportovano_khs)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = AMainDBConn.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id2);
            pstmt.setString(2, datum);
            pstmt.setInt(3, vek);
            pstmt.setString(4, pohlavi);
            pstmt.setString(5, kraj_nuts_kod);
            pstmt.setString(6, okres_lau_kod);
            pstmt.setBoolean(7, nakaza_v_zahranici);
            pstmt.setString(8, nakaza_zeme_csu_kod);
            pstmt.setBoolean(9, reportovano_khs);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) throws IOException {
        FileReader fr = null;
        String radka;
        int cnt = 0;
        FUkol.createTable();
        fr = new FileReader("X:\\stemberk\\verejne_zaci\\osoby.csv");
        BufferedReader br = new BufferedReader(fr);
        br.readLine(); // prvni radku zahodime;
        while ((radka = br.readLine()) != null) {
            String iu = "";
            String datum = "";
            int vek = 0;
            String mf = "";
            String kraj = "";
            String okres = "";
            boolean vZahranici = false;
            String stat = "";
            boolean reportovanoKhs = false;
            String[] hodnoty = radka.split(",");
            if (hodnoty.length > 0) iu = hodnoty[0];
            if (hodnoty.length > 1) datum = hodnoty[1];
            if (hodnoty.length > 2) vek = Integer.parseInt(hodnoty[2]);
            if (hodnoty.length > 3) mf = hodnoty[3];
            if (hodnoty.length > 4) kraj = hodnoty[4];
            if (hodnoty.length > 5) okres = hodnoty[5];
            if (hodnoty.length > 6) vZahranici = hodnoty[6].equals("1") ? true : false;
            if (hodnoty.length > 7) stat = hodnoty[7];
            if (hodnoty.length > 8) reportovanoKhs = hodnoty[8].equals("1") ? true : false;
            System.out.printf("%s, %s, %d, %s, %s, %s, %b, %s, %b%n",
                    iu, datum, vek, mf, kraj, okres, vZahranici, stat, reportovanoKhs);
            FUkol.insert(iu, datum, vek, mf, kraj, okres, vZahranici, stat, reportovanoKhs);
            if (cnt++ > 5000) break;
        }
    }
}

