package app.loveu;
import android.app.AlertDialog;import android.content.*;
/** Translate presentation labels, keeping item indexes and listeners unchanged. */
final class LanguageDialog extends AlertDialog.Builder {
 LanguageDialog(Context c){super(c);}
 public AlertDialog.Builder setTitle(CharSequence s){return super.setTitle(Language.ui(s));}
 public AlertDialog.Builder setMessage(CharSequence s){return super.setMessage(Language.ui(s));}
 public AlertDialog.Builder setPositiveButton(CharSequence s,DialogInterface.OnClickListener l){return super.setPositiveButton(Language.ui(s),l);}
 public AlertDialog.Builder setNegativeButton(CharSequence s,DialogInterface.OnClickListener l){return super.setNegativeButton(Language.ui(s),l);}
 public AlertDialog.Builder setNeutralButton(CharSequence s,DialogInterface.OnClickListener l){return super.setNeutralButton(Language.ui(s),l);}
 public AlertDialog.Builder setItems(CharSequence[] s,DialogInterface.OnClickListener l){return super.setItems(Language.items(s),l);}
 public AlertDialog.Builder setSingleChoiceItems(CharSequence[] s,int checked,DialogInterface.OnClickListener l){return super.setSingleChoiceItems(Language.items(s),checked,l);}
 public AlertDialog.Builder setMultiChoiceItems(CharSequence[] s,boolean[] checked,DialogInterface.OnMultiChoiceClickListener l){return super.setMultiChoiceItems(Language.items(s),checked,l);}
}
