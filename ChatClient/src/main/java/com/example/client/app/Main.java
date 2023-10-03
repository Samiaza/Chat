package com.example.client.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.example.client.core.ClientCore;

public class Main {
    public static void main(String[] args) {
        try {
            MainArgs jArgs = new MainArgs();
            JCommander mainCmd = JCommander.newBuilder().addObject(jArgs).build();
            mainCmd.parse(args);

            ClientCore clientCore = new ClientCore(jArgs.port);
            clientCore.start();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

@Parameters(separators = "=")
class MainArgs {
    @Parameter(
            names = "--server-port",
            description = "Server's port",
            arity = 1,
            required = true
    )
    int port;
}