package test.samples;

import de.spricom.dessert.slicing.Classpath;
import de.spricom.dessert.slicing.Slice;
import org.junit.jupiter.api.Test;
import test.samples.mvp.base.Presenter;
import test.samples.mvp.base.ViewBase;

import static de.spricom.dessert.assertions.SliceAssertions.assertThat;

public class MvpSampleTest {
    private Classpath cp = new Classpath();
    private Slice mvp = cp.packageTreeOf(MvpSampleTest.class.getPackageName() + ".mvp");

    @Test
    public void testUsingNamingConvention() {
        Slice presenters = mvp.slice(clazz -> clazz.getName().endsWith("Presenter"));
        Slice views = mvp.slice(clazz -> clazz.getName().endsWith("ViewImpl"));
        assertThat(presenters).usesNot(views);
    }

    @Test
    public void testUsingImplementedIterfaces() {
        Slice presenters = mvp.slice(clazz -> Presenter.class.isAssignableFrom(clazz.getClassImpl()));
        Slice views = mvp.slice(clazz -> ViewBase.class.isAssignableFrom(clazz.getClassImpl()));
        assertThat(presenters).usesNot(views);
    }
}
