package us.shiroyama.android.shoebill;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockingStaticSample {
    @Before
    public void setUp() throws Exception {
        HelperWrapper helperWrapper = mock(HelperWrapper.class);
        when(helperWrapper.isConnectedWifi(any(Context.class))).thenReturn(true);
        when(helperWrapper.isConnected3G(any(Context.class))).thenReturn(false);
        HelperWrapper.setInstance(helperWrapper);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void mockStatic() throws Exception {
        assertThat(HelperWrapper.getInstance().isConnectedWifi(null), is(true));
        assertThat(HelperWrapper.getInstance().isConnected3G(null), is(false));
    }
}