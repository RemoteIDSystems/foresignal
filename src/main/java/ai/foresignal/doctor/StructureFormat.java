package ai.foresignal.doctor;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/** The format a body was recognized as (OpenAPI {@code StructureDiff.format}). */
public enum StructureFormat {
    /** A JSON document. */
    JSON,
    /** Comma-, semicolon- or pipe-separated values. */
    CSV,
    /** Tab-separated values. */
    TSV,
    /** An XML document. */
    XML,
    /** A binary file (GRIB2, NetCDF, PNG, ZIP, gzip …). */
    BINARY,
    /** An HTML page. */
    HTML,
    /** Text that is none of the above. */
    TEXT;

    /** The wire value, lowercase. */
    @JsonValue
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** True for the formats that carry data rows or a data tree (everything but HTML and plain text). */
    public boolean isData() {
        return this != HTML && this != TEXT;
    }
}
