class SplitRoughnessOpts {
    public final int n;
    public final double stddevs;
    public final boolean includeRoughness;
    public SplitRoughnessOpts(int n, double stddevs, boolean includeRoughness) {
        this.n = n;
        this.stddevs = stddevs;
        this.includeRoughness = includeRoughness;
    }
}
