package ru.n0de.manager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.gson.internal.LinkedTreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.text.Regex;

public class CirView extends CircleImageView {


    public CirView(Context context) {
        super(context);
    }

    public CirView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CirView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public String idContact = "";
    public String number = "";
    public String email = "";
    public String name = "";
    public Actions action;
    public boolean openCard = true;
    public boolean isDrag = true;
    private Drawable image = null;
    private String idContactTemp = "";

    public Boolean inContact() {
        return idContact.isEmpty();
    }

    public void clear(int idRes){
        idContact = "";
        number = "";
        email = "";
        name = "";
        setImageResource(idRes);
    }

    public Cir toCir(){
        return new Cir(idContact, number, email, name, action);
    }

    public void fromCir(Cir cir){
        idContact = cir.getIdContact();
        number = cir.getNumber();
        email = cir.getEmail();
        name = cir.getName();
        action = cir.getAction();
    }

    public void fromCir(LinkedTreeMap cir){
        idContact = cir.get("idContact").toString();
        number = cir.get("number").toString();
        email = cir.get("email").toString();
        name = cir.get("name").toString();
    }

    public void cloneTo(CirView cir){
        cir.fromCir(this.toCir());
        /*RelativeLayout.LayoutParams laypar = (RelativeLayout.LayoutParams) cir.getLayoutParams();
        RelativeLayout.LayoutParams layparThis = (RelativeLayout.LayoutParams) this.getLayoutParams();
        laypar.setMarginStart(layparThis.getMarginStart());
        laypar.se(layparThis.getMarginStart());*/
        cir.setLayoutParams(this.getLayoutParams());
    }

    public void swapCir(CirView cir){
        Cir cirTemp = cir.toCir();
        Drawable drawTemp = cir.getDrawable();
        cir.fromCir(this.toCir());
        cir.setImageDrawable(this.getDrawable());
        this.fromCir(cirTemp);
        this.setImageDrawable(drawTemp);
    }

    public void tempDel(int idRes){
        image = this.getDrawable();
        idContactTemp = idContact;
        idContact = "";
        this.setImageResource(idRes);
    }

    public void undoDel(){
        idContact = idContactTemp;
        this.setImageDrawable(image);
    }

    public boolean compareNumber(String num){
        num = new Regex("\\D").replace(num, "").trim();
        String temp = new Regex("\\D").replace(number, "").trim();
        //Log.d("CompareNumber: ", temp + " == " + num + " = " + temp.equals(num));
        return temp.equals(num);
    }
}
