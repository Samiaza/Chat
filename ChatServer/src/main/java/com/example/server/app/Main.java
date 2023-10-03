package com.example.server.app;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.example.server.core.ServerCore;

public class Main {
    public static void main(String[] args) {
        try {
            MainArgs jArgs = new MainArgs();
            JCommander mainCmd = JCommander.newBuilder().addObject(jArgs).build();
            mainCmd.parse(args);

            ServerCore serverCore = new ServerCore(jArgs.port);
            serverCore.start();

        } catch (Exception ex) {
            System.out.println("Server Ruined");
        }
    }
}

@Parameters(separators = "=")
class MainArgs {
    @Parameter(
            names = "--port",
            description = "Server's port",
            arity = 1,
            required = true
    )
    int port;
}