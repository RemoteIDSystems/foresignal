package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.raster.model.EcmwfRunIngest;
import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.source.adapters.ecmwf.AifsAdapter;
import ai.foresignal.source.adapters.gfswave.GfsWaveAdapter;
import ai.foresignal.source.adapters.goes.GoesAbiAdapter;
import ai.foresignal.source.adapters.goes.GoesWestAbiAdapter;
import ai.foresignal.source.adapters.heatrisk.HeatRiskAdapter;
import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.adapters.nbm.NbmAdapter;
import ai.foresignal.source.adapters.nexrad.NexradL3Adapter;
import ai.foresignal.source.adapters.rtma.RtmaAdapter;
import ai.foresignal.source.adapters.sst.SstAdapter;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Per-product enablement and the profile defaults (prompt 106 task 1), read from the real adapters'
 * declarations:
 * <ul>
 *   <li>small: C08, C09, RTMA-RU, NBM, OVATION, TAF, HRRR smoke, HeatRisk and UV on (plus the v1 C13);
 *   <li>large: also C02, GOES-18 C13, GFS-Wave, MRMS extras, SST and AIFS;
 *   <li>mesoscale sectors and NEXRAD L3 opt-in on both.
 * </ul>
 * The {@code products} param has no descriptor default, because the parameter validator applies a
 * default as if the operator had set it, which would hide the profile.
 */
class ProductProfileDefaultsTest {

    /** One adapter's enablement declaration. */
    record Declared(String label, Collection<String> all, Collection<String> light, Collection<String> large) {

        Set<String> on(String profile) {
            return ProductEnablement.enabled(null, profile, all, light, large);
        }
    }

    static List<String> goesEastAll() {
        return List.of(GoesAbiAdapter.IR, GoesAbiAdapter.WV8, GoesAbiAdapter.WV9, GoesAbiAdapter.VIS,
                GoesAbiAdapter.MESO1, GoesAbiAdapter.MESO2);
    }

    @Test
    @DisplayName("GOES-East: C13/C08/C09 on small; C02 added on large; mesoscale sectors opt-in on both")
    void goesEast() {
        Declared d = new Declared("goes-east-abi", goesEastAll(), GoesAbiAdapter.LIGHT_DEFAULT,
                GoesAbiAdapter.LARGE_DEFAULT);
        assertThat(d.on("small")).containsExactlyInAnyOrder(GoesAbiAdapter.IR, GoesAbiAdapter.WV8, GoesAbiAdapter.WV9);
        assertThat(d.on("large")).containsExactlyInAnyOrder(GoesAbiAdapter.IR, GoesAbiAdapter.WV8, GoesAbiAdapter.WV9,
                GoesAbiAdapter.VIS);
        assertThat(d.on("large")).doesNotContain(GoesAbiAdapter.MESO1, GoesAbiAdapter.MESO2);
    }

    @Test
    @DisplayName("GOES-West C13: off on small, on on large")
    void goesWest() {
        Declared d = new Declared("goes-west-abi", List.of(GoesWestAbiAdapter.IR), GoesWestAbiAdapter.LIGHT_DEFAULT,
                GoesWestAbiAdapter.LARGE_DEFAULT);
        assertThat(d.on("small")).isEmpty();
        assertThat(d.on("large")).containsExactly(GoesWestAbiAdapter.IR);
    }

    @Test
    @DisplayName("NBM and HeatRisk are on for both profiles")
    void nbmAndHeatRiskOnBoth() {
        Declared nbm = new Declared("nbm", NbmAdapter.PRODUCTS, NbmAdapter.LIGHT_DEFAULT, NbmAdapter.LARGE_DEFAULT);
        assertThat(nbm.on("small")).containsExactlyInAnyOrderElementsOf(NbmAdapter.PRODUCTS);
        assertThat(nbm.on("large")).containsExactlyInAnyOrderElementsOf(NbmAdapter.PRODUCTS);
        Declared heat = new Declared("wpc-heatrisk", HeatRiskAdapter.PRODUCTS, HeatRiskAdapter.LIGHT_DEFAULT,
                HeatRiskAdapter.LARGE_DEFAULT);
        assertThat(heat.on("small")).containsExactlyInAnyOrderElementsOf(HeatRiskAdapter.PRODUCTS);
        assertThat(heat.on("large")).containsExactlyInAnyOrderElementsOf(HeatRiskAdapter.PRODUCTS);
    }

