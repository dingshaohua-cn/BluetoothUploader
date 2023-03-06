package com.example.bluetoothuploader.ui.dashboard;

public class Connection {
    private String name;
    private String adds;

    public Connection(String a, String s) {
        name = a;
        adds = s;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdds() {
        return adds;
    }

    public void setAdds(String number) {
        this.adds = adds;
    }
}