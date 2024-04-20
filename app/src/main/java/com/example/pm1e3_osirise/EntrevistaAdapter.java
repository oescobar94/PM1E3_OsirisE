package com.example.pm1e3_osirise;

import android.content.Context;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm1e3_osirise.R;
import com.example.pm1e3_osirise.configuracion.Entrevista;
import com.example.pm1e3_osirise.configuracion.Utilidades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class EntrevistaAdapter extends RecyclerView.Adapter<EntrevistaAdapter.EntrevistaViewHolder>{

    private Context context;
    private List<Entrevista> listaEntrevistas;
    private List<Boolean> seleccionados;

    public EntrevistaAdapter(Context context, List<Entrevista> listaEntrevistas) {
        this.context = context;
        this.listaEntrevistas = listaEntrevistas;
        seleccionados = new ArrayList<>(Collections.nCopies(listaEntrevistas.size(), false));
    }

    @NonNull
    @Override
    public EntrevistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entrevistas, parent, false);
        return new EntrevistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrevistaViewHolder holder, int position) {
        Entrevista entrevista = listaEntrevistas.get(position);

        // Asignar datos a las vistas en el ViewHolder
        holder.txtId.setText(entrevista.getId());
        holder.txtDescripcion.setText(entrevista.getDescripcion());
        holder.textViewPeriodista.setText(entrevista.getPeriodista());
        holder.textViewFecha.setText(entrevista.getFecha());
        Bitmap imagenBitmap = Utilidades.base64ToBitmap(entrevista.getImagenBase64());
        holder.imageViewEntrevista.setImageBitmap(imagenBitmap);

        // Guardar el ID de Firestore junto con los datos de la entrevista
        holder.itemView.setTag(entrevista.getIdFirestore());

        // Controlar la selección del elemento
        holder.itemView.setActivated(seleccionados.get(position));
        // Manejar clics en los elementos de la lista
        holder.itemView.setOnClickListener(v -> {
            seleccionados.set(position, !seleccionados.get(position));
            holder.itemView.setActivated(seleccionados.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return listaEntrevistas.size();
    }

    public void actualizarListaDespuesEliminar(List<String> idsEliminar) {
        for (String id : idsEliminar) {
            for (int i = 0; i < listaEntrevistas.size(); i++) {
                if (listaEntrevistas.get(i).getIdFirestore().equals(id)) {
                    listaEntrevistas.remove(i);
                    seleccionados.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }

    public List<String> obtenerIdsSeleccionados() {
        List<String> idsSeleccionados = new ArrayList<>();
        for (int i = 0; i < listaEntrevistas.size(); i++) {
            if (seleccionados.get(i)) {
                // Agregar el ID de Firestore de los elementos seleccionados
                idsSeleccionados.add(listaEntrevistas.get(i).getIdFirestore());
            }
        }
        return idsSeleccionados;
    }


    public static class EntrevistaViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtDescripcion, textViewFecha, textViewPeriodista;
        ImageView imageViewEntrevista;

        public EntrevistaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializar vistas
            txtId = itemView.findViewById(R.id.text_id);
            txtDescripcion = itemView.findViewById(R.id.textViewDescripcion);
            textViewFecha = itemView.findViewById(R.id.textViewFecha);
            textViewPeriodista = itemView.findViewById(R.id.textViewPeriodista);
            imageViewEntrevista = itemView.findViewById(R.id.imageViewEntrevista);
        }
    }
}
