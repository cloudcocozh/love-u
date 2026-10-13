package app.loveu;
import android.content.Context;import android.widget.*;
/** Retain source labels separately so translated display text never becomes a stored enum. */
final class LanguageViews {
 interface Source{String source();}
 static class Text extends TextView implements Source{private String original;Text(Context c){super(c);}public void setSingleLine(boolean single){super.setSingleLine(single);if(single)setHorizontallyScrolling(false);}public void setText(CharSequence s,BufferType t){if(original!=null&&s!=null&&s.toString().contentEquals(getText())){super.setText(s,t);return;}original=s==null?"":s.toString();super.setText(Language.ui(s),t);}public String source(){return original==null?"":original;}}
 static class Action extends Button implements Source{private String original;Action(Context c){super(c);}public void setSingleLine(boolean single){super.setSingleLine(single);if(single)setHorizontallyScrolling(false);}public void setText(CharSequence s,BufferType t){if(original!=null&&s!=null&&s.toString().contentEquals(getText())){super.setText(s,t);return;}original=s==null?"":s.toString();super.setText(Language.ui(s),t);}public String source(){return original==null?"":original;}}
 static class Check extends CheckBox implements Source{private String original;Check(Context c){super(c);}public void setText(CharSequence s,BufferType t){if(original!=null&&s!=null&&s.toString().contentEquals(getText())){super.setText(s,t);return;}original=s==null?"":s.toString();super.setText(Language.ui(s),t);}public String source(){return original==null?"":original;}}
 static class Toggle extends Switch implements Source{private String original;Toggle(Context c){super(c);}public void setText(CharSequence s,BufferType t){if(original!=null&&s!=null&&s.toString().contentEquals(getText())){super.setText(s,t);return;}original=s==null?"":s.toString();super.setText(Language.ui(s),t);}public String source(){return original==null?"":original;}}
}
