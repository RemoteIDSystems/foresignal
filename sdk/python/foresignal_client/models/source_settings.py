from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.source_settings_egress_type_1 import SourceSettingsEgressType1
from ..models.source_settings_egress_type_2_type_1 import SourceSettingsEgressType2Type1
from ..models.source_settings_egress_type_3_type_1 import SourceSettingsEgressType3Type1
from ..models.source_settings_region_tagging import SourceSettingsRegionTagging
from ..models.source_settings_reliability_type_1 import SourceSettingsReliabilityType1
from ..models.source_settings_reliability_type_2_type_1 import (
    SourceSettingsReliabilityType2Type1,
)
from ..models.source_settings_reliability_type_3_type_1 import (
    SourceSettingsReliabilityType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.budget import Budget
    from ..models.ingest_policy_all import IngestPolicyAll
    from ..models.ingest_policy_latest import IngestPolicyLatest
    from ..models.ingest_policy_sampled import IngestPolicySampled
    from ..models.source_settings_params import SourceSettingsParams


T = TypeVar("T", bound="SourceSettings")


@_attrs_define
class SourceSettings:
    """
    Attributes:
        enabled (bool):
        interval (str): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        params (SourceSettingsParams):
        budget (Budget):
        credentials_set (bool): True if credentials are stored (values never returned)
        retention (None | str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        embed (bool | Unset): Embed this source's records for semantic search (default true when the source declares
            embedFields)
        updated_at (datetime.datetime | Unset):
        ingest_policy (IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset): Operator override;
            null = descriptor default
        effective_ingest_policy (IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | Unset): Per-source ingest
            policy, discriminated on `mode` (Core-Data-Model §5.1); same JSON shape as `source_settings.ingest_policy`
            Example: {'mode': 'SAMPLED', 'minInterval': '60s', 'keyframe': '240s', 'minDeviationMeters': 1000, 'thresholds':
            True, 'edgeMinInterval': '30s'}.
        effective_retention (None | str | Unset): Effective retention window (TRACK: `retention.trackWindow`). ISO-8601
            duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        stats_enabled (bool | Unset): Include in activity rollups (038) Default: True.
        stats_include_private (bool | Unset): Roll up this private source anyway (038 `stats.includePrivate[sourceId]`)
            Default: False.
        private (bool | Unset): Operator tightening to private (039); a descriptor-private source cannot be made public
        region_tagging (SourceSettingsRegionTagging | Unset): Default eager for STANDARD, lazy for TRACK (040)
        downsample_interval (None | str | Unset): Per-source downsample interval override (TRACK sources only). ISO-8601
            duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 1m.
        egress (None | SourceSettingsEgressType1 | SourceSettingsEgressType2Type1 | SourceSettingsEgressType3Type1 |
            Unset): Operator override of the descriptor egress; only when `egressOverridable` (else 400 EGRESS_FIXED); null
            = descriptor default
        reliability (None | SourceSettingsReliabilityType1 | SourceSettingsReliabilityType2Type1 |
            SourceSettingsReliabilityType3Type1 | Unset): Operator override of the descriptor reliability; null = descriptor
            default
    """

    enabled: bool
    interval: str
    params: SourceSettingsParams
    budget: Budget
    credentials_set: bool
    retention: None | str | Unset = UNSET
    embed: bool | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    ingest_policy: (
        IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | None | Unset
    ) = UNSET
    effective_ingest_policy: (
        IngestPolicyAll | IngestPolicyLatest | IngestPolicySampled | Unset
    ) = UNSET
    effective_retention: None | str | Unset = UNSET
    stats_enabled: bool | Unset = True
    stats_include_private: bool | Unset = False
    private: bool | Unset = UNSET
    region_tagging: SourceSettingsRegionTagging | Unset = UNSET
    downsample_interval: None | str | Unset = UNSET
    egress: (
        None
        | SourceSettingsEgressType1
        | SourceSettingsEgressType2Type1
        | SourceSettingsEgressType3Type1
        | Unset
    ) = UNSET
    reliability: (
        None
        | SourceSettingsReliabilityType1
        | SourceSettingsReliabilityType2Type1
        | SourceSettingsReliabilityType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ingest_policy_all import IngestPolicyAll
        from ..models.ingest_policy_latest import IngestPolicyLatest
        from ..models.ingest_policy_sampled import IngestPolicySampled

        enabled = self.enabled

        interval = self.interval

        params = self.params.to_dict()

        budget = self.budget.to_dict()

        credentials_set = self.credentials_set

        retention: None | str | Unset
        if isinstance(self.retention, Unset):
            retention = UNSET
        else:
            retention = self.retention

        embed = self.embed

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

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

        effective_ingest_policy: dict[str, Any] | Unset
        if isinstance(self.effective_ingest_policy, Unset):
            effective_ingest_policy = UNSET
        elif isinstance(self.effective_ingest_policy, IngestPolicyAll) or isinstance(
            self.effective_ingest_policy, IngestPolicySampled
        ):
            effective_ingest_policy = self.effective_ingest_policy.to_dict()
        else:
            effective_ingest_policy = self.effective_ingest_policy.to_dict()

        effective_retention: None | str | Unset
        if isinstance(self.effective_retention, Unset):
            effective_retention = UNSET
        else:
            effective_retention = self.effective_retention

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
            isinstance(self.egress, SourceSettingsEgressType1)
            or isinstance(self.egress, SourceSettingsEgressType2Type1)
            or isinstance(self.egress, SourceSettingsEgressType3Type1)
        ):
            egress = self.egress.value
        else:
            egress = self.egress

        reliability: None | str | Unset
        if isinstance(self.reliability, Unset):
            reliability = UNSET
        elif (
            isinstance(self.reliability, SourceSettingsReliabilityType1)
            or isinstance(self.reliability, SourceSettingsReliabilityType2Type1)
            or isinstance(self.reliability, SourceSettingsReliabilityType3Type1)
        ):
            reliability = self.reliability.value
        else:
            reliability = self.reliability

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "enabled": enabled,
                "interval": interval,
                "params": params,
                "budget": budget,
                "credentialsSet": credentials_set,
            }
        )
        if retention is not UNSET:
            field_dict["retention"] = retention
        if embed is not UNSET:
            field_dict["embed"] = embed
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if ingest_policy is not UNSET:
            field_dict["ingestPolicy"] = ingest_policy
        if effective_ingest_policy is not UNSET:
            field_dict["effectiveIngestPolicy"] = effective_ingest_policy
        if effective_retention is not UNSET:
            field_dict["effectiveRetention"] = effective_retention
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
        from ..models.source_settings_params import SourceSettingsParams

        d = dict(src_dict)
        enabled = d.pop("enabled")

        interval = d.pop("interval")

        params = SourceSettingsParams.from_dict(d.pop("params"))

        budget = Budget.from_dict(d.pop("budget"))

        credentials_set = d.pop("credentialsSet")

        def _parse_retention(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        retention = _parse_retention(d.pop("retention", UNSET))

        embed = d.pop("embed", UNSET)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

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

        def _parse_effective_ingest_policy(
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

        effective_ingest_policy = _parse_effective_ingest_policy(
            d.pop("effectiveIngestPolicy", UNSET)
        )

        def _parse_effective_retention(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        effective_retention = _parse_effective_retention(
            d.pop("effectiveRetention", UNSET)
        )

        stats_enabled = d.pop("statsEnabled", UNSET)

        stats_include_private = d.pop("statsIncludePrivate", UNSET)

        private = d.pop("private", UNSET)

        _region_tagging = d.pop("regionTagging", UNSET)
        region_tagging: SourceSettingsRegionTagging | Unset
        if isinstance(_region_tagging, Unset):
            region_tagging = UNSET
        else:
            region_tagging = SourceSettingsRegionTagging(_region_tagging)

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
            | SourceSettingsEgressType1
            | SourceSettingsEgressType2Type1
            | SourceSettingsEgressType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_1 = SourceSettingsEgressType1(data)

                return egress_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_2_type_1 = SourceSettingsEgressType2Type1(data)

                return egress_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                egress_type_3_type_1 = SourceSettingsEgressType3Type1(data)

                return egress_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SourceSettingsEgressType1
                | SourceSettingsEgressType2Type1
                | SourceSettingsEgressType3Type1
                | Unset,
                data,
            )

        egress = _parse_egress(d.pop("egress", UNSET))

        def _parse_reliability(
            data: object,
        ) -> (
            None
            | SourceSettingsReliabilityType1
            | SourceSettingsReliabilityType2Type1
            | SourceSettingsReliabilityType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_1 = SourceSettingsReliabilityType1(data)

                return reliability_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_2_type_1 = SourceSettingsReliabilityType2Type1(data)

                return reliability_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                reliability_type_3_type_1 = SourceSettingsReliabilityType3Type1(data)

                return reliability_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | SourceSettingsReliabilityType1
                | SourceSettingsReliabilityType2Type1
                | SourceSettingsReliabilityType3Type1
                | Unset,
                data,
            )

        reliability = _parse_reliability(d.pop("reliability", UNSET))

        source_settings = cls(
            enabled=enabled,
            interval=interval,
            params=params,
            budget=budget,
            credentials_set=credentials_set,
            retention=retention,
            embed=embed,
            updated_at=updated_at,
            ingest_policy=ingest_policy,
            effective_ingest_policy=effective_ingest_policy,
            effective_retention=effective_retention,
            stats_enabled=stats_enabled,
            stats_include_private=stats_include_private,
            private=private,
            region_tagging=region_tagging,
            downsample_interval=downsample_interval,
            egress=egress,
            reliability=reliability,
        )

        source_settings.additional_properties = d
        return source_settings

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
