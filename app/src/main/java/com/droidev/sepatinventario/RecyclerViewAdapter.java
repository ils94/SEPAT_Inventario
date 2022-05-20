package com.droidev.sepatinventario;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<String> banco;
    private RecyclerViewClickInterface recyclerViewClickInterface;

    public RecyclerViewAdapter(Context context, ArrayList<String> banco, RecyclerViewClickInterface recyclerViewClickInterface) {

        this.layoutInflater = LayoutInflater.from(context);
        this.banco = banco;
        this.recyclerViewClickInterface = recyclerViewClickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = layoutInflater.inflate(R.layout.row_visualizador, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String textId = banco.get(position);
        holder.visualizador.setText(textId);
    }

    @Override
    public int getItemCount() {
        return banco.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView visualizador;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnLongClickListener(v -> {

                recyclerViewClickInterface.onLongItemClick(getAdapterPosition());

                return true;
            });

            visualizador = itemView.findViewById(R.id.visualizador);
        }
    }
}
