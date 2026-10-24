from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.history_tier import HistoryTier
from ..models.source_descriptor_egress import SourceDescriptorEgress
from ..models.source_descriptor_mode import SourceDescriptorMode
from ..models.source_descriptor_observed_at_mode import SourceDescriptorObservedAtMode
from ..models.source_descriptor_reliability import SourceDescriptorReliability
from ..models.source_descriptor_ttl_anchor import SourceDescriptorTtlAnchor
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.credential_field import CredentialField
    from ..models.declared_observability import DeclaredObservability
    from ..models.field_threshold import FieldThreshold
    from ..models.ingest_policy_all import IngestPolicyAll
    from ..models.ingest_policy_latest import IngestPolicyLatest
    from ..models.ingest_policy_sampled import IngestPolicySampled
    from ..models.licence_terms import LicenceTerms
    from ..models.motion_spec import MotionSpec
    from ..models.param_spec import ParamSpec
    from ..models.source_access import SourceAccess
    from ..models.source_descriptor_entity_keys import SourceDescriptorEntityKeys
    from ..models.source_descriptor_field_specs import SourceDescriptorFieldSpecs
    from ..models.source_descriptor_kind_display import SourceDescriptorKindDisplay
    from ..models.source_descriptor_kind_retention import SourceDescriptorKindRetention


T = TypeVar("T", bound="SourceDescriptor")


