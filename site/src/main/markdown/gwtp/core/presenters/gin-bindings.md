# GIN Bindings

GWTP uses dependency injection as a foundation of its architecture. It allows to have a maintainable application and loosely coupled components.

GIN is the library used for dependency injection in GWT.

## References
[Google GIN](http://code.google.com/p/google-gin/)

## Binding MVP
GWTP needs a boostrapping process to link a Presenter class to its View and View implementation classes. The way of doing this is by creating a GIN module and put the bindings in there.

Here's an example of a basic module:

```java
public class ApplicationModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new HomeModule());
        install(new WidgetModule());

        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, ApplicationView.class,
                      ApplicationPresenter.MyProxy.class);
    }
}
```

Things to notice in this example:

1. A GWTP module should extend `AbstractPresenterModule` to inherit convenience methods for binding a different Presenter types. (See [the Javadoc](http://arcbees.github.io/GWTP/javadoc/apidocs/) for more information.)
2. A module can install other modules
3. A module needs to be installed (by another installed module) to have its code executed. (See [GIN module registration]({{#gwtp.doc.url.gwtp.doc.url.bootstrapper}}))
