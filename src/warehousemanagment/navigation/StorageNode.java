package warehousemanagment.navigation;

import warehousemanagment.DataConnection;
import warehousemanagment.gui.StorageNodeConfiguration;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Erweitert die {@link Node} um die Moeglichkeit Waren zu lagern. Roboter können an diesen Nodes Waren
 * abholen oder abgeben
 */
public class StorageNode extends Node implements MouseListener {

    private int materialType;
    private int amount;
    private int storageSize;

    private ArrayList<warehousemanagment.navigation.Robot> robotQueue;

    private boolean blocked;

    public StorageNode(int id) {
        super(id);
        materialType = 0;
        storageSize = 100;
        amount = 0;
        blocked = false;
        robotQueue = new ArrayList<>();
    }

    public StorageNode(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height);
        materialType = 0;
        storageSize = 100;
        amount = 0;
        blocked = false;
        robotQueue = new ArrayList<>();

        addMouseListener(this);
    }

    public int getMaterialType() {
        return materialType;
    }

    public int getAmount() {
        return amount;
    }

    public int getStorageSize() {
        return storageSize;
    }

    public boolean accessNode(Robot robot) {
        if (blocked) {
            robotQueue.add(robot);
            return false;
        } else {
            blocked = true;
            return true;
        }
    }

    public void leaveNode() {
        blocked = false;
        if (robotQueue.size() != 0) {
            robotQueue.get(0).notify();
        }
    }

    /**
     * Ueberschreibt den {@link StorageNode#materialType}, dieser kann aber nur erfolgreich ueberschrieben
     * werden wenn das Lager leer ist
     *
     * @param materialType der neue {@code materialType}
     * @return ob das aendern des {@code materialType} erfolgreich war
     * @throws RuntimeException Wenn ein ungültiger Materialtyp mitgegeben wird
     */
    public boolean setMaterialType(int materialType) {
        if (DataConnection.getDataConnection().isValidMaterialType(materialType)) {
            if (amount == 0) {
                this.materialType = materialType;
                return true;
            } else {
                return false;
            }
        } else {
            throw new RuntimeException("Kann Storage Einheit nicht auf ungültigen materialType setzen");
        }
    }

    public void resetMaterialType() {
        if (getAmount() == 0) {
            this.materialType = 0;
        } else {
            throw new RuntimeException("Kann Materialtype von StorageNode nicht zurücksetzen wenn Waren im Lager sind");
        }
    }

    /**
     * Kontrolliert ob die Lagereinheit den richtigen Materialtyp hat, und lädt dann die angegebene Menge ein
     *
     * @param materialType Type der Ware die eingelagert werden soll
     * @param amount       Menge der Ware die eingeladen werden soll
     * @throws RuntimeException Wenn die Lagereinheit nicht den richtigen Materialtyp hat
     */
    public void loadItems(int materialType, int amount) {
        if (materialType != getMaterialType()) {
            throw new RuntimeException("Falscher Materialtyp");
        }
        if (this.amount + amount <= storageSize) {
            this.amount += amount;
        }
    }

    /**
     * Kontrolliert ob genug Material im Lager ist um die angefragte Menge auszuladen
     *
     * @param amount Menge die ausgeladen werden soll
     * @throws RuntimeException Wenn im Lager weniger Materialien sind als der Roboter benötigt
     */
    public void unloadItems(int amount) {
        if (this.amount >= amount) {
            this.amount -= amount;
        } else {
            throw new RuntimeException("Lager hat nicht genug Material");
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getY() > getHeight() / 2) {
            new StorageNodeConfiguration(this);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        int gap = (int) (0.05 * height);

        g.setColor(Color.BLACK);
        g.fillRect(0, (int) (0.5 * height), width, (int) (0.5 * height));
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(gap, (int) (0.5 * height) + gap, width - 2 * gap, (int) (0.5 * height) - 2 * gap);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, (int) (0.3 * height)));
        g.drawString(DataConnection.getDataConnection().getMaterialType(getMaterialType()), 2 * gap, height - (int) (2.5 * gap));

        if (blocked) {
            g.setColor(Color.BLACK);
            g.fillRect((int) (0.45 * width), (int) (0.15 * height), (int) (0.1 * width), (int) (0.2 * height));
        }
    }
}