    @Test
    @DisplayName("HRRR smoke (surface and column) is on for both profiles")
    void hrrrSmokeOnBoth() {
        assertThat(HrrrAdapter.DEFAULT_PRODUCTS).contains(HrrrAdapter.SMOKE, HrrrAdapter.SMOKE_COLUMN);
    }

    @Test
    @DisplayName("GFS-Wave, AIFS, SST and the MRMS extras are off on small and on on large")
    void largeOnly() {
        List<Declared> declared = List.of(
                new Declared("gfs-wave", GfsWaveAdapter.DEFAULT_PRODUCTS, GfsWaveAdapter.LIGHT_DEFAULT,
                        GfsWaveAdapter.LARGE_DEFAULT),
                new Declared("aifs", AifsAdapter.PRODUCTS, AifsAdapter.LIGHT_DEFAULT, AifsAdapter.LARGE_DEFAULT),
                new Declared("sst-geopolar", List.of(SstAdapter.PRODUCT), SstAdapter.LIGHT_DEFAULT,
                        SstAdapter.LARGE_DEFAULT),
                new Declared("mrms extras", MrmsAdapter.EXTRAS, MrmsAdapter.LIGHT_DEFAULT, MrmsAdapter.LARGE_DEFAULT));
        for (Declared d : declared) {
            assertThat(d.on("small")).as(d.label() + " on small").isEmpty();
            assertThat(d.on("large")).as(d.label() + " on large").containsExactlyInAnyOrderElementsOf(d.all());
        }
    }

    @Test
    @DisplayName("the MRMS adapter applies its extras' defaults by profile, and an explicit list wins")
    void mrmsAdapterExtras() {
        assertThat(new MrmsAdapter(null, "", "small").enabledExtras(null)).isEmpty();
        assertThat(new MrmsAdapter(null, "", "large").enabledExtras(null))
                .containsExactlyInAnyOrderElementsOf(MrmsAdapter.EXTRAS);
        ValidatedParams vil = ValidatedParams.of(Map.of("products", List.of(MrmsAdapter.VIL)));
        assertThat(new MrmsAdapter(null, "", "small").enabledExtras(vil)).containsExactly(MrmsAdapter.VIL);
    }

    static Stream<SourceDescriptor> descriptorsWithProducts() {
        return Stream.of(new GoesAbiAdapter(null, "").descriptor(),
                new GoesWestAbiAdapter(null, "", "small", null).descriptor(),
                new NbmAdapter(new ModelRunIngest(null, null), "", "small").descriptor(),
                new GfsWaveAdapter(new ModelRunIngest(null, null), "").descriptor(),
                new AifsAdapter(new EcmwfRunIngest(null, null), "small", "").descriptor(),
                new SstAdapter(null, "", "small", null).descriptor(),
                new HeatRiskAdapter("", "small").descriptor(),
                new HrrrAdapter(new ModelRunIngest(null, null), "small", "").descriptor(),
                new RtmaAdapter(null, null, "", "small").descriptor(),
                new MrmsAdapter(null, "").descriptor(),
                new NexradL3Adapter("", null).descriptor());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("descriptorsWithProducts")
    @DisplayName("every products param leaves the default to the profile (no descriptor default)")
    void noDescriptorDefault(SourceDescriptor descriptor) {
        ParamSpec products = descriptor.param(ProductEnablement.PRODUCTS_PARAM);
        assertThat(products).as(descriptor.id() + " declares products").isNotNull();
        assertThat(products.hasDefault()).as(descriptor.id() + " products default").isFalse();
    }

    @Test
    @DisplayName("an explicit products list wins, minus unknown ids")
    void explicitWins() {
        ValidatedParams params = ValidatedParams.of(Map.of("products", List.of("b", "zzz")));
        assertThat(ProductEnablement.enabled(params, "small", List.of("a", "b", "c"), List.of("a"),
                List.of("a", "b", "c"))).containsExactly("b");
    }

    @Test
    @DisplayName("the on() helper tests membership")
    void on() {
        assertThat(ProductEnablement.on(Set.of("a"), "a")).isTrue();
        assertThat(ProductEnablement.on(Set.of("a"), "b")).isFalse();
    }
}
