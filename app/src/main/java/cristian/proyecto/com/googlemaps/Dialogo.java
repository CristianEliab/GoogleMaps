package cristian.proyecto.com.googlemaps;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


public class Dialogo extends DialogFragment {

    //widgets
    public String lugarCercano;
    private EditText mInput;
    private TextView mActionOk, mActionCancel;

    public Dialogo() {
    }


    //vars

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add, container, false);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(lugarCercano);
        return view;
    }

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static Dialogo newInstance(int num) {
        Dialogo f = new Dialogo();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
        }catch (ClassCastException e){
        }
    }



}
