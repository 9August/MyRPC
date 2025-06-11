package me.August.MyRPC;

public class ServiceConfig<T>{
    private String group = "default";
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

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public void setRef(Object ref) {this.ref = ref;}
    public Object getRef() {
        return ref;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
