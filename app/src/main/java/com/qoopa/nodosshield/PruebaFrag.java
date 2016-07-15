package com.qoopa.nodosshield;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PruebaFrag extends Fragment { // ESTO NO HACE NADA, SE USO PARA PRUEBAS
//public class HelloFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflating the layout for this fragment **/
        View v = inflater.inflate(R.layout.fragment_prueba, null);
        return v;
    }
}
