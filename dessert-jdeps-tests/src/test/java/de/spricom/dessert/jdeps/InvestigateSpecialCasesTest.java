package de.spricom.dessert.jdeps;

import de.spricom.dessert.classfile.ClassFile;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.config.web.servlet.OAuth2ClientDsl;

import java.io.IOException;
import java.net.URL;

@Disabled
public class InvestigateSpecialCasesTest {

    /**
     * {@link AbstractObjectAssert} has a dependency to {@link org.assertj.core.api.Assert},
     * because it uses the {@link org.assertj.core.api.AssertFactory}. This is a method-type
     * that returns some generic extension of {@link org.assertj.core.api.Assert}.
     * Jdeps does not consider this to be a dependency.
     */
    @Test
    void testAbstractObjectAssert() throws IOException {
        dump(AbstractObjectAssert.class);
    }

    /**
     * Jdeps finds a dependency to kotlin.jvm.functions.Function1 which is not referenced otherwise within the classfile.
     * The javap output shows:
     * <pre>
     *   #10 = Utf8               bean
     *   #11 = Utf8               (Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Scope;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Role;Lkotlin/jvm/functions/Function1;)V
     *   #12 = NameAndType        #10:#11        // bean:(Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Scope;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Role;Lkotlin/jvm/functions/Function1;)V
     *   #65 = Utf8               (Lkotlin/jvm/functions/Function0;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Scope;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Role;)V
     *   #66 = NameAndType        #10:#65        // bean:(Lkotlin/jvm/functions/Function0;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Scope;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/springframework/context/support/BeanDefinitionDsl$Role;)V
     * </pre>
     * Hence <i>bean</i> is defined twice. The constant pool entry <i>#12</i> is never used.
     */
    @Test
    void testBeanDefinitionDsl() throws IOException {
        dump(BeanDefinitionDsl.class, "$bean$$inlined$bean$1");
    }

    /**
     * The {@link OAuth2ClientDsl} contains a RuntimeVisibleAnnotations attribute of kotlin.Metadata for which the
     * d2 attribute contains an array of strings. Some of these strings contain field-type or method-type descriptors.
     * Jdeps treats the content of these descriptors as dependencies.
     */
    @Test
    void testOAuth2ClientDsl() throws IOException {
        dump(OAuth2ClientDsl.class);
    }

    private ClassFile dump(Class<?> clazz) throws IOException {
        return dump(clazz, "");
    }

    private ClassFile dump(Class<?> clazz, String innerClassSuffix) throws IOException {
        String resource = clazz.getSimpleName() + innerClassSuffix + ".class";
        URL url = clazz.getResource(resource);
        System.out.println(url);
        ClassFile cf = new ClassFile(clazz.getResourceAsStream(resource));
        System.out.println(cf.dump());
        return cf;
    }
}
