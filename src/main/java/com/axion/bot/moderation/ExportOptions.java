package com.axion.bot.moderation;

/**
 * Options for exporting moderation configurations
 */
public class ExportOptions {
    private final boolean includeMetadata;
    private final boolean includeHistory;
    private final boolean compressOutput;
    
    public ExportOptions(boolean includeMetadata, boolean includeHistory, boolean compressOutput) {
        this.includeMetadata = includeMetadata;
        this.includeHistory = includeHistory;
        this.compressOutput = compressOutput;
    }
    
    public boolean isIncludeMetadata() { return includeMetadata; }
    public boolean isIncludeHistory() { return includeHistory; }
    public boolean isCompressOutput() { return compressOutput; }
}