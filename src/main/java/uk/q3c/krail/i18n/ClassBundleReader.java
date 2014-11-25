package uk.q3c.krail.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Reads a {@link ResourceBundle} from a Java class.  The result (if it exists) is expected to be an {@link
 * EnumResourceBundle}.
 * <p/>
 * The code for this was lifted directly from {@link ResourceBundle.Control} newBundle and toBundleName methods
 * <p/>
 * Created by David Sowerby on 18/11/14.
 */
public class ClassBundleReader implements BundleReader {
    @Override
    public EnumResourceBundle newBundle(String baseName, Locale locale, ClassLoader loader, boolean reload) {
        String bundleName = this.toBundleName(baseName, locale);
        try {
            Class resourceName = loader.loadClass(bundleName);
            if (!EnumResourceBundle.class.isAssignableFrom(resourceName)) {
                throw new ClassCastException(resourceName.getName() + " cannot be cast to EnumResourceBundle");
            }

            EnumResourceBundle bundle = (EnumResourceBundle) resourceName.newInstance();
            return bundle;

        } catch (Exception e) {
            return null;
        }
    }

    public String toBundleName(String baseName, Locale locale) {
        if (locale == Locale.ROOT) {
            return baseName;
        } else {
            String language = locale.getLanguage();
            String script = locale.getScript();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            if (language == "" && country == "" && variant == "") {
                return baseName;
            } else {
                StringBuilder sb = new StringBuilder(baseName);
                sb.append('_');
                if (script != "") {
                    if (variant != "") {
                        sb.append(language)
                          .append('_')
                          .append(script)
                          .append('_')
                          .append(country)
                          .append('_')
                          .append(variant);
                    } else if (country != "") {
                        sb.append(language)
                          .append('_')
                          .append(script)
                          .append('_')
                          .append(country);
                    } else {
                        sb.append(language)
                          .append('_')
                          .append(script);
                    }
                } else if (variant != "") {
                    sb.append(language)
                      .append('_')
                      .append(country)
                      .append('_')
                      .append(variant);
                } else if (country != "") {
                    sb.append(language)
                      .append('_')
                      .append(country);
                } else {
                    sb.append(language);
                }

                return sb.toString();
            }
        }
    }
}