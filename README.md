# Shoebill

Static methods are always a pain in the neck when you want to mock them in Unit Testing.

Shoebill offers you a clean way to generate a simple wrapper class for the one that has a bunch of static methods.

## How to Use

Just annotate your class with static methods with `@WrapStatic` annotation.

```java
@WrapStatic
public class Helper {
    public static boolean isConnectedWifi(@NonNull Context context) { }

    public static boolean isConnected3G(@NonNull Context context) { }
}
```

Shoebill generates a class `HelperWrapper` that has all the proxy instance methods corresponding to the static methods with the same signatures except `static` modifier.

```java
// generated code
public class HelperWrapper {
  private static HelperWrapper singleton = new HelperWrapper();

  public static HelperWrapper getInstance() {
    return singleton;
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  public static void setInstance(HelperWrapper wrapper) {
    singleton = wrapper;
  }

  public boolean isConnectedWifi(@NonNull Context context) {
    return Helper.isConnectedWifi(context);
  }

  public boolean isConnected3G(@NonNull Context context) {
    return Helper.isConnected3G(context);
  }
}
```

You can just use the wrapper methods instead of using the static methods directly.

```java
// boolean wifiStatus = Helper.isConnectedWifi(getApplicationContext());
boolean wifiStatus = HelperWrapper.getInstance().isConnectedWifi(getApplicationContext());
```

## How to mock

As you may notice that `HelperWrapper` has `setInstance(HelperWrapper wrapper)`, you can set mock instance when unit testing.

```java
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
```

This setter method is properly annotated with `@VisibleForTesting(otherwise = VisibleForTesting.NONE)` that your lint tool warns you if you try to use this method in production code as of Android Studio 2.3 or later.

![VisibleForTesting](https://raw.githubusercontent.com/srym/shoebill/master/images/visible_for_testing.png)

## Installation

```
annotationProcessor 'us.shiroyama.android:shoebill-processor:0.1.1'
provided 'us.shiroyama.android:shoebill:0.1.1'
```

## License

```
Copyright 2017 Fumihiko Shiroyama

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
