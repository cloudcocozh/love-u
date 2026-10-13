package app.loveu;

/** Immutable semantic colors; intentionally independent of Android for JVM tests. */
final class ThemePalette {
 final int accent, ink, muted, background, surface, soft, secondary, border, onAccent, heroStart, heroEnd;
 final boolean dark;
 private ThemePalette(boolean dark,int accent,int ink,int muted,int background,int surface,int soft,int secondary,int border,int onAccent,int heroStart,int heroEnd){
  this.dark=dark;this.accent=accent;this.ink=ink;this.muted=muted;this.background=background;this.surface=surface;this.soft=soft;this.secondary=secondary;this.border=border;this.onAccent=onAccent;this.heroStart=heroStart;this.heroEnd=heroEnd;
 }
 static ThemePalette resolve(String palette,boolean dark){
  if("teal".equals(palette))return dark
   ?new ThemePalette(true,0xFFA0DDD7,0xFFF0FAF8,0xFFA8C5C1,0xFF071A1B,0xFF10282A,0xFF1B3E40,0xFF71B8B1,0xFF294647,0xFF082F31,0xFF285B5C,0xFF183537)
   :new ThemePalette(false,0xFF246F70,0xFF143A3D,0xFF657E7E,0xFFF4F9F8,0xFFFDFFFF,0xFFE0EFEB,0xFF68A6A2,0xFFE0EAE7,0xFFFFFFFF,0xFFBEDFDB,0xFFE9F3D7);
  if("amber".equals(palette))return dark
   ?new ThemePalette(true,0xFFF3CC8E,0xFFFFF5E7,0xFFC7B499,0xFF1E1711,0xFF2A2119,0xFF443324,0xFFD8A969,0xFF4B3B2A,0xFF392410,0xFF60482D,0xFF37291B)
   :new ThemePalette(false,0xFF94602D,0xFF392717,0xFF8A7660,0xFFFCF8F1,0xFFFFFDF9,0xFFF5E9D7,0xFFC89A5D,0xFFEDE4D7,0xFFFFFFFF,0xFFF1DBC3,0xFFFFEDC0);
  return dark
   ?new ThemePalette(true,0xFFF0A2C8,0xFFFFF3F9,0xFFC7AFBD,0xFF1C151F,0xFF2A202C,0xFF443044,0xFFBA85B0,0xFF4A3547,0xFF401329,0xFF754B72,0xFF382537)
   :new ThemePalette(false,0xFFA03966,0xFF392731,0xFF867681,0xFFFCF8FA,0xFFFFFDFE,0xFFF6E6EE,0xFFA987B8,0xFFEFE5EB,0xFFFFFFFF,0xFFC4A5DD,0xFFFFE7E7);
 }
}
