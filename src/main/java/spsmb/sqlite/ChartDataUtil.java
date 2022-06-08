package spsmb.sqlite;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChartDataUtil {
    public static ObservableList<XYChart.Series<String, Number>> getCovidSeries(){
        XYChart.Series<String, Number> seriesPrumer = new XYChart.Series<>();
        seriesPrumer.setName("prumer");
        XYChart.Series<String, Number> seriesMuzi = new XYChart.Series<>();
        seriesMuzi.setName("muzi");
        XYChart.Series<String, Number> seriesZeny = new XYChart.Series<>();
        seriesZeny.setName("zeny");
        XYChart.Series<String, Number> seriesCelkem = new XYChart.Series<>();
        seriesCelkem.setName("celkem");
        Connection c = AMainDBConn.connect();
        String sql = "SELECT datum, AVG(vek) as vekovy_prumer,\n" +
                " (SELECT COUNT(*) FROM Covid as C2 where pohlavi like 'M' AND C2.datum = C1.datum) as pocet_muzu,\n" +
                " (SELECT COUNT(*) FROM Covid as C2 where pohlavi like 'Z' AND C2.datum = C1.datum) as pocet_zen,\n" +
                "COUNT(*) as celkovy_pocet FROM Covid as C1 GROUP BY datum;";
        try (Statement st = c.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            int cnt = 0;
            while(rs.next()) {
                seriesPrumer.getData().add(new XYChart.Data<>(rs.getString("datum"), (rs.getInt("vekovy_prumer"))));
                seriesMuzi.getData().add(new XYChart.Data<>(rs.getString("datum"), (rs.getInt("pocet_muzu"))));
                seriesZeny.getData().add(new XYChart.Data<>(rs.getString("datum"), (rs.getInt("pocet_zen"))));
                seriesCelkem.getData().add(new XYChart.Data<>(rs.getString("datum"), (rs.getInt("celkovy_pocet"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ObservableList<XYChart.Series<String, Number>> data =
                FXCollections.<XYChart.Series<String, Number>>observableArrayList();
        data.addAll(seriesPrumer, seriesMuzi, seriesZeny, seriesCelkem);
        //Warehouse p1 = new Warehouse(0, "Sharan", 4000 );
        return data; //FXCollections.<Warehouse>observableArrayList(p1);
    }
}
