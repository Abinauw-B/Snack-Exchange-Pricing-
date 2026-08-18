package com.retailpos.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    private String description;

    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public static SystemConfigBuilder builder() { return new SystemConfigBuilder(); }

    public static class SystemConfigBuilder {
        private String configKey;
        private String configValue;
        private String description;

        public SystemConfigBuilder configKey(String configKey) { this.configKey = configKey; return this; }
        public SystemConfigBuilder configValue(String configValue) { this.configValue = configValue; return this; }
        public SystemConfigBuilder description(String description) { this.description = description; return this; }

        public SystemConfig build() {
            return new SystemConfig(configKey, configValue, description);
        }
    }
}
