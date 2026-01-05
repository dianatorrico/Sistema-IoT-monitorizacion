package com.example.apppecl3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceAdapter
        extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(Dispositivo dispositivo);
    }

    private final List<Dispositivo> dispositivos;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<Dispositivo> dispositivos,
                         OnDeviceClickListener listener) {
        this.dispositivos = dispositivos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);

        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DeviceViewHolder holder, int position) {

        Dispositivo d = dispositivos.get(position);

        holder.tvDeviceName.setText(d.getDispositivoId());
        holder.tvStreet.setText("Calle " + d.getCalleId());


        // 🔴 CLICK GARANTIZADO AQUÍ
        holder.itemView.setOnClickListener(v -> {
            Log.i("ADAPTER", "CLICK en dispositivo: " + d.getDispositivoId());


            if (listener != null) {
                listener.onDeviceClick(d);
            } else {
                Log.e("ADAPTER", "Listener es NULL");
            }
        });
        holder.itemView.setOnClickListener(v -> {

            Log.i("DEBUG_CLICK", "ID: " + d.getDispositivoId());
            Log.i("DEBUG_CLICK", "ID: " + d.getNombre());
            Log.i("DEBUG_CLICK", "CALLE: " + d.getCalleId());
            Log.i("DEBUG_CLICK", "TIPO: " + d.getTipoDispositivo());
            Log.i("DEBUG_CLICK", "ACTIVO: " + d.isActivo());

            if (listener != null) {
                listener.onDeviceClick(d);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dispositivos.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        ImageView imgDevice;
        TextView tvDeviceName;
        TextView tvStreet;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);

            imgDevice = itemView.findViewById(R.id.imgDevice);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvStreet = itemView.findViewById(R.id.tvStreet);

            // 🔒 Forzar clicabilidad
            itemView.setClickable(true);
            itemView.setFocusable(true);
        }
    }
}
