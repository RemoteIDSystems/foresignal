from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.custom_source_input_observed_at_mode import (
    CustomSourceInputObservedAtMode,
)
from ..models.custom_source_input_profile_type_1 import CustomSourceInputProfileType1
from ..models.custom_source_input_profile_type_2_type_1 import (
    CustomSourceInputProfileType2Type1,
)
from ..models.custom_source_input_profile_type_3_type_1 import (
    CustomSourceInputProfileType3Type1,
)
from ..models.custom_source_input_ttl_anchor import CustomSourceInputTtlAnchor
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.custom_source_formats import CustomSourceFormats
    from ..models.custom_source_input_field_specs import CustomSourceInputFieldSpecs
    from ..models.custom_source_input_preset_params_type_0 import (
        CustomSourceInputPresetParamsType0,
    )
    from ..models.custom_source_input_verifiers import CustomSourceInputVerifiers
    from ..models.licence_terms import LicenceTerms
    from ..models.mapping_preset_setup import MappingPresetSetup
    from ..models.record_mapping import RecordMapping


T = TypeVar("T", bound="CustomSourceCreated")


@_attrs_define
class CustomSourceCreated:
    """
    Attributes:
        id (str):
        name (str):
        kinds (list[str]):
        created_at (datetime.datetime):
        ingest_key (str): Shown once
        description (str | Unset):
        attribution (str | Unset):
        record_ttl (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        tile_properties (list[str] | Unset):
        search_fields (list[str] | Unset):
        embed_fields (list[str] | Unset):
        rank_field (None | str | Unset):
        rate_limit_per_minute (int | Unset):  Default: 60.
        observed_at_mode (CustomSourceInputObservedAtMode | Unset): Time base; changeable only while the source has no
            heads (409 OBSERVED_AT_MODE_LOCKED). Under UPSTREAM a record without observedAt is rejected MISSING_OBSERVED_AT.
            Default: CustomSourceInputObservedAtMode.UPSTREAM.
        ttl_anchor (CustomSourceInputTtlAnchor | Unset):  Default: CustomSourceInputTtlAnchor.RECEIVED.
        volatile_fields (list[str] | Unset): Excluded from the hash; editing triggers lazy recipe migration
        significant_fields (list[str] | Unset): Required for SAMPLED/LATEST policies
        title_field (None | str | Unset):
        field_specs (CustomSourceInputFieldSpecs | Unset): Keys are top-level payload names
        private (bool | Unset): Private source (039); cannot be unset for profile personal-location Default: False.
        licence (None | str | Unset):
        records_per_minute (int | Unset): Records/min cap per source (429 INGEST_RECORD_RATE) Default: 6000.
        formats (CustomSourceFormats | Unset): Per-format options of a custom source (024); keys are format ids
        verifiers (CustomSourceInputVerifiers | Unset): Signature verifier per format id
        profile (CustomSourceInputProfileType1 | CustomSourceInputProfileType2Type1 | CustomSourceInputProfileType3Type1
            | None | Unset): Preset defaults (039): personal-location = private, STANDARD, kind tracker, UPSTREAM + OBSERVED
            anchor, 24h TTL, 7d retention, SAMPLED policy, no embedding, no enrichment
        mapping (None | RecordMapping | Unset): RecordMapping used by the `mapping` ingest format and the device-preset
            formats (replaces 024's interim JSON-Pointer mapping under formats.mapping). Compiled against this input's kinds
            and observedAtMode (400 MAPPING_INVALID with per-path errors); editing it publishes the custom-source change
            event
        preset (None | str | Unset): Create only: a MappingPreset id (`GET /v1/mappings/presets`) whose defaults fill
            every member the input leaves out (kinds, fieldSpecs, observedAtMode, recordTtl, significantFields,
            volatileFields, private, allowPathKey, tileProperties, titleField, mapping, format options) and whose format
            becomes the default of `POST /v1/ingest/{sourceId}`. On update it may only repeat the stored value
        preset_params (CustomSourceInputPresetParamsType0 | None | Unset): Create only: preset parameters, e.g. `lon`,
            `lat` (rounded to 0.01° unless `exactLocation=true`)
        allow_path_key (bool | Unset): Accept the ingest key in the path (`/v1/ingest/{sourceId}/k/{key}/{format}`)
            Default: False.
        ingest_allow_cidrs (list[str] | None | Unset): Sender allowlist; null = private ranges only (loopback, RFC 1918,
            link-local, ULA) when a path key, query key or GET is used. Others → 403 INGEST_SOURCE_DENIED
        capture_samples (bool | Unset): Keep the last 5 raw requests in memory (redacted) for `/samples`; auto-off 1 h
            after it is turned on (a PUT that keeps it on does not extend the window; turning it off drops the samples)
            Default: False.
        licence_terms (LicenceTerms | None | Unset): Licence of pushed data, snapshotted on each PUSH batch
        last_ingest_at (datetime.datetime | None | Unset):
        ingest_key_prefix (str | Unset):
        setup (MappingPresetSetup | None | Unset): Device setup instructions with the concrete URL (contains the key;
            shown once) when created from a preset
    """

    id: str
    name: str
    kinds: list[str]
    created_at: datetime.datetime
    ingest_key: str
    description: str | Unset = UNSET
    attribution: str | Unset = UNSET
    record_ttl: None | str | Unset = UNSET
    tile_properties: list[str] | Unset = UNSET
    search_fields: list[str] | Unset = UNSET
    embed_fields: list[str] | Unset = UNSET
    rank_field: None | str | Unset = UNSET
    rate_limit_per_minute: int | Unset = 60
    observed_at_mode: CustomSourceInputObservedAtMode | Unset = (
        CustomSourceInputObservedAtMode.UPSTREAM
    )
    ttl_anchor: CustomSourceInputTtlAnchor | Unset = CustomSourceInputTtlAnchor.RECEIVED
    volatile_fields: list[str] | Unset = UNSET
    significant_fields: list[str] | Unset = UNSET
    title_field: None | str | Unset = UNSET
    field_specs: CustomSourceInputFieldSpecs | Unset = UNSET
    private: bool | Unset = False
    licence: None | str | Unset = UNSET
    records_per_minute: int | Unset = 6000
    formats: CustomSourceFormats | Unset = UNSET
    verifiers: CustomSourceInputVerifiers | Unset = UNSET
    profile: (
        CustomSourceInputProfileType1
        | CustomSourceInputProfileType2Type1
        | CustomSourceInputProfileType3Type1
        | None
        | Unset
    ) = UNSET
    mapping: None | RecordMapping | Unset = UNSET
    preset: None | str | Unset = UNSET
    preset_params: CustomSourceInputPresetParamsType0 | None | Unset = UNSET
    allow_path_key: bool | Unset = False
    ingest_allow_cidrs: list[str] | None | Unset = UNSET
    capture_samples: bool | Unset = False
    licence_terms: LicenceTerms | None | Unset = UNSET
    last_ingest_at: datetime.datetime | None | Unset = UNSET
    ingest_key_prefix: str | Unset = UNSET
    setup: MappingPresetSetup | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.custom_source_input_preset_params_type_0 import (
            CustomSourceInputPresetParamsType0,
        )
        from ..models.licence_terms import LicenceTerms
        from ..models.mapping_preset_setup import MappingPresetSetup
        from ..models.record_mapping import RecordMapping

        id = self.id

        name = self.name

        kinds = self.kinds

        created_at = self.created_at.isoformat()

        ingest_key = self.ingest_key

        description = self.description

        attribution = self.attribution

        record_ttl: None | str | Unset
        if isinstance(self.record_ttl, Unset):
            record_ttl = UNSET
        else:
            record_ttl = self.record_ttl

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

        rate_limit_per_minute = self.rate_limit_per_minute

        observed_at_mode: str | Unset = UNSET
        if not isinstance(self.observed_at_mode, Unset):
            observed_at_mode = self.observed_at_mode.value

        ttl_anchor: str | Unset = UNSET
        if not isinstance(self.ttl_anchor, Unset):
            ttl_anchor = self.ttl_anchor.value

        volatile_fields: list[str] | Unset = UNSET
        if not isinstance(self.volatile_fields, Unset):
            volatile_fields = self.volatile_fields

        significant_fields: list[str] | Unset = UNSET
        if not isinstance(self.significant_fields, Unset):
            significant_fields = self.significant_fields

        title_field: None | str | Unset
        if isinstance(self.title_field, Unset):
            title_field = UNSET
        else:
            title_field = self.title_field

        field_specs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.field_specs, Unset):
            field_specs = self.field_specs.to_dict()

        private = self.private

        licence: None | str | Unset
        if isinstance(self.licence, Unset):
            licence = UNSET
        else:
            licence = self.licence

        records_per_minute = self.records_per_minute

        formats: dict[str, Any] | Unset = UNSET
        if not isinstance(self.formats, Unset):
            formats = self.formats.to_dict()

        verifiers: dict[str, Any] | Unset = UNSET
        if not isinstance(self.verifiers, Unset):
            verifiers = self.verifiers.to_dict()

        profile: None | str | Unset
        if isinstance(self.profile, Unset):
            profile = UNSET
        elif (
            isinstance(self.profile, CustomSourceInputProfileType1)
            or isinstance(self.profile, CustomSourceInputProfileType2Type1)
            or isinstance(self.profile, CustomSourceInputProfileType3Type1)
        ):
            profile = self.profile.value
        else:
            profile = self.profile

        mapping: dict[str, Any] | None | Unset
        if isinstance(self.mapping, Unset):
            mapping = UNSET
        elif isinstance(self.mapping, RecordMapping):
            mapping = self.mapping.to_dict()
        else:
            mapping = self.mapping

        preset: None | str | Unset
        if isinstance(self.preset, Unset):
            preset = UNSET
        else:
            preset = self.preset

        preset_params: dict[str, Any] | None | Unset
        if isinstance(self.preset_params, Unset):
            preset_params = UNSET
        elif isinstance(self.preset_params, CustomSourceInputPresetParamsType0):
            preset_params = self.preset_params.to_dict()
        else:
            preset_params = self.preset_params

        allow_path_key = self.allow_path_key

        ingest_allow_cidrs: list[str] | None | Unset
        if isinstance(self.ingest_allow_cidrs, Unset):
            ingest_allow_cidrs = UNSET
        elif isinstance(self.ingest_allow_cidrs, list):
            ingest_allow_cidrs = self.ingest_allow_cidrs

        else:
            ingest_allow_cidrs = self.ingest_allow_cidrs

        capture_samples = self.capture_samples

        licence_terms: dict[str, Any] | None | Unset
        if isinstance(self.licence_terms, Unset):
            licence_terms = UNSET
        elif isinstance(self.licence_terms, LicenceTerms):
            licence_terms = self.licence_terms.to_dict()
        else:
            licence_terms = self.licence_terms

        last_ingest_at: None | str | Unset
        if isinstance(self.last_ingest_at, Unset):
            last_ingest_at = UNSET
        elif isinstance(self.last_ingest_at, datetime.datetime):
            last_ingest_at = self.last_ingest_at.isoformat()
        else:
            last_ingest_at = self.last_ingest_at

        ingest_key_prefix = self.ingest_key_prefix

        setup: dict[str, Any] | None | Unset
        if isinstance(self.setup, Unset):
            setup = UNSET
        elif isinstance(self.setup, MappingPresetSetup):
            setup = self.setup.to_dict()
        else:
            setup = self.setup

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "kinds": kinds,
                "createdAt": created_at,
                "ingestKey": ingest_key,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if attribution is not UNSET:
            field_dict["attribution"] = attribution
        if record_ttl is not UNSET:
            field_dict["recordTtl"] = record_ttl
        if tile_properties is not UNSET:
            field_dict["tileProperties"] = tile_properties
        if search_fields is not UNSET:
            field_dict["searchFields"] = search_fields
        if embed_fields is not UNSET:
            field_dict["embedFields"] = embed_fields
        if rank_field is not UNSET:
            field_dict["rankField"] = rank_field
        if rate_limit_per_minute is not UNSET:
            field_dict["rateLimitPerMinute"] = rate_limit_per_minute
        if observed_at_mode is not UNSET:
            field_dict["observedAtMode"] = observed_at_mode
        if ttl_anchor is not UNSET:
            field_dict["ttlAnchor"] = ttl_anchor
        if volatile_fields is not UNSET:
            field_dict["volatileFields"] = volatile_fields
        if significant_fields is not UNSET:
            field_dict["significantFields"] = significant_fields
        if title_field is not UNSET:
            field_dict["titleField"] = title_field
        if field_specs is not UNSET:
            field_dict["fieldSpecs"] = field_specs
        if private is not UNSET:
            field_dict["private"] = private
        if licence is not UNSET:
            field_dict["licence"] = licence
        if records_per_minute is not UNSET:
            field_dict["recordsPerMinute"] = records_per_minute
        if formats is not UNSET:
            field_dict["formats"] = formats
        if verifiers is not UNSET:
            field_dict["verifiers"] = verifiers
        if profile is not UNSET:
            field_dict["profile"] = profile
        if mapping is not UNSET:
            field_dict["mapping"] = mapping
        if preset is not UNSET:
            field_dict["preset"] = preset
        if preset_params is not UNSET:
            field_dict["presetParams"] = preset_params
        if allow_path_key is not UNSET:
            field_dict["allowPathKey"] = allow_path_key
        if ingest_allow_cidrs is not UNSET:
            field_dict["ingestAllowCidrs"] = ingest_allow_cidrs
        if capture_samples is not UNSET:
            field_dict["captureSamples"] = capture_samples
        if licence_terms is not UNSET:
            field_dict["licenceTerms"] = licence_terms
        if last_ingest_at is not UNSET:
            field_dict["lastIngestAt"] = last_ingest_at
        if ingest_key_prefix is not UNSET:
            field_dict["ingestKeyPrefix"] = ingest_key_prefix
        if setup is not UNSET:
            field_dict["setup"] = setup

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.custom_source_formats import CustomSourceFormats
        from ..models.custom_source_input_field_specs import CustomSourceInputFieldSpecs
        from ..models.custom_source_input_preset_params_type_0 import (
            CustomSourceInputPresetParamsType0,
        )
        from ..models.custom_source_input_verifiers import CustomSourceInputVerifiers
        from ..models.licence_terms import LicenceTerms
        from ..models.mapping_preset_setup import MappingPresetSetup
        from ..models.record_mapping import RecordMapping

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        kinds = cast(list[str], d.pop("kinds"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        ingest_key = d.pop("ingestKey")

        description = d.pop("description", UNSET)

        attribution = d.pop("attribution", UNSET)

        def _parse_record_ttl(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ttl = _parse_record_ttl(d.pop("recordTtl", UNSET))

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

        rate_limit_per_minute = d.pop("rateLimitPerMinute", UNSET)

        _observed_at_mode = d.pop("observedAtMode", UNSET)
        observed_at_mode: CustomSourceInputObservedAtMode | Unset
        if isinstance(_observed_at_mode, Unset):
            observed_at_mode = UNSET
        else:
            observed_at_mode = CustomSourceInputObservedAtMode(_observed_at_mode)

        _ttl_anchor = d.pop("ttlAnchor", UNSET)
        ttl_anchor: CustomSourceInputTtlAnchor | Unset
        if isinstance(_ttl_anchor, Unset):
            ttl_anchor = UNSET
        else:
            ttl_anchor = CustomSourceInputTtlAnchor(_ttl_anchor)

        volatile_fields = cast(list[str], d.pop("volatileFields", UNSET))

        significant_fields = cast(list[str], d.pop("significantFields", UNSET))

        def _parse_title_field(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title_field = _parse_title_field(d.pop("titleField", UNSET))

        _field_specs = d.pop("fieldSpecs", UNSET)
        field_specs: CustomSourceInputFieldSpecs | Unset
        if isinstance(_field_specs, Unset):
            field_specs = UNSET
        else:
            field_specs = CustomSourceInputFieldSpecs.from_dict(_field_specs)

        private = d.pop("private", UNSET)

        def _parse_licence(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        licence = _parse_licence(d.pop("licence", UNSET))

        records_per_minute = d.pop("recordsPerMinute", UNSET)

        _formats = d.pop("formats", UNSET)
        formats: CustomSourceFormats | Unset
        if isinstance(_formats, Unset):
            formats = UNSET
        else:
            formats = CustomSourceFormats.from_dict(_formats)

        _verifiers = d.pop("verifiers", UNSET)
        verifiers: CustomSourceInputVerifiers | Unset
        if isinstance(_verifiers, Unset):
            verifiers = UNSET
        else:
            verifiers = CustomSourceInputVerifiers.from_dict(_verifiers)

        def _parse_profile(
            data: object,
        ) -> (
            CustomSourceInputProfileType1
            | CustomSourceInputProfileType2Type1
            | CustomSourceInputProfileType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                profile_type_1 = CustomSourceInputProfileType1(data)

                return profile_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                profile_type_2_type_1 = CustomSourceInputProfileType2Type1(data)

                return profile_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                profile_type_3_type_1 = CustomSourceInputProfileType3Type1(data)

                return profile_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                CustomSourceInputProfileType1
                | CustomSourceInputProfileType2Type1
                | CustomSourceInputProfileType3Type1
                | None
                | Unset,
                data,
            )

        profile = _parse_profile(d.pop("profile", UNSET))

        def _parse_mapping(data: object) -> None | RecordMapping | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                mapping_type_0 = RecordMapping.from_dict(data)

                return mapping_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RecordMapping | Unset, data)

        mapping = _parse_mapping(d.pop("mapping", UNSET))

        def _parse_preset(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        preset = _parse_preset(d.pop("preset", UNSET))

        def _parse_preset_params(
            data: object,
        ) -> CustomSourceInputPresetParamsType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                preset_params_type_0 = CustomSourceInputPresetParamsType0.from_dict(
                    data
                )

                return preset_params_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CustomSourceInputPresetParamsType0 | None | Unset, data)

        preset_params = _parse_preset_params(d.pop("presetParams", UNSET))

        allow_path_key = d.pop("allowPathKey", UNSET)

        def _parse_ingest_allow_cidrs(data: object) -> list[str] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                ingest_allow_cidrs_type_0 = cast(list[str], data)

                return ingest_allow_cidrs_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[str] | None | Unset, data)

        ingest_allow_cidrs = _parse_ingest_allow_cidrs(d.pop("ingestAllowCidrs", UNSET))

        capture_samples = d.pop("captureSamples", UNSET)

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

        def _parse_last_ingest_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_ingest_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_ingest_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_ingest_at = _parse_last_ingest_at(d.pop("lastIngestAt", UNSET))

        ingest_key_prefix = d.pop("ingestKeyPrefix", UNSET)

        def _parse_setup(data: object) -> MappingPresetSetup | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                setup_type_0 = MappingPresetSetup.from_dict(data)

                return setup_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(MappingPresetSetup | None | Unset, data)

        setup = _parse_setup(d.pop("setup", UNSET))

        custom_source_created = cls(
            id=id,
            name=name,
            kinds=kinds,
            created_at=created_at,
            ingest_key=ingest_key,
            description=description,
            attribution=attribution,
            record_ttl=record_ttl,
            tile_properties=tile_properties,
            search_fields=search_fields,
            embed_fields=embed_fields,
            rank_field=rank_field,
            rate_limit_per_minute=rate_limit_per_minute,
            observed_at_mode=observed_at_mode,
            ttl_anchor=ttl_anchor,
            volatile_fields=volatile_fields,
            significant_fields=significant_fields,
            title_field=title_field,
            field_specs=field_specs,
            private=private,
            licence=licence,
            records_per_minute=records_per_minute,
            formats=formats,
            verifiers=verifiers,
            profile=profile,
            mapping=mapping,
            preset=preset,
            preset_params=preset_params,
            allow_path_key=allow_path_key,
            ingest_allow_cidrs=ingest_allow_cidrs,
            capture_samples=capture_samples,
            licence_terms=licence_terms,
            last_ingest_at=last_ingest_at,
            ingest_key_prefix=ingest_key_prefix,
            setup=setup,
        )

        custom_source_created.additional_properties = d
        return custom_source_created

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
