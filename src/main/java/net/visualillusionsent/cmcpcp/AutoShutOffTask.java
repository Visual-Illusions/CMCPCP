package net.visualillusionsent.cmcpcp;

/**
 * @author Jason Jones (darkdiplomat)
 */
public class AutoShutOffTask implements Runnable {

    private final CoffeePotController controller;

    AutoShutOffTask(CoffeePotController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        if (controller.reportPower()) {
            controller.togglePower();
        }
    }
}
