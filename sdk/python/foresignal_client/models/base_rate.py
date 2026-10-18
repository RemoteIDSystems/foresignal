from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.base_rate_origin import BaseRateOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.base_rate_definition import BaseRateDefinition
    from ..models.base_rate_licences_item import BaseRateLicencesItem
    from ..models.base_rate_source_mix import BaseRateSourceMix
    from ..models.base_rate_unconditional_type_0 import BaseRateUnconditionalType0
    from ..models.caveat import Caveat
    from ..models.detector_ref import DetectorRef
    from ..models.region_profile import RegionProfile
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="BaseRate")


@_attrs_define
class BaseRate:
    """A base rate with its reference class, period, sample size, interval, sources and caveats

    Attributes:
        id (None | str): Null for ad-hoc query results that were not stored
        definition (BaseRateDefinition): Reference class of a base rate: family, regions or region class, window and
            optional condition
        reference_class (str): Plain-language reference class
        rate_per_window (float): Mean events per window
        p_at_least_one (float):
        interval (StatInterval): A two-sided statistical interval for a rate, proportion or difference
        n_windows (int):
        n_events (int):
        period_from (datetime.date):
        period_to (datetime.date):
        source_mix (BaseRateSourceMix): Events per source, e.g. `{foresignal: 12, ucdp: 340}`
        caveats (list[Caveat]): `SMALL_SAMPLE`, `LOW_REPORTING_DENSITY`, `MIXED_SOURCES`, `LOW_DETECTABILITY`, and
            `LOCAL_ONLY_WITHHELD` when the per-source counts of a LOCAL_ONLY source are not shown on the path
        computer (DetectorRef):
        computed_at (datetime.datetime):
        origin (BaseRateOrigin):
        manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (None | str | Unset):
        licences (list[BaseRateLicencesItem] | Unset):
        unconditional (BaseRateUnconditionalType0 | None | Unset):
        region_profiles (list[RegionProfile] | Unset):
        cached (bool | Unset): Ad-hoc queries: served from the 24 h cache
    """

    id: None | str
    definition: BaseRateDefinition
    reference_class: str
    rate_per_window: float
    p_at_least_one: float
    interval: StatInterval
    n_windows: int
    n_events: int
    period_from: datetime.date
    period_to: datetime.date
    source_mix: BaseRateSourceMix
    caveats: list[Caveat]
    computer: DetectorRef
    computed_at: datetime.datetime
    origin: BaseRateOrigin
    manifest_id: str
    name: None | str | Unset = UNSET
    licences: list[BaseRateLicencesItem] | Unset = UNSET
    unconditional: BaseRateUnconditionalType0 | None | Unset = UNSET
    region_profiles: list[RegionProfile] | Unset = UNSET
    cached: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.base_rate_unconditional_type_0 import BaseRateUnconditionalType0

        id: None | str
        id = self.id

        definition = self.definition.to_dict()

        reference_class = self.reference_class

        rate_per_window = self.rate_per_window

        p_at_least_one = self.p_at_least_one

        interval = self.interval.to_dict()

        n_windows = self.n_windows

        n_events = self.n_events

        period_from = self.period_from.isoformat()

        period_to = self.period_to.isoformat()

        source_mix = self.source_mix.to_dict()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        computer = self.computer.to_dict()

        computed_at = self.computed_at.isoformat()

        origin = self.origin.value

        manifest_id = self.manifest_id

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        licences: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.licences, Unset):
            licences = []
            for licences_item_data in self.licences:
                licences_item = licences_item_data.to_dict()
                licences.append(licences_item)

        unconditional: dict[str, Any] | None | Unset
        if isinstance(self.unconditional, Unset):
            unconditional = UNSET
        elif isinstance(self.unconditional, BaseRateUnconditionalType0):
            unconditional = self.unconditional.to_dict()
        else:
            unconditional = self.unconditional

        region_profiles: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.region_profiles, Unset):
            region_profiles = []
            for region_profiles_item_data in self.region_profiles:
                region_profiles_item = region_profiles_item_data.to_dict()
                region_profiles.append(region_profiles_item)

        cached = self.cached

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "definition": definition,
                "referenceClass": reference_class,
                "ratePerWindow": rate_per_window,
                "pAtLeastOne": p_at_least_one,
                "interval": interval,
                "nWindows": n_windows,
                "nEvents": n_events,
                "periodFrom": period_from,
                "periodTo": period_to,
                "sourceMix": source_mix,
                "caveats": caveats,
                "computer": computer,
                "computedAt": computed_at,
                "origin": origin,
                "manifestId": manifest_id,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if licences is not UNSET:
            field_dict["licences"] = licences
        if unconditional is not UNSET:
            field_dict["unconditional"] = unconditional
        if region_profiles is not UNSET:
            field_dict["regionProfiles"] = region_profiles
        if cached is not UNSET:
            field_dict["cached"] = cached

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.base_rate_definition import BaseRateDefinition
        from ..models.base_rate_licences_item import BaseRateLicencesItem
        from ..models.base_rate_source_mix import BaseRateSourceMix
        from ..models.base_rate_unconditional_type_0 import BaseRateUnconditionalType0
        from ..models.caveat import Caveat
        from ..models.detector_ref import DetectorRef
        from ..models.region_profile import RegionProfile
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)

        def _parse_id(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        id = _parse_id(d.pop("id"))

        definition = BaseRateDefinition.from_dict(d.pop("definition"))

        reference_class = d.pop("referenceClass")

        rate_per_window = d.pop("ratePerWindow")

        p_at_least_one = d.pop("pAtLeastOne")

        interval = StatInterval.from_dict(d.pop("interval"))

        n_windows = d.pop("nWindows")

        n_events = d.pop("nEvents")

        period_from = datetime.date.fromisoformat(d.pop("periodFrom"))

        period_to = datetime.date.fromisoformat(d.pop("periodTo"))

        source_mix = BaseRateSourceMix.from_dict(d.pop("sourceMix"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        computer = DetectorRef.from_dict(d.pop("computer"))

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        origin = BaseRateOrigin(d.pop("origin"))

        manifest_id = d.pop("manifestId")

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _licences = d.pop("licences", UNSET)
        licences: list[BaseRateLicencesItem] | Unset = UNSET
        if _licences is not UNSET:
            licences = []
            for licences_item_data in _licences:
                licences_item = BaseRateLicencesItem.from_dict(licences_item_data)

                licences.append(licences_item)

        def _parse_unconditional(
            data: object,
        ) -> BaseRateUnconditionalType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                unconditional_type_0 = BaseRateUnconditionalType0.from_dict(data)

                return unconditional_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(BaseRateUnconditionalType0 | None | Unset, data)

        unconditional = _parse_unconditional(d.pop("unconditional", UNSET))

        _region_profiles = d.pop("regionProfiles", UNSET)
        region_profiles: list[RegionProfile] | Unset = UNSET
        if _region_profiles is not UNSET:
            region_profiles = []
            for region_profiles_item_data in _region_profiles:
                region_profiles_item = RegionProfile.from_dict(
                    region_profiles_item_data
                )

                region_profiles.append(region_profiles_item)

        cached = d.pop("cached", UNSET)

        base_rate = cls(
            id=id,
            definition=definition,
            reference_class=reference_class,
            rate_per_window=rate_per_window,
            p_at_least_one=p_at_least_one,
            interval=interval,
            n_windows=n_windows,
            n_events=n_events,
            period_from=period_from,
            period_to=period_to,
            source_mix=source_mix,
            caveats=caveats,
            computer=computer,
            computed_at=computed_at,
            origin=origin,
            manifest_id=manifest_id,
            name=name,
            licences=licences,
            unconditional=unconditional,
            region_profiles=region_profiles,
            cached=cached,
        )

        base_rate.additional_properties = d
        return base_rate

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
