package com.thoth.application.port.output;

public interface EventPublisherPort {
    void publishEvent(Object event);
    void publishEquipmentRegistered(Object event);
    void publishStatusChanged(Object event);
}
