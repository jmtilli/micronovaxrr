class DFTOptions {
    double minAngle, maxAngle;
    double minThickness, maxThickness;
    boolean useSimul;
    boolean useWindow;
    int multiplier;
    public DFTOptions(double minAngle, double maxAngle, double minThickness, double maxThickness, boolean useSimul, boolean useWindow, int multiplier) {
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.minThickness = minThickness;
        this.maxThickness = maxThickness;
        this.useSimul = useSimul;
        this.useWindow = useWindow;
        this.multiplier = multiplier;
    }
}
