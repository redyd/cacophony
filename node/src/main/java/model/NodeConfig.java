package model;

import java.util.Map;

public class NodeConfig {

    private String Domain ;
//le string c'est le nom de lutilisateur
    private Map<String,User> properties;

    public NodeConfig(String Domain, Map<String,User> properties) {
        this.Domain = Domain;
        this.properties = properties;
    }

}
