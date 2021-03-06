package warehousemanagement.navigation;

import warehousemanagement.DataConnection;
import warehousemanagement.gui.StorageNodeConfiguration;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * Erweitert die {@link Node} um die Möglichkeit Waren zu lagern. Roboter können an diesen Nodes Waren
 * abholen oder abgeben
 */
public class StorageNode extends Node implements MouseListener {

    /**
     * Hier werden Roboter gespeichert wenn sie warten müssen bis die Node freigegeben wird.
     */
    final ArrayList<Robot> robotQueue;
    /**
     * Repräsentiert den Materialtyp der in dieser StorageNode gelagert wird.
     *
     * @see DataConnection#getMaterialType
     */
    private int materialType;
    /**
     * Gibt an wieviel Waren aktuell im Lager sind.
     */
    int amount;
    /**
     * Gibt an wieviele Einheiten in das Lager reinpassen.
     */
    private final int storageSize;
    /**
     * Gibt an ob die StorageNode gerade von einem Roboter blockiert wird.
     */
    private boolean blocked;

    public StorageNode(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height);
        materialType = 0;
        storageSize = 100;
        amount = 0;
        blocked = false;
        robotQueue = new ArrayList<>();

        addMouseListener(this);
    }

    /**
     * Gibt einen Integer Wert zurück der die Materialart dieser Node repräsentiert
     *
     * @return int der die Materialart repräsentiert
     * @see DataConnection#getMaterialType
     */
    public int getMaterialType() {
        return materialType;
    }

    /**
     * Überschreibt den {@link StorageNode#materialType}, dieser kann aber nur erfolgreich überschrieben
     * werden wenn das Lager leer ist.
     *
     * @param materialType der neue {@code materialType}
     * @throws RuntimeException wenn ein ungültiger Materialtyp mitgegeben wird
     */
    public void setMaterialType(int materialType) {
        if (DataConnection.getDataConnection().isValidMaterialType(materialType)) {
            if (amount == 0) {
                this.materialType = materialType;
            } else {
                throw new RuntimeException("Kann materialType nicht ändern wenn noch Waren im Lager sind");
            }
        } else {
            throw new RuntimeException("Kann Storage Einheit nicht auf ungültigen materialType setzen");
        }
    }

    /**
     * Gibt zurück wie viel Waren aktuell im Lager sind
     *
     * @return Menge der Waren im Lager
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Gibt zurück wie groß das Warenlager ist
     *
     * @return Größe des Lagers
     */
    public int getStorageSize() {
        return storageSize;
    }

    /**
     * Setzt den Materialtyp zurück auf null, nur möglich wenn das Warenlager leer ist
     */
    public void resetMaterialType() {
        if (getAmount() == 0) {
            this.materialType = 0;
        } else {
            throw new RuntimeException("Kann Materialtype von StorageNode nicht zurücksetzen wenn Waren im Lager sind");
        }
    }

    boolean accessNode(Robot robot) {
        if (blocked) {
            robotQueue.add(robot);
            return false;
        } else {
            blocked = true;
            return true;
        }
    }

    void leaveNode() {
        blocked = false;
        if (robotQueue.size() != 0) {
            synchronized (robotQueue.get(0)) {
                robotQueue.get(0).notify();
            }
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
            new StorageNodeConfiguration(this, e.getXOnScreen(), e.getYOnScreen());
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

        if (robots > 0) {
            g.setColor(Color.BLACK);
            g.fillRect((int) (0.5 * width) - 10, (int) (0.15 * height), 20, 20);
        }
    }
}
