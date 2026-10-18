from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_revision_cause import HypothesisRevisionCause
from ..models.hypothesis_revision_rationale_origin import (
    HypothesisRevisionRationaleOrigin,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_probability_state import HypothesisProbabilityState
    from ..models.revision_driver import RevisionDriver


T = TypeVar("T", bound="HypothesisRevision")


@_attrs_define
class HypothesisRevision:
    """Append-only revision: the only way probabilities change (ICD 203 standard 7)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime):
        cause (HypothesisRevisionCause):
        actor (str): Principal, `indicator-bayes` or component id
        as_of_seq (int):
        before (list[HypothesisProbabilityState]):
        after (list[HypothesisProbabilityState]):
        drivers (list[RevisionDriver]):
        rationale (str):
        updater_version (str):  Example: indicator-bayes 1.0.0.
        citations (list[str] | Unset):
        rationale_origin (HypothesisRevisionRationaleOrigin | Unset):
        band_changed (bool | Unset):
        override (bool | Unset): Includes a logged operator override
        ledger_forecast_id (None | str | Unset):
    """

    id: str
    set_id: str
    at: datetime.datetime
    cause: HypothesisRevisionCause
    actor: str
    as_of_seq: int
    before: list[HypothesisProbabilityState]
    after: list[HypothesisProbabilityState]
    drivers: list[RevisionDriver]
    rationale: str
    updater_version: str
    citations: list[str] | Unset = UNSET
    rationale_origin: HypothesisRevisionRationaleOrigin | Unset = UNSET
    band_changed: bool | Unset = UNSET
    override: bool | Unset = UNSET
    ledger_forecast_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        set_id = self.set_id

        at = self.at.isoformat()

        cause = self.cause.value

        actor = self.actor

        as_of_seq = self.as_of_seq

        before = []
        for before_item_data in self.before:
            before_item = before_item_data.to_dict()
            before.append(before_item)

        after = []
        for after_item_data in self.after:
            after_item = after_item_data.to_dict()
            after.append(after_item)

        drivers = []
        for drivers_item_data in self.drivers:
            drivers_item = drivers_item_data.to_dict()
            drivers.append(drivers_item)

        rationale = self.rationale

        updater_version = self.updater_version

        citations: list[str] | Unset = UNSET
        if not isinstance(self.citations, Unset):
            citations = self.citations

        rationale_origin: str | Unset = UNSET
        if not isinstance(self.rationale_origin, Unset):
            rationale_origin = self.rationale_origin.value

        band_changed = self.band_changed

        override = self.override

        ledger_forecast_id: None | str | Unset
        if isinstance(self.ledger_forecast_id, Unset):
            ledger_forecast_id = UNSET
        else:
            ledger_forecast_id = self.ledger_forecast_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "at": at,
                "cause": cause,
                "actor": actor,
                "asOfSeq": as_of_seq,
                "before": before,
                "after": after,
                "drivers": drivers,
                "rationale": rationale,
                "updaterVersion": updater_version,
            }
        )
        if citations is not UNSET:
            field_dict["citations"] = citations
        if rationale_origin is not UNSET:
            field_dict["rationaleOrigin"] = rationale_origin
        if band_changed is not UNSET:
            field_dict["bandChanged"] = band_changed
        if override is not UNSET:
            field_dict["override"] = override
        if ledger_forecast_id is not UNSET:
            field_dict["ledgerForecastId"] = ledger_forecast_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_probability_state import HypothesisProbabilityState
        from ..models.revision_driver import RevisionDriver

        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        cause = HypothesisRevisionCause(d.pop("cause"))

        actor = d.pop("actor")

        as_of_seq = d.pop("asOfSeq")

        before = []
        _before = d.pop("before")
        for before_item_data in _before:
            before_item = HypothesisProbabilityState.from_dict(before_item_data)

            before.append(before_item)

        after = []
        _after = d.pop("after")
        for after_item_data in _after:
            after_item = HypothesisProbabilityState.from_dict(after_item_data)

            after.append(after_item)

        drivers = []
        _drivers = d.pop("drivers")
        for drivers_item_data in _drivers:
            drivers_item = RevisionDriver.from_dict(drivers_item_data)

            drivers.append(drivers_item)

        rationale = d.pop("rationale")

        updater_version = d.pop("updaterVersion")

        citations = cast(list[str], d.pop("citations", UNSET))

        _rationale_origin = d.pop("rationaleOrigin", UNSET)
        rationale_origin: HypothesisRevisionRationaleOrigin | Unset
        if isinstance(_rationale_origin, Unset):
            rationale_origin = UNSET
        else:
            rationale_origin = HypothesisRevisionRationaleOrigin(_rationale_origin)

        band_changed = d.pop("bandChanged", UNSET)

        override = d.pop("override", UNSET)

        def _parse_ledger_forecast_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ledger_forecast_id = _parse_ledger_forecast_id(d.pop("ledgerForecastId", UNSET))

        hypothesis_revision = cls(
            id=id,
            set_id=set_id,
            at=at,
            cause=cause,
            actor=actor,
            as_of_seq=as_of_seq,
            before=before,
            after=after,
            drivers=drivers,
            rationale=rationale,
            updater_version=updater_version,
            citations=citations,
            rationale_origin=rationale_origin,
            band_changed=band_changed,
            override=override,
            ledger_forecast_id=ledger_forecast_id,
        )

        hypothesis_revision.additional_properties = d
        return hypothesis_revision

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
