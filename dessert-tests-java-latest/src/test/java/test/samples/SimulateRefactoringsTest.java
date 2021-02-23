package test.samples;

import de.spricom.dessert.groups.SliceGroup;
import de.spricom.dessert.slicing.ConcreteSlice;
import de.spricom.dessert.slicing.Slice;
import de.spricom.dessert.slicing.SliceContext;
import org.junit.Test;

public class SimulateRefactoringsTest {

    @Test
    public void test() {
        SliceContext sc = new SliceContext();

        // define building blocks
        Slice slicing = sc.packageTreeOf(Slice.class);
        Slice groups = sc.packageTreeOf(SliceGroup.class);

        // simulate moving one class to an other building block
        Slice packageSlice = sc.sliceOf(ConcreteSlice.class);
        slicing = slicing.without(packageSlice);
        groups = groups.with(packageSlice);

        // dessert(slicing).doesNotUse(groups);
        // dessert(groups).doesNotUse(slicing);

        // check for cycles
//        List<Slice> group = Arrays.asList(slicing, groups);
//        dessert(group).isCycleFree();
    }
}
