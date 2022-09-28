package com.droidev.sepatinventario;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickInterface {

    private ArrayList<String> banco;

    RecyclerView RecyclerView;
    RecyclerView.Adapter Adapter;

    EditText id, descricao, quantidade, local;
    Button cadastrar, alterar;

    private Boolean confirmar = false;
    private String data, hora;

    String deviceID;

    TinyDB tinyDB;

    DBQueries dbQueries;

    public static Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        dbQueries = new DBQueries();

        tinyDB = new TinyDB(MainActivity.this);

        RecyclerView = findViewById(R.id.visualizador_recycle);

        id = findViewById(R.id.ID);
        descricao = findViewById(R.id.descricao);
        quantidade = findViewById(R.id.qtd);
        local = findViewById(R.id.local);

        cadastrar = findViewById(R.id.cadastrar);

        alterar = findViewById(R.id.alterar);

        cadastrar.setOnClickListener(v -> cadastrar());

        alterar.setOnClickListener(v -> alterar());

        conectarBanco();

        getDeviceID();

        Uri uri = getIntent().getData();

        if (uri != null) {

            String path = uri.toString();

            deepLink(path.replace("https://sepatinventario.db/", ""));
        }
    }

    @Override
    public void onLongItemClick(int position) {

        String[] result = ((banco.get(position).replace("ID: ", "").replace("Descrição: ", "").replace("Quantidade: ", "").replace("Local: ", "")).split("\n"));

        id.setText(result[0]);
        descricao.setText(result[1]);
        quantidade.setText(result[2]);
        local.setText(result[3]);
    }

    @Override
    public void onBackPressed() {
        if (confirmar) {

            finish();
        } else {
            Toast.makeText(this, "Aperte voltar novamente para fechar o app.",
                    Toast.LENGTH_SHORT).show();
            confirmar = true;
            new Handler().postDelayed(() -> confirmar = false, 3 * 1000);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.carregar:

                carregar();

                break;

            case R.id.pesquisar:

                pesquisar();

                break;

            case R.id.login:

                login();

                break;

            case R.id.compartLink:

                if (tinyDB.getString("dbName").isEmpty()) {

                    Toast.makeText(this, "Ainda não há nenhuma credencial salva.", Toast.LENGTH_SHORT).show();
                } else {

                    String link = "https://sepatinventario.db/"
                            + tinyDB.getString("dbName")
                            + "/" + tinyDB.getString("dbUser")
                            + "/" + tinyDB.getString("dbPass")
                            + "/" + tinyDB.getString("dbHost")
                            + "/" + tinyDB.getString("dbPort");

                    Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                    shareLinkIntent.setType("text/plain");
                    shareLinkIntent.putExtra(Intent.EXTRA_TEXT, link);
                    startActivity(Intent.createChooser(shareLinkIntent, "Compartilhar link com..."));
                }

                break;

            case R.id.deviceID:

                showDeviceID();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HardwareIds")
    public void getDeviceID() {

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void deepLink(String link) {

        String[] linkArray = link.split("/");

        limparChaves();

        tinyDB.putString("dbName", linkArray[0]);
        tinyDB.putString("dbUser", linkArray[1]);
        tinyDB.putString("dbPass", linkArray[2]);
        tinyDB.putString("dbHost", linkArray[3]);
        tinyDB.putString("dbPort", linkArray[4]);

        Toast.makeText(MainActivity.this, "Salvo.", Toast.LENGTH_SHORT).show();

        new Thread(() -> {

            try {

                if (!(connection == null)) {

                    connection.close();
                }

                conectarBanco();
            } catch (SQLException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void showDeviceID() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Device ID: " + deviceID)
                .setPositiveButton("Ok", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> dialog.dismiss());
    }

    public void dataHora() {

        data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        data = data.replace("/01/", "/jan/")
                .replace("/02/", "/fev/")
                .replace("/03/", "/mar/")
                .replace("/04/", "/abr/")
                .replace("/05/", "/mai/")
                .replace("/06/", "/jun/")
                .replace("/07/", "/jul/")
                .replace("/08/", "/ago/")
                .replace("/09/", "/set/")
                .replace("/10/", "/out/")
                .replace("/11/", "/nov/")
                .replace("/12/", "/dez/")
                .toUpperCase();

        hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void limparChaves() {

        tinyDB.remove("dbName");
        tinyDB.remove("dbUser");
        tinyDB.remove("dbPass");
        tinyDB.remove("dbHost");
        tinyDB.remove("dbPort");
    }

    private void conectarBanco() {

        new Thread(() -> {
            try {

                String dbHost = tinyDB.getString("dbHost");
                String dbPort = tinyDB.getString("dbPort");
                String dbName = tinyDB.getString("dbName");
                String dbUser = tinyDB.getString("dbUser");
                String dbPass = tinyDB.getString("dbPass");

                String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

                if (!dbName.isEmpty()) {

                    connection = DriverManager.getConnection(url, dbUser, dbPass);
                }

            } catch (SQLException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @SuppressLint("SetTextI18n")
    private void login() {

        EditText dbName = new EditText(this);
        dbName.setHint("dbName");
        dbName.setInputType(InputType.TYPE_CLASS_TEXT);
        dbName.setMaxLines(1);

        EditText dbUser = new EditText(this);
        dbUser.setHint("dbUser");
        dbUser.setInputType(InputType.TYPE_CLASS_TEXT);
        dbUser.setMaxLines(1);

        EditText dbPass = new EditText(this);
        dbPass.setHint("dbPass");
        dbPass.setInputType(InputType.TYPE_CLASS_TEXT);
        dbPass.setMaxLines(1);

        EditText dbHost = new EditText(this);
        dbHost.setHint("dbHost");
        dbHost.setInputType(InputType.TYPE_CLASS_TEXT);
        dbHost.setMaxLines(1);

        EditText dbPort = new EditText(this);
        dbPort.setHint("dbPort");
        dbPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        dbPort.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(dbName);
        lay.addView(dbUser);
        lay.addView(dbPass);
        lay.addView(dbHost);
        lay.addView(dbPort);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Login")
                .setMessage("Insira as credenciais do banco de dados abaixo:")
                .setPositiveButton("Salvar", null)
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Limpar Tudo", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        dbName.setText(tinyDB.getString("dbName"));
        dbUser.setText(tinyDB.getString("dbUser"));
        dbPass.setText(tinyDB.getString("dbPass"));
        dbHost.setText(tinyDB.getString("dbHost"));
        dbPort.setText(tinyDB.getString("dbPort"));

        if (dbPort.getText().toString().isEmpty()) {

            dbPort.setText("5432");
        }

        positiveButton.setOnClickListener(v -> {

            if (dbName.getText().toString().isEmpty() || dbUser.getText().toString().isEmpty() || dbPass.getText().toString().isEmpty() || dbHost.getText().toString().isEmpty() || dbPort.getText().toString().isEmpty()) {

                Toast.makeText(MainActivity.this, "Os campos não podem esta vazios.", Toast.LENGTH_SHORT).show();

            } else {

                limparChaves();

                tinyDB.putString("dbName", dbName.getText().toString());
                tinyDB.putString("dbUser", dbUser.getText().toString());
                tinyDB.putString("dbPass", dbPass.getText().toString());
                tinyDB.putString("dbHost", dbHost.getText().toString());
                tinyDB.putString("dbPort", dbPort.getText().toString());

                dialog.dismiss();

                Toast.makeText(MainActivity.this, "Salvo.", Toast.LENGTH_SHORT).show();

                new Thread(() -> {

                    try {

                        if (!(connection == null)) {

                            connection.close();
                        }

                        conectarBanco();
                    } catch (SQLException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }
        });

        neutralButton.setOnClickListener(v -> {

            dbName.setText("");
            dbUser.setText("");
            dbPass.setText("");
            dbHost.setText("");
        });
    }

    public void cadastrar() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Cadastrar")
                .setMessage("Cadastrar uma nova entrada no banco de dados?")
                .setPositiveButton("Sim", null)
                .setNegativeButton("Cancelar", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (!descricao.getText().toString().equals("") && !quantidade.getText().toString().equals("") && !local.getText().toString().equals("")) {

                try {

                    if (connection.isClosed() || connection == null) {

                        conectarBanco();

                    } else {

                        dataHora();

                        dbQueries.inserir(MainActivity.this, connection, descricao.getText().toString(), quantidade.getText().toString(), local.getText().toString(), data + " - " + hora, deviceID);

                        id.setText("");
                        descricao.setText("");
                        quantidade.setText("");
                        local.setText("");

                        carregar();

                        dialog.dismiss();
                    }
                } catch (Exception e) {

                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }

            } else {

                Toast.makeText(this, "Os campos não podem estar vazios.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });
    }

    public void alterar() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Alterar")
                .setMessage("Alterar o bem com ID: " + id.getText().toString() + " ?")
                .setPositiveButton("Sim", null)
                .setNegativeButton("Cancelar", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (!descricao.getText().toString().isEmpty() && !quantidade.getText().toString().isEmpty() && !local.getText().toString().isEmpty() && !id.getText().toString().isEmpty()) {

                try {

                    if (connection.isClosed() || connection == null) {

                        conectarBanco();

                    } else {

                        dataHora();

                        dbQueries.alterar(MainActivity.this, connection, descricao.getText().toString(), quantidade.getText().toString(), local.getText().toString(), data + " - " + hora, deviceID, id.getText().toString());

                        id.setText("");
                        descricao.setText("");
                        quantidade.setText("");
                        local.setText("");

                        carregar();

                        dialog.dismiss();
                    }
                } catch (Exception e) {

                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }

            } else {

                Toast.makeText(this, "Os campos não podem estar vazios.", Toast.LENGTH_LONG).show();

                dialog.dismiss();
            }
        });
    }

    public void carregar() {

        try {

            if (connection.isClosed() || connection == null) {

                conectarBanco();

            } else {

                banco = new ArrayList<>();

                RecyclerView = findViewById(R.id.visualizador_recycle);
                RecyclerView.setLayoutManager(new LinearLayoutManager(this));
                Adapter = new RecyclerViewAdapter(this, banco, this);
                RecyclerView.setAdapter(Adapter);

                banco.addAll(dbQueries.carregar(MainActivity.this, connection));
            }
        } catch (Exception e) {

            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pesquisar() {

        EditText editTextPesquisar = new EditText(this);
        editTextPesquisar.setInputType(InputType.TYPE_CLASS_TEXT);
        editTextPesquisar.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(editTextPesquisar);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Pesquisar no Banco")
                .setPositiveButton("Pesquisar", null)
                .setNegativeButton("Cancelar", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            try {

                if (connection.isClosed() || connection == null) {

                    conectarBanco();

                } else {

                    banco = new ArrayList<>();

                    RecyclerView = findViewById(R.id.visualizador_recycle);
                    RecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    Adapter = new RecyclerViewAdapter(MainActivity.this, banco, MainActivity.this);
                    RecyclerView.setAdapter(Adapter);

                    banco.addAll(dbQueries.pesquisar(MainActivity.this, connection, editTextPesquisar.getText().toString()));

                    dialog.dismiss();
                }
            } catch (Exception e) {

                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

        });
    }
}