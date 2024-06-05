package br.com.pulse.ranked.integrations;

public interface IIntegration {

    boolean isRunning();
    boolean isPresent();
    boolean isEnabled();
    boolean enable();
    void disable();

}
