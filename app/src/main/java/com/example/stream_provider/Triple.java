package com.example.stream_provider;

public class Triple {
    public static final String STATUS_IDLE = "idle";
    public String ip;
    public String name;
    public String status;
    public Triple(String name, String ip, String status) {
        this.ip = ip;
        this.name = name;
        this.status = status;
    }
}
