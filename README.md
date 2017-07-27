# @Maqueta

_Live_-models backed up by SharedPreferences and observable with Rx, LiveData or old-fashioned listeners

[maven]

This library is an abstraction other Android SharedPreferences, which allows easily _share_ arbitrary data between different application layers without exposing Android specifics. For example given the **decriptor** class:

```java
@Maqueta(className = ".First")
class FirstMaqueta {
    long id;
    String name;
    boolean flag;
}
```

**Maqueta** library will generate a normal POJO with getters & setters which directly coordinate with underying SharedPreferences:

```java
final First first = First.create(context, "name_of_pref");

// getters
first.id();
first.name();
first.flag();

// setters (implemented with Builder pattern for easy chaining)
first.id(23L)
    .name("my_name")
    .flag(true);

// clear
first.clear();
```


### ObserveWith

Additionally **Maqueta** allows to specify what type of observable a generated class must be. There are few options:

* RxJava2
* LiveData
* Old-fashioned listeners

_*Please note, that Maqueta is not compiled against RxJava2 or LiveData, so make sure you have desired library in your classpath_

#### RxJava2
[RxJava](https://github.com/ReactiveX/RxJava)
```java
@Maqueta(className = ".Rx", observeWith = Maqueta.ObserveWith.RX)
class RxMaqueta {
// fields are omitted for brevity
}

// generated class will have the `observe` method
final Rx rx = Rx.create(context, "rx_pref");

final Observable<Rx> observable = rx.observe();
final Disposable disposable = observable
    .subscribe(rx -> { /* will be called when any of keys have been updated */ });

// do not forget to dispose your disposables after you are finished
// for example, in `onStop()`
disposable.dispose();
```

#### LiveData

Please note that [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html) is still in alpha, use with caution

```java
@Maqueta(className = ".Live", observeWith = Maqueta.ObserveWith.LIVE_DATA)
class LiveMaqueta {
// fields are omitted for brevity
}

// generated class will extend `LiveData<Live>`
final Live live = Live.create(context, "live_pref");

live.observe(lifecycleOwner, (live) -> {
    // Maqueta will never deliver `null` values
    // so it's safe to suppress nullable problems here (Observer's method is annotated with @Nullable)
})
```

#### Old-fashioned listeners
```java
@Maqueta(className = ".Fashion", observeWith = Maqueta.ObserveWith.LISTENER)
class FashionMaqueta {
// fields are omitted for brevity
}

final Fashion fashion = Fashion.create(context, "fashion_pref");

// new MaquetaListener<Fashion>
fashion.register(fashion -> {});

fashion.unregister(listener);

fashion.unregisterAll();
```


## Configuration

**@Maqueta** annotation requires at least `className` option. It's the name of a generated class. It can be fully-qualified Java class name (`com.example.MyClass`) or start with a dot (`.MyClass`) to create a `MyClass` class in the same package as a descriptor class.

Descriptor class (that is annotated with **@Maqueta**) is used only to provide description of a class to be generated. It's not used further and is safe to delete. There are also no restrictions for a descriptor - it can be any valid class (including private inner classes, this can be helpful if you want to keep all descriptors in one place).


### @Maqueta.Key

Use this annotation for the fields in descriptor class to provide key specific logic.

```java
@Maqueta(className = ".Keys")
class KeysMaqueta {

    @Maqueta.Key(name = "this_is_key_name_in_shared_preferences")
    long id;

    @Maqueta.Key(defaultValue = "true")
    boolean flag;

    @Maqueta.Key(serializedType = Maqueta.Type.LONG)
    Date date;
}
```

#### name

This option describes what key name will be used to put/retrieve values to/from SharedPreferences for this field. Please note, that this option does not affect the generated getters/setters for a field (field's name is still used for them).


#### defaultValue

This option lets you specify default value for the specified key. By default **@Maqueta** uses these defaults:

* boolean -> **false**
* int -> **0**
* long -> **0L**
* float -> **.0F**
* String -> **\"\"** (empty string)
* Set&lt;String&gt; - > **Collections.EMPTY_SET**

Please note, that defaultValue for a String must be quoted (`@Maqueta.Key(defaultValue = "\"raw_string\"")`)

This option does not enforce one specific usage. So, everything that is provided will be directly moved to a generated class. This lets to use any value as a default one, even if available only at runtime.

```java
@Maqueta.Key(defaultValue = "BuildConfig.APPLICATION_ID")
String appId;

@Maqueta.Key(defaultValue = "System.currentTimeMillis()")
long time;
```

#### serializedType

**@Maqueta** allows storing/retrieving any object in/from the SharedPreferences with the help of serializers. But in order to ensure type safety **serializedType** must be specified. It can be any of the natively supported type by SharedPreferences:

* BOOLEAN
* INT
* LONG
* FLOAT
* STRING
* SET_STRING

```java
@Maqueta(className = ".LongDate")
class LongDateMaqueta {

    @Maqueta.Key(serializedType = Maqueta.Type.LONG)
    Date date;

}
```

If **@Maqueta** encounters a type that is not natively supported, instead of generating static `create` method a `builder` will be generated.

```java
final LongDate longDate = LongDate.builder(context, "long_date_pref")
        .dateSerializer(new MaquetaSerializer<Date, Long>() {
            @Override
            public Long serialize(Date date) {
                return date.getTime();
            }

            @Override
            public Date deserialize(Long aLong) {
                return new Date(aLong);
            }
        })
        .build();
```

Please note that all serializable fields must have serializers provided. If not `Builder` will throw an exception indicating what required argument is missing.


### @Maqueta.Field

In order to include a field (that is not backed-up in SharedPreferences) in a generated class the **@Maqueta.Field** annotation can be used. It has only one option `getter` which describes if a getter for this field must be generated.

It can be helpful for example to use in default values.

```java
@Maqueta(className = ".Fields")
class FieldsMaqueta {

    @Maqueta.Field
    float ratio;

    @Maqueta.Key(defaultValue = "13.F * ratio"
    float value;
}
```

Please note, that as fields are not backed-up by SharedPreferences they are instance specific.

Also, if a **@Maqueta.Field** is present in descriptor a `builder` static method will be generated instead of `create`.

```java
final Fields fields = Fields.builder(context, "fields_pref")
    .ratio(5.F)
    .build();
```

## License

```
  Copyright 2017 Dimitry Ivanov (mail@dimitryivanov.ru)

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