@_attrs_define
class SourceDescriptor:
    """
    Attributes:
        id (str):  Example: usgs-earthquakes.
        name (str):
        kinds (list[str]):
        mode (SourceDescriptorMode): Collection mode; `derived` sources are fed by ChangeBus or frame events and never
            polled
        credential_fields (list[CredentialField]):
        params (list[ParamSpec]):
        default_budget (Budget):
        description (str | Unset):
        homepage (str | Unset):
        attribution (str | Unset):
        custom (bool | Unset): Runtime-defined custom push source
        min_interval (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h); null for a source
            that is not polled (streaming or push) Example: 7d.
        default_interval (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h); null for a
            source that is not polled (streaming or push) Example: 7d.
        record_ttl (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        zero_results_normal (bool | Unset):
        tile_properties (list[str] | Unset):
        search_fields (list[str] | Unset):
        embed_fields (list[str] | Unset):
        rank_field (None | str | Unset):
        licence (None | str | Unset): Licence identifier (snapshotted on fetch rows) Example: US-PD.
        terms_url (None | str | Unset):
        history_tier (HistoryTier | Unset): History tier: STANDARD (per-source windows, chunked deletes) or TRACK (one
            `trackWindow`, partition drops)
        observed_at_mode (SourceDescriptorObservedAtMode | Unset): Fixed time base: upstream timestamps, or receipt time
            for every record (TIME_IMPUTED)
        ttl_anchor (SourceDescriptorTtlAnchor | Unset): Axis for TTL and keyframes (TRACK must be OBSERVED; STANDARD
            defaults to RECEIVED)
        default_ingest_policy (IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | Unset): Per-source ingest
            policy, discriminated on `mode` (Core-Data-Model §5.1); same JSON shape as `source_settings.ingest_policy`
            Example: {'mode': 'SAMPLED', 'minInterval': '60s', 'keyframe': '240s', 'minDeviationMeters': 1000, 'thresholds':
            True, 'edgeMinInterval': '30s'}.
        volatile_fields (list[str] | Unset): Top-level payload keys left out of hashes and kinematic state
        significant_fields (list[str] | Unset): Keys whose change persists SIGNIFICANT; empty = every non-volatile key
        motion (MotionSpec | None | Unset):
        thresholds (list[FieldThreshold] | Unset):
        default_retention (None | str | Unset): STANDARD only. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m,
            12h) Example: 1825d.
        kind_retention (SourceDescriptorKindRetention | Unset): Per-kind default retention (STANDARD only)
        skew_tolerance (str | Unset): Future-clamp tolerance before FUTURE_CLAMPED is flagged. ISO-8601 duration (P7D,
            PT15M) or shorthand (7d, 15m, 12h) Example: 2m.
        allow_null_island (bool | Unset): Keep exact (0,0) points instead of nulling them Default: False.
        products (list[str] | Unset): Raster product ids served by this source
        title_field (None | str | Unset): Payload key used as record title Example: title.
        entity_keys (SourceDescriptorEntityKeys | Unset): Entity key type → top-level payload field (icao24,
            registration, mmsi, imo, callsign …) Example: {'icao24': 'hex'}.
        field_specs (SourceDescriptorFieldSpecs | Unset): Declared payload fields keyed by top-level name
        kind_display (SourceDescriptorKindDisplay | Unset): Display hints keyed by kind
        agg_below_zoom (int | None | Unset): Tiles aggregate below this zoom with agg=auto; null = never
        start_field (None | str | Unset): Payload key of an interval start (with endField)
        end_field (None | str | Unset): Payload key of an interval end (with startField)
        private (bool | Unset): Personal data: excluded from anonymous reads, tiles and other egress paths Default:
            False.
        private_precision_decimals (int | Unset): Coordinate decimals kept for private sources Default: 4.
        accepts_remote_media (bool | Unset): Source accepts `POST /v1/ingest/{id}/media|rasters` (satdump-* family)
        egress (SourceDescriptorEgress | Unset): LOCAL_ONLY: served to this instance's own API clients (reads, tiles,
            SSE, MCP, an export to the caller) and notifications, never re-served by sinks (110), federation (113), share
            links (112) or feeds and feed keys; off-box archive exports (111) refuse it too. Set by the upstream's terms:
            opensky-states is LOCAL_ONLY (062), the ODbL adsb.lol sources are OPEN. MLAT positions are never redistributable
            regardless (069). Default: SourceDescriptorEgress.OPEN.
        egress_overridable (bool | Unset): The operator may override `egress` in SourceSettings (local receivers, 069)
            Default: False.
        llm_notice (None | str | Unset): Notice appended whenever this source's records are placed in an LLM context
            (090; e.g. OpenFEMA eligibility notice)
        licence_terms (LicenceTerms | None | Unset): Structured terms; derived from the catalogue when only `licence` is
            set and matches an id
        reliability (SourceDescriptorReliability | Unset): Default reliability (F unless set); overridable per source
        derived_from (list[str] | Unset): DERIVED sources: input source ids; the most restrictive input terms apply
        originator (None | str | Unset): Originator id override (default from intel/originators.json)
        upstream_ref_fields (list[str] | Unset): Payload fields carrying the upstream's own event id ('field', or
            'field@namespace' so differently named fields of two sources compare; '$externalId' is the record's external id)
            or, prefixed 'cite:', the cited original source (a URL, domain or name). Empty means the default from
            intel/originators.json; GET /v1/sources shows the fields in force.
        observability (DeclaredObservability | None | Unset): Declared observability per kind
        access (SourceAccess | Unset): How a source reaches its data and what enabling it involves
    """

    id: str
    name: str
    kinds: list[str]
    mode: SourceDescriptorMode
    credential_fields: list[CredentialField]
    params: list[ParamSpec]
    default_budget: Budget
    description: str | Unset = UNSET
    homepage: str | Unset = UNSET
    attribution: str | Unset = UNSET
    custom: bool | Unset = UNSET
    min_interval: None | str | Unset = UNSET
    default_interval: None | str | Unset = UNSET
    record_ttl: None | str | Unset = UNSET
    zero_results_normal: bool | Unset = UNSET
    tile_properties: list[str] | Unset = UNSET
    search_fields: list[str] | Unset = UNSET
    embed_fields: list[str] | Unset = UNSET
    rank_field: None | str | Unset = UNSET
    licence: None | str | Unset = UNSET
    terms_url: None | str | Unset = UNSET
    history_tier: HistoryTier | Unset = UNSET
    observed_at_mode: SourceDescriptorObservedAtMode | Unset = UNSET
    ttl_anchor: SourceDescriptorTtlAnchor | Unset = UNSET
    default_ingest_policy: (
        IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | Unset
    ) = UNSET
    volatile_fields: list[str] | Unset = UNSET
    significant_fields: list[str] | Unset = UNSET
    motion: MotionSpec | None | Unset = UNSET
    thresholds: list[FieldThreshold] | Unset = UNSET
    default_retention: None | str | Unset = UNSET
    kind_retention: SourceDescriptorKindRetention | Unset = UNSET
    skew_tolerance: str | Unset = UNSET
    allow_null_island: bool | Unset = False
    products: list[str] | Unset = UNSET
    title_field: None | str | Unset = UNSET
    entity_keys: SourceDescriptorEntityKeys | Unset = UNSET
    field_specs: SourceDescriptorFieldSpecs | Unset = UNSET
    kind_display: SourceDescriptorKindDisplay | Unset = UNSET
    agg_below_zoom: int | None | Unset = UNSET
    start_field: None | str | Unset = UNSET
    end_field: None | str | Unset = UNSET
    private: bool | Unset = False
    private_precision_decimals: int | Unset = 4
    accepts_remote_media: bool | Unset = UNSET
    egress: SourceDescriptorEgress | Unset = SourceDescriptorEgress.OPEN
    egress_overridable: bool | Unset = False
    llm_notice: None | str | Unset = UNSET
    licence_terms: LicenceTerms | None | Unset = UNSET
    reliability: SourceDescriptorReliability | Unset = UNSET
    derived_from: list[str] | Unset = UNSET
    originator: None | str | Unset = UNSET
    upstream_ref_fields: list[str] | Unset = UNSET
    observability: DeclaredObservability | None | Unset = UNSET
    access: SourceAccess | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.declared_observability import DeclaredObservability
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_sampled import IngestPolicySampled
        from ..models.licence_terms import LicenceTerms
        from ..models.motion_spec import MotionSpec

        id = self.id

        name = self.name

        kinds = self.kinds

        mode = self.mode.value

        credential_fields = []
        for credential_fields_item_data in self.credential_fields:
            credential_fields_item = credential_fields_item_data.to_dict()
            credential_fields.append(credential_fields_item)

        params = []
        for params_item_data in self.params:
            params_item = params_item_data.to_dict()
            params.append(params_item)

        default_budget = self.default_budget.to_dict()

        description = self.description

        homepage = self.homepage

        attribution = self.attribution

        custom = self.custom

        min_interval: None | str | Unset
        if isinstance(self.min_interval, Unset):
            min_interval = UNSET
        else:
            min_interval = self.min_interval

        default_interval: None | str | Unset
        if isinstance(self.default_interval, Unset):
            default_interval = UNSET
        else:
            default_interval = self.default_interval

        record_ttl: None | str | Unset
        if isinstance(self.record_ttl, Unset):
            record_ttl = UNSET
        else:
            record_ttl = self.record_ttl

        zero_results_normal = self.zero_results_normal

        tile_properties: list[str] | Unset = UNSET
        if not isinstance(self.tile_properties, Unset):
            tile_properties = self.tile_properties

        search_fields: list[str] | Unset = UNSET
        if not isinstance(self.search_fields, Unset):
            search_fields = self.search_fields

        embed_fields: list[str] | Unset = UNSET
        if not isinstance(self.embed_fields, Unset):
            embed_fields = self.embed_fields

        rank_field: None | str | Unset
        if isinstance(self.rank_field, Unset):
            rank_field = UNSET
        else:
            rank_field = self.rank_field

        licence: None | str | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        else:
            licence = self.licence

        terms_url: None | str | Unset
        if isinstance(self.terms_url, Unset):
            terms_url = UNSET
        else:
            terms_url = self.terms_url

        history_tier: str | Unset = UNSET
        if not isinstance(self.history_tier, Unset):
            history_tier = self.history_tier.value

        observed_at_mode: str | Unset = UNSET
        if not isinstance(self.observed_at_mode, Unset):
            observed_at_mode = self.observed_at_mode.value

        ttl_anchor: str | Unset = UNSET
        if not isinstance(self.ttl_anchor, Unset):
            ttl_anchor = self.ttl_anchor.value

        default_ingest_policy: dict[str, Any] | Unset
        if isinstance(self.default_ingest_policy, Unset):
            default_ingest_policy = UNSET
        elif isinstance(self.default_ingest_policy, IngestPolicyAll) or isinstance(
            self.default_ingest_policy, IngestPolicySampled
        ):
            default_ingest_policy = self.default_ingest_policy.to_dict()
        else:
            default_ingest_policy = self.default_ingest_policy.to_dict()

        volatile_fields: list[str] | Unset = UNSET
        if not isinstance(self.volatile_fields, Unset):
            volatile_fields = self.volatile_fields

        significant_fields: list[str] | Unset = UNSET
        if not isinstance(self.significant_fields, Unset):
            significant_fields = self.significant_fields

        motion: dict[str, Any] | None | Unset
        if isinstance(self.motion, Unset):
            motion = UNSET
        elif isinstance(self.motion, MotionSpec):
            motion = self.motion.to_dict()
        else:
            motion = self.motion

        thresholds: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.thresholds, Unset):
            thresholds = []
            for thresholds_item_data in self.thresholds:
                thresholds_item = thresholds_item_data.to_dict()
                thresholds.append(thresholds_item)

        default_retention: None | str | Unset
        if isinstance(self.default_retention, Unset):
            default_retention = UNSET
        else:
            default_retention = self.default_retention

        kind_retention: dict[str, Any] | Unset = UNSET
        if not isinstance(self.kind_retention, Unset):
            kind_retention = self.kind_retention.to_dict()

        skew_tolerance = self.skew_tolerance

        allow_null_island = self.allow_null_island

        products: list[str] | Unset = UNSET
        if not isinstance(self.products, Unset):
            products = self.products

        title_field: None | str | Unset
        if isinstance(self.title_field, Unset):
            title_field = UNSET
        else:
            title_field = self.title_field

        entity_keys: dict[str, Any] | Unset = UNSET
        if not isinstance(self.entity_keys, Unset):
            entity_keys = self.entity_keys.to_dict()

        field_specs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.field_specs, Unset):
            field_specs = self.field_specs.to_dict()

        kind_display: dict[str, Any] | Unset = UNSET
        if not isinstance(self.kind_display, Unset):
            kind_display = self.kind_display.to_dict()

        agg_below_zoom: int | None | Unset
        if isinstance(self.agg_below_zoom, Unset):
            agg_below_zoom = UNSET
        else:
            agg_below_zoom = self.agg_below_zoom

        start_field: None | str | Unset
        if isinstance(self.start_field, Unset):
            start_field = UNSET
        else:
            start_field = self.start_field

        end_field: None | str | Unset
        if isinstance(self.end_field, Unset):
            end_field = UNSET
        else:
            end_field = self.end_field

        private = self.private

        private_precision_decimals = self.private_precision_decimals

        accepts_remote_media = self.accepts_remote_media

        egress: str | Unset = UNSET
        if not isinstance(self.egress, Unset):
            egress = self.egress.value

        egress_overridable = self.egress_overridable

        llm_notice: None | str | Unset
        if isinstance(self.llm_notice, Unset):
            llm_notice = UNSET
        else:
            llm_notice = self.llm_notice

        licence_terms: dict[str, Any] | None | Unset
        if isinstance(self.licence_terms, Unset):
            licence_terms = UNSET
        elif isinstance(self.licence_terms, LicenceTerms):
            licence_terms = self.licence_terms.to_dict()
        else:
            licence_terms = self.licence_terms

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        derived_from: list[str] | Unset = UNSET
        if not isinstance(self.derived_from, Unset):
            derived_from = self.derived_from

        originator: None | str | Unset
        if isinstance(self.originator, Unset):
            originator = UNSET
        else:
            originator = self.originator

        upstream_ref_fields: list[str] | Unset = UNSET
        if not isinstance(self.upstream_ref_fields, Unset):
            upstream_ref_fields = self.upstream_ref_fields

        observability: dict[str, Any] | None | Unset
        if isinstance(self.observability, Unset):
            observability = UNSET
        elif isinstance(self.observability, DeclaredObservability):
            observability = self.observability.to_dict()
        else:
            observability = self.observability

        access: dict[str, Any] | Unset = UNSET
        if not isinstance(self.access, Unset):
            access = self.access.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "kinds": kinds,
                "mode": mode,
                "credentialFields": credential_fields,
                "params": params,
                "defaultBudget": default_budget,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if homepage is not UNSET:
            field_dict["homepage"] = homepage
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if custom is not UNSET:
            field_dict["custom"] = custom
        if min_interval is not UNSET:
            field_dict["minInterval"] = min_interval
        if default_interval is not UNSET:
            field_dict["defaultInterval"] = default_interval
        if record_ttl is not UNSET:
            field_dict["recordTtl"] = record_ttl
        if zero_results_normal is not UNSET:
            field_dict["zeroResultsNormal"] = zero_results_normal
        if tile_properties is not UNSET:
            field_dict["tileProperties"] = tile_properties
        if search_fields is not UNSET:
            field_dict["searchFields"] = search_fields
        if embed_fields is not UNSET:
            field_dict["embedFields"] = embed_fields
        if rank_field is not UNSET:
            field_dict["rankField"] = rank_field
        if licence is not UNSET:
            field_dict["licence"] = licence
        if terms_url is not UNSET:
            field_dict["termsUrl"] = terms_url
        if history_tier is not UNSET:
            field_dict["historyTier"] = history_tier
        if observed_at_mode is not UNSET:
            field_dict["observedAtMode"] = observed_at_mode
        if ttl_anchor is not UNSET:
            field_dict["ttlAnchor"] = ttl_anchor
        if default_ingest_policy is not UNSET:
            field_dict["defaultIngestPolicy"] = default_ingest_policy
        if volatile_fields is not UNSET:
            field_dict["volatileFields"] = volatile_fields
        if significant_fields is not UNSET:
            field_dict["significantFields"] = significant_fields
        if motion is not UNSET:
            field_dict["motion"] = motion
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if default_retention is not UNSET:
            field_dict["defaultRetention"] = default_retention
        if kind_retention is not UNSET:
            field_dict["kindRetention"] = kind_retention
        if skew_tolerance is not UNSET:
            field_dict["skewTolerance"] = skew_tolerance
        if allow_null_island is not UNSET:
            field_dict["allowNullIsland"] = allow_null_island
        if products is not UNSET:
            field_dict["products"] = products
        if title_field is not UNSET:
            field_dict["titleField"] = title_field
        if entity_keys is not UNSET:
            field_dict["entityKeys"] = entity_keys
        if field_specs is not UNSET:
            field_dict["fieldSpecs"] = field_specs
        if kind_display is not UNSET:
            field_dict["kindDisplay"] = kind_display
        if agg_below_zoom is not UNSET:
            field_dict["aggBelowZoom"] = agg_below_zoom
        if start_field is not UNSET:
            field_dict["startField"] = start_field
        if end_field is not UNSET:
            field_dict["endField"] = end_field
        if private is not UNSET:
            field_dict["private"] = private
        if private_precision_decimals is not UNSET:
            field_dict["privatePrecisionDecimals"] = private_precision_decimals
        if accepts_remote_media is not UNSET:
            field_dict["acceptsRemoteMedia"] = accepts_remote_media
        if egress is not UNSET:
            field_dict["egress"] = egress
        if egress_overridable is not UNSET:
            field_dict["egressOverridable"] = egress_overridable
        if llm_notice is not UNSET:
            field_dict["llmNotice"] = llm_notice
        if licence_terms is not UNSET:
            field_dict["licenceTerms"] = licence_terms
        if reliability is not UNSET:
            field_dict["reliability"] = reliability
        if derived_from is not UNSET:
            field_dict["derivedFrom"] = derived_from
        if originator is not UNSET:
            field_dict["originator"] = originator
        if upstream_ref_fields is not UNSET:
            field_dict["upstreamRefFields"] = upstream_ref_fields
        if observability is not UNSET:
            field_dict["observability"] = observability
        if access is not UNSET:
            field_dict["access"] = access

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.budget import Budget
        from ..models.credential_field import CredentialField
        from ..models.declared_observability import DeclaredObservability
        from ..models.field_threshold import FieldThreshold
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled
        from ..models.licence_terms import LicenceTerms
        from ..models.motion_spec import MotionSpec
        from ..models.param_spec import ParamSpec
        from ..models.source_access import SourceAccess
        from ..models.source_descriptor_entity_keys import SourceDescriptorEntityKeys
        from ..models.source_descriptor_field_specs import SourceDescriptorFieldSpecs
        from ..models.source_descriptor_kind_display import SourceDescriptorKindDisplay
        from ..models.source_descriptor_kind_retention import (
            SourceDescriptorKindRetention,
        )

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        kinds = cast(list[str], d.pop("kinds"))

        mode = SourceDescriptorMode(d.pop("mode"))

        credential_fields = []
        _credential_fields = d.pop("credentialFields")
        for credential_fields_item_data in _credential_fields:
            credential_fields_item = CredentialField.from_dict(
                credential_fields_item_data
            )

            credential_fields.append(credential_fields_item)

        params = []
        _params = d.pop("params")
        for params_item_data in _params:
            params_item = ParamSpec.from_dict(params_item_data)

            params.append(params_item)

        default_budget = Budget.from_dict(d.pop("defaultBudget"))

        description = d.pop("description", UNSET)

        homepage = d.pop("homepage", UNSET)

        attribution = d.pop("attribution", UNSET)

        custom = d.pop("custom", UNSET)

        def _parse_min_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        min_interval = _parse_min_interval(d.pop("minInterval", UNSET))

        def _parse_default_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        default_interval = _parse_default_interval(d.pop("defaultInterval", UNSET))

        def _parse_record_ttl(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ttl = _parse_record_ttl(d.pop("recordTtl", UNSET))

        zero_results_normal = d.pop("zeroResultsNormal", UNSET)

        tile_properties = cast(list[str], d.pop("tileProperties", UNSET))

        search_fields = cast(list[str], d.pop("searchFields", UNSET))

        embed_fields = cast(list[str], d.pop("embedFields", UNSET))

        def _parse_rank_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        rank_field = _parse_rank_field(d.pop("rankField", UNSET))

        def _parse_licence(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        def _parse_terms_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        terms_url = _parse_terms_url(d.pop("termsUrl", UNSET))

        _history_tier = d.pop("historyTier", UNSET)
        history_tier: HistoryTier | Unset
        if isinstance(_history_tier, Unset):
            history_tier = UNSET
        else:
            history_tier = HistoryTier(_history_tier)

        _observed_at_mode = d.pop("observedAtMode", UNSET)
        observed_at_mode: SourceDescriptorObservedAtMode | Unset
        if isinstance(_observed_at_mode, Unset):
            observed_at_mode = UNSET
        else:
            observed_at_mode = SourceDescriptorObservedAtMode(_observed_at_mode)

        _ttl_anchor = d.pop("ttlAnchor", UNSET)
        ttl_anchor: SourceDescriptorTtlAnchor | Unset
        if isinstance(_ttl_anchor, Unset):
            ttl_anchor = UNSET
        else:
            ttl_anchor = SourceDescriptorTtlAnchor(_ttl_anchor)

        def _parse_default_ingest_policy(
            data: object,
        ) -> IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | Unset:
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_ingest_policy_type_0 = IngestPolicyAll.from_dict(data)

                return componentsschemas_ingest_policy_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_ingest_policy_type_1 = IngestPolicySampled.from_dict(
                    data
                )

                return componentsschemas_ingest_policy_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            componentsschemas_ingest_policy_type_2 = IngestPolicyLatest.from_dict(data)

            return componentsschemas_ingest_policy_type_2

        default_ingest_policy = _parse_default_ingest_policy(
            d.pop("defaultIngestPolicy", UNSET)
        )

        volatile_fields = cast(list[str], d.pop("volatileFields", UNSET))

        significant_fields = cast(list[str], d.pop("significantFields", UNSET))

        def _parse_motion(data: object) -> MotionSpec | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                motion_type_0 = MotionSpec.from_dict(data)

                return motion_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MotionSpec | None | Unset, data)

        motion = _parse_motion(d.pop("motion", UNSET))

        _thresholds = d.pop("thresholds", UNSET)
        thresholds: list[FieldThreshold] | Unset = UNSET
        if _thresholds is not UNSET:
            thresholds = []
            for thresholds_item_data in _thresholds:
                thresholds_item = FieldThreshold.from_dict(thresholds_item_data)

                thresholds.append(thresholds_item)

        def _parse_default_retention(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        default_retention = _parse_default_retention(d.pop("defaultRetention", UNSET))

        _kind_retention = d.pop("kindRetention", UNSET)
        kind_retention: SourceDescriptorKindRetention | Unset
        if isinstance(_kind_retention, Unset):
            kind_retention = UNSET
        else:
            kind_retention = SourceDescriptorKindRetention.from_dict(_kind_retention)

        skew_tolerance = d.pop("skewTolerance", UNSET)

        allow_null_island = d.pop("allowNullIsland", UNSET)

        products = cast(list[str], d.pop("products", UNSET))

        def _parse_title_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title_field = _parse_title_field(d.pop("titleField", UNSET))

        _entity_keys = d.pop("entityKeys", UNSET)
        entity_keys: SourceDescriptorEntityKeys | Unset
        if isinstance(_entity_keys, Unset):
            entity_keys = UNSET
        else:
            entity_keys = SourceDescriptorEntityKeys.from_dict(_entity_keys)

        _field_specs = d.pop("fieldSpecs", UNSET)
        field_specs: SourceDescriptorFieldSpecs | Unset
        if isinstance(_field_specs, Unset):
            field_specs = UNSET
        else:
            field_specs = SourceDescriptorFieldSpecs.from_dict(_field_specs)

        _kind_display = d.pop("kindDisplay", UNSET)
        kind_display: SourceDescriptorKindDisplay | Unset
        if isinstance(_kind_display, Unset):
            kind_display = UNSET
        else:
            kind_display = SourceDescriptorKindDisplay.from_dict(_kind_display)

        def _parse_agg_below_zoom(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        agg_below_zoom = _parse_agg_below_zoom(d.pop("aggBelowZoom", UNSET))

        def _parse_start_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        start_field = _parse_start_field(d.pop("startField", UNSET))

        def _parse_end_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        end_field = _parse_end_field(d.pop("endField", UNSET))

        private = d.pop("private", UNSET)

        private_precision_decimals = d.pop("privatePrecisionDecimals", UNSET)

        accepts_remote_media = d.pop("acceptsRemoteMedia", UNSET)

        _egress = d.pop("egress", UNSET)
        egress: SourceDescriptorEgress | Unset
        if isinstance(_egress, Unset):
            egress = UNSET
        else:
            egress = SourceDescriptorEgress(_egress)

        egress_overridable = d.pop("egressOverridable", UNSET)

        def _parse_llm_notice(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        llm_notice = _parse_llm_notice(d.pop("llmNotice", UNSET))

        def _parse_licence_terms(data: object) -> LicenceTerms | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                licence_terms_type_0 = LicenceTerms.from_dict(data)

                return licence_terms_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LicenceTerms | None | Unset, data)

        licence_terms = _parse_licence_terms(d.pop("licenceTerms", UNSET))

        _reliability = d.pop("reliability", UNSET)
        reliability: SourceDescriptorReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = SourceDescriptorReliability(_reliability)

        derived_from = cast(list[str], d.pop("derivedFrom", UNSET))

        def _parse_originator(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        originator = _parse_originator(d.pop("originator", UNSET))

        upstream_ref_fields = cast(list[str], d.pop("upstreamRefFields", UNSET))

        def _parse_observability(data: object) -> DeclaredObservability | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                observability_type_0 = DeclaredObservability.from_dict(data)

                return observability_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DeclaredObservability | None | Unset, data)

        observability = _parse_observability(d.pop("observability", UNSET))

        _access = d.pop("access", UNSET)
        access: SourceAccess | Unset
        if isinstance(_access, Unset):
            access = UNSET
        else:
            access = SourceAccess.from_dict(_access)

        source_descriptor = cls(
            id=id,
            name=name,
            kinds=kinds,
            mode=mode,
            credential_fields=credential_fields,
            params=params,
            default_budget=default_budget,
            description=description,
            homepage=homepage,
            attribution=attribution,
            custom=custom,
            min_interval=min_interval,
            default_interval=default_interval,
            record_ttl=record_ttl,
            zero_results_normal=zero_results_normal,
            tile_properties=tile_properties,
            search_fields=search_fields,
            embed_fields=embed_fields,
            rank_field=rank_field,
            licence=licence,
            terms_url=terms_url,
            history_tier=history_tier,
            observed_at_mode=observed_at_mode,
            ttl_anchor=ttl_anchor,
            default_ingest_policy=default_ingest_policy,
            volatile_fields=volatile_fields,
            significant_fields=significant_fields,
            motion=motion,
            thresholds=thresholds,
            default_retention=default_retention,
            kind_retention=kind_retention,
            skew_tolerance=skew_tolerance,
            allow_null_island=allow_null_island,
            products=products,
            title_field=title_field,
            entity_keys=entity_keys,
            field_specs=field_specs,
            kind_display=kind_display,
            agg_below_zoom=agg_below_zoom,
            start_field=start_field,
            end_field=end_field,
            private=private,
            private_precision_decimals=private_precision_decimals,
            accepts_remote_media=accepts_remote_media,
            egress=egress,
            egress_overridable=egress_overridable,
            llm_notice=llm_notice,
            licence_terms=licence_terms,
            reliability=reliability,
            derived_from=derived_from,
            originator=originator,
            upstream_ref_fields=upstream_ref_fields,
            observability=observability,
            access=access,
        )

        source_descriptor.additional_properties = d
        return source_descriptor

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
