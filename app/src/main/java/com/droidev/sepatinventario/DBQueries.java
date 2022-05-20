package com.droidev.sepatinventario;

import android.app.Activity;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DBQueries {

    ArrayList<String> visualizador;

    public ArrayList<String> carregar(Activity activity, Connection connection) {
        Thread thread = new Thread(() -> {

            try {

                Statement stmt;
                String sql = "SELECT * FROM INVENTARIO ORDER BY ID DESC";

                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                visualizador = new ArrayList<>();

                while (rs.next()) {

                    String id = rs.getString("ID");
                    String descricao = rs.getString("DESCRICAO");
                    String QTD = rs.getString("QUANTIDADE");
                    String local = rs.getString("LOCAL");
                    String dataHora = rs.getString("DATA_HORA");

                    visualizador.add("ID: " + id + "\nDescrição: " + descricao + "\nQuantidade: " + QTD + "\nLocal: " + local + "\nÚltima Modificação: " + dataHora);

                }

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }

        return visualizador;
    }

    public ArrayList<String> pesquisar(Activity activity, Connection connection, String string) {
        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;

                String sql = "SELECT * FROM INVENTARIO WHERE " +
                        "DESCRICAO ILIKE ? " +
                        "OR QUANTIDADE ILIKE ? " +
                        "OR LOCAL ILIKE ? " +
                        "OR DATA_HORA ILIKE ? " +
                        "ORDER BY ID DESC";

                pst = connection.prepareStatement(sql);
                pst.setString(1, "%" + string + "%");
                pst.setString(2, "%" + string + "%");
                pst.setString(3, "%" + string + "%");
                pst.setString(4, "%" + string + "%");

                ResultSet rs = pst.executeQuery();

                visualizador = new ArrayList<>();

                while (rs.next()) {

                    String id = rs.getString("ID");
                    String descricao = rs.getString("DESCRICAO");
                    String QTD = rs.getString("QUANTIDADE");
                    String local = rs.getString("LOCAL");
                    String dataHora = rs.getString("DATA_HORA");

                    visualizador.add("ID: " + id + "\nDescrição: " + descricao + "\nQuantidade: " + QTD + "\nLocal: " + local + "\nÚltima Modificação: " + dataHora);
                }

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }

        return visualizador;
    }

    public void alterar(Activity activity, Connection connection, String descricao, String quantidade, String local, String dataHora, String id) {

        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;

                String sql = "UPDATE INVENTARIO SET DESCRICAO = ?, QUANTIDADE = ?, LOCAL = ?, DATA_HORA = ? WHERE ID = ?";

                pst = connection.prepareStatement(sql);

                pst.setString(1, descricao.toUpperCase());
                pst.setString(2, quantidade.toUpperCase());
                pst.setString(3, local.toUpperCase());
                pst.setString(4, dataHora.toUpperCase());
                pst.setInt(5, Integer.parseInt(id));

                pst.executeUpdate();

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }

    public void inserir(Activity activity, Connection connection, String descricao, String quantidade, String local, String dataHora) {
        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;

                String sql = "INSERT INTO INVENTARIO (DESCRICAO, QUANTIDADE, LOCAL, DATA_HORA) VALUES (?, ?, ?, ?)";

                pst = connection.prepareStatement(sql);

                pst.setString(1, descricao.toUpperCase());
                pst.setString(2, quantidade.toUpperCase());
                pst.setString(3, local.toUpperCase());
                pst.setString(4, dataHora.toUpperCase());

                pst.executeUpdate();

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }

    public void deletar(Activity activity, Connection connection, String id) {

        Thread thread = new Thread(() -> {

            try {

                PreparedStatement pst;

                String sql = "DELETE FROM INVENTARIO WHERE ID = ?";

                pst = connection.prepareStatement(sql);

                pst.setInt(1, Integer.parseInt(id));

                pst.executeUpdate();

            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            activity.runOnUiThread(() -> Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
        }
    }
}
