package de.spricom.dessert.samples;

import de.spricom.dessert.samples.mvp.base.Presenter;
import de.spricom.dessert.samples.mvp.base.ViewBase;
import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;

import static de.spricom.dessert.assertions.SliceAssertions.dessert;

public class MvpSampleTest {
    private Classpath cp = new Classpath();
    private Slice mvp = cp.packageTreeOf(MvpSampleTest.class.getPackageName() + ".mvp");

    @Test
    public void testUsingNamingConvention() {
        Slice presenters = mvp.slice(clazz -> clazz.getName().endsWith("Presenter"));
        Slice views = mvp.slice(clazz -> clazz.getName().endsWith("ViewImpl"));
        dessert(presenters).usesNot(views);
    }

    @Test
    public void testUsingImplementedIterfaces() {
        Slice presenters = mvp.slice(clazz -> Presenter.class.isAssignableFrom(clazz.getClassImpl()));
        Slice views = mvp.slice(clazz -> ViewBase.class.isAssignableFrom(clazz.getClassImpl()));
        dessert(presenters).usesNot(views);
    }
}
