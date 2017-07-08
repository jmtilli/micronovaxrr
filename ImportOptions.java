/* Used by ImportDialog */


public class ImportOptions {
    public final int modulo;
    public final double minAngle, maxAngle, minNormal, maxNormal;
    public final int meascol;
    public ImportOptions(int modulo, double minAngle, double maxAngle,
                         double minNormal, double maxNormal,
                         int meascol) {
        this.modulo = modulo;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        this.meascol = meascol;
    }
}
