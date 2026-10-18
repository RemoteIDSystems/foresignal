from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.source_settings_update_egress_type_1 import (
    SourceSettingsUpdateEgressType1,
)
from ..models.source_settings_update_egress_type_2_type_1 import (
    SourceSettingsUpdateEgressType2Type1,
)
from ..models.source_settings_update_egress_type_3_type_1 import (
    SourceSettingsUpdateEgressType3Type1,
)
from ..models.source_settings_update_region_tagging import (
    SourceSettingsUpdateRegionTagging,
)
from ..models.source_settings_update_reliability_type_1 import (
    SourceSettingsUpdateReliabilityType1,
)
from ..models.source_settings_update_reliability_type_2_type_1 import (
    SourceSettingsUpdateReliabilityType2Type1,
)
from ..models.source_settings_update_reliability_type_3_type_1 import (
    SourceSettingsUpdateReliabilityType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.ingest_policy_all import IngestPolicyAll
    from ..models.ingest_policy_latest import IngestPolicyLatest
    from ..models.ingest_policy_sampled import IngestPolicySampled
    from ..models.source_settings_update_params import SourceSettingsUpdateParams


T = TypeVar("T", bound="SourceSettingsUpdate")


@_attrs_define
class SourceSettingsUpdate:
    """Partial update; omitted fields unchanged. retention=null reverts to the default (400 RETENTION_TIER_FIXED on TRACK
    sources); ingestPolicy=null clears the policy override.

        Attributes:
            enabled (bool | Unset):
            interval (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            params (SourceSettingsUpdateParams | Unset):
            budget (Budget | Unset):
            retention (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
            embed (bool | Unset): Embed this source's records for semantic search (027). `true` is refused (400
                VALIDATION_FAILED, field `embed`) when the source declares no embedFields or is private; turning it off deletes
                the source's record documents.
            ingest_policy (IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset): Explicit null clears
                the override; omitted leaves it unchanged. Invalid → 400 INGEST_POLICY_INVALID
            stats_enabled (bool | Unset):
            stats_include_private (bool | Unset):
            private (bool | Unset):
            region_tagging (SourceSettingsUpdateRegionTagging | Unset):
            downsample_interval (None | str | Unset): TRACK sources only (STANDARD → 400 VALIDATION_FAILED, field
                `downsampleInterval`); explicit null clears it. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
                Example: 1m.
            egress (None | SourceSettingsUpdateEgressType1 | SourceSettingsUpdateEgressType2Type1 |
                SourceSettingsUpdateEgressType3Type1 | Unset): Egress override; accepted only when the descriptor is
                `egressOverridable` (400 EGRESS_FIXED otherwise); null reverts to the descriptor
            reliability (None | SourceSettingsUpdateReliabilityType1 | SourceSettingsUpdateReliabilityType2Type1 |
                SourceSettingsUpdateReliabilityType3Type1 | Unset): Admiralty reliability override; null reverts to the
                descriptor default
    """

    enabled: bool | Unset = UNSET
    interval: str | Unset = UNSET
    params: SourceSettingsUpdateParams | Unset = UNSET
    budget: Budget | Unset = UNSET
    retention: None | str | Unset = UNSET
    embed: bool | Unset = UNSET
    ingest_policy: (
        IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset
    ) = UNSET
    stats_enabled: bool | Unset = UNSET
    stats_include_private: bool | Unset = UNSET
    private: bool | Unset = UNSET
    region_tagging: SourceSettingsUpdateRegionTagging | Unset = UNSET
    downsample_interval: None | str | Unset = UNSET
    egress: (
        None
        | SourceSettingsUpdateEgressType1
        | SourceSettingsUpdateEgressType2Type1
        | SourceSettingsUpdateEgressType3Type1
        | Unset
    ) = UNSET
    reliability: (
        None
        | SourceSettingsUpdateReliabilityType1
        | SourceSettingsUpdateReliabilityType2Type1
        | SourceSettingsUpdateReliabilityType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled

        enabled = self.enabled

        interval = self.interval

        params: dict[str, Any] | Unset = UNSET
        if not isinstance(self.params, Unset):
            params = self.params.to_dict()

        budget: dict[str, Any] | Unset = UNSET
        if not isinstance(self.budget, Unset):
            budget = self.budget.to_dict()

        retention: None | str | Unset
        if isinstance(self.retention, Unset):
            retention = UNSET
        else:
            retention = self.retention

        embed = self.embed

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

        stats_enabled = self.stats_enabled

        stats_include_private = self.stats_include_private

        private = self.private

        region_tagging: str | Unset = UNSET
        if not isinstance(self.region_tagging, Unset):
            region_tagging = self.region_tagging.value

        downsample_interval: None | str | Unset
        if isinstance(self.downsample_interval, Unset):
            downsample_interval = UNSET
        else:
            downsample_interval = self.downsample_interval

        egress: None | str | Unset
        if isinstance(self.egress, Unset):
            egress = UNSET
        elif (
            isinstance(self.egress, SourceSettingsUpdateEgressType1)
            or isinstance(self.egress, SourceSettingsUpdateEgressType2Type1)
            or isinstance(self.egress, SourceSettingsUpdateEgressType3Type1)
        ):
            egress = self.egress.value
        else:
            egress = self.egress

        reliability: None | str | Unset
        if isinstance(self.reliability, Unset):
            reliability = UNSET
        elif (
            isinstance(self.reliability, SourceSettingsUpdateReliabilityType1)
            or isinstance(self.reliability, SourceSettingsUpdateReliabilityType2Type1)
            or isinstance(self.reliability, SourceSettingsUpdateReliabilityType3Type1)
        ):
            reliability = self.reliability.value
        else:
            reliability = self.reliability

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if interval is not UNSET:
            field_dict["interval"] = interval
        if params is not UNSET:
            field_dict["params"] = params
        if budget is not UNSET:
            field_dict["budget"] = budget
        if retention is not UNSET:
            field_dict["retention"] = retention
        if embed is not UNSET:
            field_dict["embed"] = embed
        if ingest_policy is not UNSET:
            field_dict["ingestPolicy"] = ingest_policy
        if stats_enabled is not UNSET:
            field_dict["statsEnabled"] = stats_enabled
        if stats_include_private is not UNSET:
            field_dict["statsIncludePrivate"] = stats_include_private
        if private is not UNSET:
            field_dict["private"] = private
        if region_tagging is not UNSET:
            field_dict["regionTagging"] = region_tagging
        if downsample_interval is not UNSET:
            field_dict["downsampleInterval"] = downsample_interval
        if egress is not UNSET:
            field_dict["egress"] = egress
        if reliability is not UNSET:
            field_dict["reliability"] = reliability

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.budget import Budget
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled
        from ..models.source_settings_update_params import SourceSettingsUpdateParams

        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        interval = d.pop("interval", UNSET)

        _params = d.pop("params", UNSET)
        params: SourceSettingsUpdateParams | Unset
        if isinstance(_params, Unset):
            params = UNSET
        else:
            params = SourceSettingsUpdateParams.from_dict(_params)

        _budget = d.pop("budget", UNSET)
        budget: Budget | Unset
        if isinstance(_budget, Unset):
            budget = UNSET
        else:
            budget = Budget.from_dict(_budget)

        def _parse_retention(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        retention = _parse_retention(d.pop("retention", UNSET))

        embed = d.pop("embed", UNSET)

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

        stats_enabled = d.pop("statsEnabled", UNSET)

        stats_include_private = d.pop("statsIncludePrivate", UNSET)

        private = d.pop("private", UNSET)

        _region_tagging = d.pop("regionTagging", UNSET)
        region_tagging: SourceSettingsUpdateRegionTagging | Unset
        if isinstance(_region_tagging, Unset):
            region_tagging = UNSET
        else:
            region_tagging = SourceSettingsUpdateRegionTagging(_region_tagging)

        def _parse_downsample_interval(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        downsample_interval = _parse_downsample_interval(
            d.pop("downsampleInterval", UNSET)
        )

        def _parse_egress(
            data: object,
        ) -> (
            None
            | SourceSettingsUpdateEgressType1
            | SourceSettingsUpdateEgressType2Type1
            | SourceSettingsUpdateEgressType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_1 = SourceSettingsUpdateEgressType1(data)

                return egress_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_2_type_1 = SourceSettingsUpdateEgressType2Type1(data)

                return egress_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_3_type_1 = SourceSettingsUpdateEgressType3Type1(data)

                return egress_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SourceSettingsUpdateEgressType1
                | SourceSettingsUpdateEgressType2Type1
                | SourceSettingsUpdateEgressType3Type1
                | Unset,
                data,
            )

        egress = _parse_egress(d.pop("egress", UNSET))

        def _parse_reliability(
            data: object,
        ) -> (
            None
            | SourceSettingsUpdateReliabilityType1
            | SourceSettingsUpdateReliabilityType2Type1
            | SourceSettingsUpdateReliabilityType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_1 = SourceSettingsUpdateReliabilityType1(data)

                return reliability_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_2_type_1 = SourceSettingsUpdateReliabilityType2Type1(
                    data
                )

                return reliability_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_3_type_1 = SourceSettingsUpdateReliabilityType3Type1(
                    data
                )

                return reliability_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SourceSettingsUpdateReliabilityType1
                | SourceSettingsUpdateReliabilityType2Type1
                | SourceSettingsUpdateReliabilityType3Type1
                | Unset,
                data,
            )

        reliability = _parse_reliability(d.pop("reliability", UNSET))

        source_settings_update = cls(
            enabled=enabled,
            interval=interval,
            params=params,
            budget=budget,
            retention=retention,
            embed=embed,
            ingest_policy=ingest_policy,
            stats_enabled=stats_enabled,
            stats_include_private=stats_include_private,
            private=private,
            region_tagging=region_tagging,
            downsample_interval=downsample_interval,
            egress=egress,
            reliability=reliability,
        )

        source_settings_update.additional_properties = d
        return source_settings_update

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
