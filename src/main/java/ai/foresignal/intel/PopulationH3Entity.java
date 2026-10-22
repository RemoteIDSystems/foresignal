package ai.foresignal.intel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * Modeled residential population aggregated to an H3 cell (prompt 101): {@code population_h3(h3,
 * res, pop, epoch)}. Imported from a GHSL GHS-POP GeoTIFF; every pixel's population is aggregated to
 * the H3 res-5 and res-7 cell containing its center. This is a model, never a casualty estimate.
 */
@Entity
@Table(name = "population_h3")
@IdClass(PopulationH3Entity.Key.class)
public class PopulationH3Entity {

    @Id
    @Column(name = "h3", nullable = false)
    private long h3;

    @Id
    @Column(name = "res", nullable = false)
    private short res;

    @Column(name = "pop", nullable = false)
    private double pop;

    @Column(name = "epoch", nullable = false)
    private short epoch;

    /** JPA. */
    protected PopulationH3Entity() {}

    /** Creates a cell's population. */
    public PopulationH3Entity(long h3, short res, double pop, short epoch) {
        this.h3 = h3;
        this.res = res;
        this.pop = pop;
        this.epoch = epoch;
    }

    public long getH3() {
        return h3;
    }

    public short getRes() {
        return res;
    }

    public double getPop() {
        return pop;
    }

    public void setPop(double pop) {
        this.pop = pop;
    }

    public short getEpoch() {
        return epoch;
    }

    /** The composite key. */
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        private long h3;
        private short res;

        /** JPA. */
        public Key() {}

        /** Creates the key. */
        public Key(long h3, short res) {
            this.h3 = h3;
            this.res = res;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            return other instanceof Key key && h3 == key.h3 && res == key.res;
        }

        @Override
        public int hashCode() {
            return Objects.hash(h3, res);
        }
    }
}
