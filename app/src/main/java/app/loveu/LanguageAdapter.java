package app.loveu;
import android.content.Context;import android.view.*;import android.widget.*;
/** Display translations but keep getItem()/selection bound to canonical stored values. */
final class LanguageAdapter extends ArrayAdapter<String> {
 LanguageAdapter(Context c,String[] values){super(c,android.R.layout.simple_spinner_dropdown_item,values);}
 public View getView(int position,View recycled,ViewGroup parent){TextView view=(TextView)super.getView(position,recycled,parent);view.setText(Language.ui(getItem(position)));return view;}
 public View getDropDownView(int position,View recycled,ViewGroup parent){TextView view=(TextView)super.getDropDownView(position,recycled,parent);view.setText(Language.ui(getItem(position)));return view;}
}
