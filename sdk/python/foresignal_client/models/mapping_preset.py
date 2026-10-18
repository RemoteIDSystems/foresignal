from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mapping_preset_observed_at_mode import MappingPresetObservedAtMode
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ingest_policy_all import IngestPolicyAll
    from ..models.ingest_policy_latest import IngestPolicyLatest
    from ..models.ingest_policy_sampled import IngestPolicySampled
    from ..models.mapping_preset_field_specs import MappingPresetFieldSpecs
    from ..models.mapping_preset_setup import MappingPresetSetup
    from ..models.param_spec import ParamSpec
    from ..models.record_mapping import RecordMapping


T = TypeVar("T", bound="MappingPreset")


@_attrs_define
class MappingPreset:
    """A built-in device preset: format, mapping and custom-source defaults (059)

    Attributes:
        id (str):  Example: ecowitt.
        title (str):
        format_ (str): Ingest format id used at `/v1/ingest/{sourceId}/{format}` Example: ecowitt.
        kinds (list[str]):
        observed_at_mode (MappingPresetObservedAtMode):
        params (list[ParamSpec]): Preset parameters (e.g. lon, lat, exactLocation)
        setup (MappingPresetSetup): Device-side setup for a preset: instructions and the exact ingest URL (059). On a
            preset's catalogue entry `url` is a template; on creation it is concrete and contains the key once.
        description (str | Unset):
        mapping (None | RecordMapping | Unset): null when the preset delegates to a built-in format (e.g.
            `alertmanager`)
        field_specs (MappingPresetFieldSpecs | Unset):
        ttl (None | str | Unset): Record TTL (ISO-8601 duration or shorthand)
        ingest_policy (IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset):
        significant_fields (list[str] | Unset):
        volatile_fields (list[str] | Unset):
        private (bool | Unset):
        allow_path_key (bool | Unset):
        allows_get (bool | Unset): The format accepts GET (wu-pws, gmc)
    """

    id: str
    title: str
    format_: str
    kinds: list[str]
    observed_at_mode: MappingPresetObservedAtMode
    params: list[ParamSpec]
    setup: MappingPresetSetup
    description: str | Unset = UNSET
    mapping: None | RecordMapping | Unset = UNSET
    field_specs: MappingPresetFieldSpecs | Unset = UNSET
    ttl: None | str | Unset = UNSET
    ingest_policy: (
        IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset
    ) = UNSET
    significant_fields: list[str] | Unset = UNSET
    volatile_fields: list[str] | Unset = UNSET
    private: bool | Unset = UNSET
    allow_path_key: bool | Unset = UNSET
    allows_get: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled
        from ..models.record_mapping import RecordMapping

        id = self.id

        title = self.title

        format_ = self.format_

        kinds = self.kinds

        observed_at_mode = self.observed_at_mode.value

        params = []
        for params_item_data in self.params:
            params_item = params_item_data.to_dict()
            params.append(params_item)

        setup = self.setup.to_dict()

        description = self.description

        mapping: dict[str, Any] | None | Unset
        if isinstance(self.mapping, Unset):
            mapping = UNSET
        elif isinstance(self.mapping, RecordMapping):
            mapping = self.mapping.to_dict()
        else:
            mapping = self.mapping

        field_specs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.field_specs, Unset):
            field_specs = self.field_specs.to_dict()

        ttl: None | str | Unset
        if isinstance(self.ttl, Unset):
            ttl = UNSET
        else:
            ttl = self.ttl

        ingest_policy: dict[str, Any] | None | Unset
        if isinstance(self.ingest_policy, Unset):
            ingest_policy = UNSET
        elif (
            isinstance(self.ingest_policy, IngestPolicyAll)
            or isinstance(self.ingest_policy, IngestPolicySampled)
            or isinstance(self.ingest_policy, IngestPolicyLatest)
        ):
            ingest_policy = self.ingest_policy.to_dict()
        else:
            ingest_policy = self.ingest_policy

        significant_fields: list[str] | Unset = UNSET
        if not isinstance(self.significant_fields, Unset):
            significant_fields = self.significant_fields

        volatile_fields: list[str] | Unset = UNSET
        if not isinstance(self.volatile_fields, Unset):
            volatile_fields = self.volatile_fields

        private = self.private

        allow_path_key = self.allow_path_key

        allows_get = self.allows_get

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
                "format": format_,
                "kinds": kinds,
                "observedAtMode": observed_at_mode,
                "params": params,
                "setup": setup,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if mapping is not UNSET:
            field_dict["mapping"] = mapping
        if field_specs is not UNSET:
            field_dict["fieldSpecs"] = field_specs
        if ttl is not UNSET:
            field_dict["ttl"] = ttl
        if ingest_policy is not UNSET:
            field_dict["ingestPolicy"] = ingest_policy
        if significant_fields is not UNSET:
            field_dict["significantFields"] = significant_fields
        if volatile_fields is not UNSET:
            field_dict["volatileFields"] = volatile_fields
        if private is not UNSET:
            field_dict["private"] = private
        if allow_path_key is not UNSET:
            field_dict["allowPathKey"] = allow_path_key
        if allows_get is not UNSET:
            field_dict["allowsGet"] = allows_get

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled
        from ..models.mapping_preset_field_specs import MappingPresetFieldSpecs
        from ..models.mapping_preset_setup import MappingPresetSetup
        from ..models.param_spec import ParamSpec
        from ..models.record_mapping import RecordMapping

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        format_ = d.pop("format")

        kinds = cast(list[str], d.pop("kinds"))

        observed_at_mode = MappingPresetObservedAtMode(d.pop("observedAtMode"))

        params = []
        _params = d.pop("params")
        for params_item_data in _params:
            params_item = ParamSpec.from_dict(params_item_data)

            params.append(params_item)

        setup = MappingPresetSetup.from_dict(d.pop("setup"))

        description = d.pop("description", UNSET)

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

        _field_specs = d.pop("fieldSpecs", UNSET)
        field_specs: MappingPresetFieldSpecs | Unset
        if isinstance(_field_specs, Unset):
            field_specs = UNSET
        else:
            field_specs = MappingPresetFieldSpecs.from_dict(_field_specs)

        def _parse_ttl(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ttl = _parse_ttl(d.pop("ttl", UNSET))

        def _parse_ingest_policy(
            data: object,
        ) -> IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset:
            if data is None:
                return data
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
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_ingest_policy_type_2 = IngestPolicyLatest.from_dict(
                    data
                )

                return componentsschemas_ingest_policy_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                IngestPolicyAll
                | IngestPolicyLatest
                | IngestPolicySampled
                | None
                | Unset,
                data,
            )

        ingest_policy = _parse_ingest_policy(d.pop("ingestPolicy", UNSET))

        significant_fields = cast(list[str], d.pop("significantFields", UNSET))

        volatile_fields = cast(list[str], d.pop("volatileFields", UNSET))

        private = d.pop("private", UNSET)

        allow_path_key = d.pop("allowPathKey", UNSET)

        allows_get = d.pop("allowsGet", UNSET)

        mapping_preset = cls(
            id=id,
            title=title,
            format_=format_,
            kinds=kinds,
            observed_at_mode=observed_at_mode,
            params=params,
            setup=setup,
            description=description,
            mapping=mapping,
            field_specs=field_specs,
            ttl=ttl,
            ingest_policy=ingest_policy,
            significant_fields=significant_fields,
            volatile_fields=volatile_fields,
            private=private,
            allow_path_key=allow_path_key,
            allows_get=allows_get,
        )

        mapping_preset.additional_properties = d
        return mapping_preset

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
