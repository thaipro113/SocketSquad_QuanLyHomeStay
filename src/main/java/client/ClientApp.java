package client;

import client.controller.ClientController;
import client.view.LoginView;

import javax.swing.*;

public class ClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientController controller = new ClientController();
            new LoginView(controller).setVisible(true);
        });
    }
}
