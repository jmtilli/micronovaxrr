class SplitRoughnessException extends Exception {
    String s;
    public SplitRoughnessException(String s) {
        super(s);
        this.s = s;
    }
    public String toString() {
        return s;
    }
};
