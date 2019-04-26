package warehousemanagement.navigation;

import warehousemanagement.Controller;
import warehousemanagement.Shipment;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Erweitert die {@link StorageNode} um die Moeglichkeit Spezifikationen aus einem {@link warehousemanagement.Shipment} zu laden
 * und diese an das System aktiv abzugeben/anzufragen.
 */
public class DeliveryNode extends StorageNode {

    /**
     * Gibt an ob die Node gerade be- oder entladen wird um es Robotern zu ermoeglichen die richtige Entscheidung zu treffen
     */
    private boolean loading;

    private ArrayList<Robot> robots;

    public DeliveryNode(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height);

        robots = new ArrayList<>();
    }

    /**
     * Gibt zurück ob diese DeliveryNode gerade Waren einlaedt.
     *
     * @return ein booleanscher wert der angibt ob die DeliveryNode Waren einlaedt
     */
    boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    /**
     * Gibt zurück ob diese DeliveryNode gerade Waren auslaedt.
     *
     * @return ein booleanscher wert der angibt ob die DeliveryNode Waren auslaedt
     */
    boolean isUnloading() {
        return !loading;
    }

    @Override
    boolean accessNode(Robot robot) {
        if (getMaterialType() == 0) {
            robotQueue.add(robot);
            return false;
        }
        return super.accessNode(robot);
    }

    /**
     * Kontrolliert ob eingeladen werden darf und ruft dann {@link StorageNode#loadItems} auf, um es
     * Robotern zu ermoeglichen Waren an DeliveryNodes auszuladen.
     * <br>
     * {@inheritDoc}
     *
     * @param materialType {@inheritDoc}
     * @param amount       {@inheritDoc}
     * @throws RuntimeException wenn {@link DeliveryNode#isLoading()} }{@code == false}
     */
    @Override
    public void loadItems(int materialType, int amount) {
        if (isLoading()) {
            super.loadItems(materialType, amount);
            if (getAmount() == getStorageSize()) {
                requestNextShipment();
            }
        } else {
            throw new RuntimeException("Kann keine Items einladen wenn ausgeladen werden soll");
        }
    }

    /**
     * Kontrolliert ob ausgeladen werden darf und ruft dann {@link StorageNode#loadItems} auf, um es
     * Robotern zu ermöglichen Waren an DeliveryNodes einzuladen.
     * <br>
     * {@inheritDoc}
     *
     * @param amount {@inheritDoc}
     * @throws RuntimeException wenn {@link DeliveryNode#isUnloading()} {@code == false}
     */
    @Override
    public void unloadItems(int amount) {
        if (getMaterialType() == 0) {
            throw new RuntimeException("Keine Waren an dieser DeliveryNode");
        }
        if (isUnloading()) {
            super.unloadItems(amount);
        } else {
            throw new RuntimeException("Kann keine Items ausladen wenn eingeladen werden soll");
        }
        if (getAmount() == 0) {
            //TODO auf die Waren setzen die danach angefragt werden müssen
            resetMaterialType();
        }
    }

    /**
     * Wenn ein {@link warehousemanagement.Shipment} abgefertigt wurde laedt diese Methode das naechste {@link warehousemanagement.Shipment} beziehungsweise
     * reiht sich in eine Warteschlange ein
     */
    private void requestNextShipment() {
        Shipment next = Controller.getController().requestNextShipment(this);
        if (next != null) {
            setMaterialType(next.getMaterialType());
            loading = true;
            loadItems(next.getMaterialType(), next.getAmount());
            loading = false;
        }
    }

    public void loadShipment(Shipment s) {
        setMaterialType(s.getMaterialType());
        loading = true;
        loadItems(s.getMaterialType(), s.getAmount());
        loading = false;

        for (int i = 0; i < robotQueue.size(); i++) {
            robotQueue.get(i).notify();
        }
    }

    /**
     * Aendert den Modus von abladen in einladen, damit zugeordnete {@link Robot}er wissen, dass sie zukuenftig Waren anliefern muessen
     */
    private void requestItems() {
        //TODO implementieren
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int width = getWidth();
        int height = getHeight();

        if (0.4 * height < y && y < 0.6 * width) {
            if (0.2 * width < x && x < 0.4 * width) {
                robots.get(robots.size() - 1).shutdown();
                robots.remove(robots.size() - 1);
            } else if (0.6 * width < x && x < 0.8 * width) {
                robots.add(new Robot(this, this));
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, (int) (0.2 * height));
        g.fillRect(0, (int) (0.8 * height), width, (int) (0.2 * height));

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect((int) (0.1 * width), (int) (0.2 * height) - 1, (int) (0.8 * width), (int) (0.6 * height) + 1);

        g.setColor(Color.BLACK);
        g.drawRect((int) (0.1 * width), (int) (0.2 * height) - 1, (int) (0.8 * width), (int) (0.6 * height) + 1);

        g.setColor(Color.WHITE);
        g.fillRect((int) (0.2 * width), (int) (0.4 * height), (int) (0.2 * width), (int) (0.2 * height));
        g.fillRect((int) (0.6 * width), (int) (0.4 * height), (int) (0.2 * width), (int) (0.2 * height));

        g.setColor(Color.BLACK);
        g.drawRect((int) (0.2 * width), (int) (0.4 * height), (int) (0.2 * width), (int) (0.2 * height));
        g.drawRect((int) (0.6 * width), (int) (0.4 * height), (int) (0.2 * width), (int) (0.2 * height));

        g.setFont(new Font("Arial", Font.PLAIN, (int) (0.15 * Math.min(width, height))));

        g.drawString("-", (int) (0.29 * width), (int) (0.55 * height));
        g.drawString("+", (int) (0.66 * width), (int) (0.56 * height));

        g.drawString(Integer.toString(robots.size()), (int) (0.46 * width), (int) (0.56 * height));

        if (getMaterialType() != 0) {
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, 10, 10);
        }
    }
}