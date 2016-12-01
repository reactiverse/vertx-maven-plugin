package io.fabric8.vertx.maven.plugin.model;

/**
 * @author kameshs
 */
public class Relocator {

    private String serviceInterface;
    private RelocatorMode mode;

    public String getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public RelocatorMode getMode() {
        return mode;
    }

    public void setMode(RelocatorMode mode) {
        this.mode = mode;
    }

}
