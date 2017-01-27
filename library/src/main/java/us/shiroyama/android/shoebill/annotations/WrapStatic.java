package us.shiroyama.android.shoebill.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes to whose static methods Shoebill generates wrapper methods.
 *
 * @author Fumihiko Shiroyama
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface WrapStatic {
}
