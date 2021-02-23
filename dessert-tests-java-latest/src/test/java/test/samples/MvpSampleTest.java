package test.samples;

import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;
import test.samples.mvp.base.Presenter;
import test.samples.mvp.base.ViewBase;

import static de.spricom.dessert.assertions.SliceAssertions.assertThat;

public class MvpSampleTest {
    private SliceContext sc = new SliceContext();
    private Slice mvp = sc.packageTreeOf(MvpSampleTest.class.getPackageName() + ".mvp");

    @Test
    public void testUsingNamingConvention() {
        Slice presenters = mvp.slice(se -> se.getClassName().endsWith("Presenter"));
        Slice views = mvp.slice(se -> se.getClassName().endsWith("ViewImpl"));
        assertThat(presenters).doesNotUse(views);
    }

    @Test
    public void testUsingImplementedIterfaces() {
        Slice presenters = mvp.slice(se -> Presenter.class.isAssignableFrom(se.getClazz()));
        Slice views = mvp.slice(se -> ViewBase.class.isAssignableFrom(se.getClazz()));
        assertThat(presenters).doesNotUse(views);
    }
}
