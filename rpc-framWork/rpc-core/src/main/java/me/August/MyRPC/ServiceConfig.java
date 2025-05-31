package me.August.MyRPC;

public class ServiceConfig<T>{
    private Class<?> interfaceProvider;
    private Object ref;
    public ServiceConfig(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public ServiceConfig() {
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public void setRef(Object ref) {this.ref = ref;}
    public Object getRef() {
        return ref;
    }

}
