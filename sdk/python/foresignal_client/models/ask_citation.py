from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ask_citation_type import AskCitationType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_citation_corroboration_type_0 import AskCitationCorroborationType0
    from ..models.ask_citation_provenance_type_0 import AskCitationProvenanceType0


T = TypeVar("T", bound="AskCitation")


@_attrs_define
class AskCitation:
    """
    Attributes:
        id (str): <sourceId>:<externalId>, briefing:<id>, alert:<id>, or (056) wx:<productId>@<frameKey> for a weather
            frame and wx:<productId>@<run>/<step> for a forecast step (GFS and HRRR providers: wx:gfs@<run>/<step>, NWS:
            wx:nws@<updatedAt>/<hours>)
        title (str):
        seq (int | None | Unset):
        type_ (AskCitationType | Unset): weather (056): a value a weather tool returned; productId, frameKey, run, step,
            validTime and ageMinutes are set only for this type
        product_id (None | str | Unset):
        frame_key (None | str | Unset):
        run (None | str | Unset):
        step (int | None | Unset):
        valid_time (datetime.datetime | None | Unset):
        age_minutes (float | None | Unset):
        provenance (AskCitationProvenanceType0 | None | Unset): Briefing-run citations of a briefing with
            `includeProvenance` only: why the item is in the edition (NP-007); absent elsewhere
        corroboration (AskCitationCorroborationType0 | None | Unset): Briefing-run citations of a briefing with
            `includeCorroboration` only: how well-supported the item is (NP-007), from the 154 corroboration policy; absent
            elsewhere
    """

    id: str
    title: str
    seq: int | None | Unset = UNSET
    type_: AskCitationType | Unset = UNSET
    product_id: None | str | Unset = UNSET
    frame_key: None | str | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    valid_time: datetime.datetime | None | Unset = UNSET
    age_minutes: float | None | Unset = UNSET
    provenance: AskCitationProvenanceType0 | None | Unset = UNSET
    corroboration: AskCitationCorroborationType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ask_citation_corroboration_type_0 import (
            AskCitationCorroborationType0,
        )
        from ..models.ask_citation_provenance_type_0 import AskCitationProvenanceType0

        id = self.id

        title = self.title

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

        product_id: None | str | Unset
        if isinstance(self.product_id, Unset):
            product_id = UNSET
        else:
            product_id = self.product_id

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        step: int | None | Unset
        if isinstance(self.step, Unset):
            step = UNSET
        else:
            step = self.step

        valid_time: None | str | Unset
        if isinstance(self.valid_time, Unset):
            valid_time = UNSET
        elif isinstance(self.valid_time, datetime.datetime):
            valid_time = self.valid_time.isoformat()
        else:
            valid_time = self.valid_time

        age_minutes: float | None | Unset
        if isinstance(self.age_minutes, Unset):
            age_minutes = UNSET
        else:
            age_minutes = self.age_minutes

        provenance: dict[str, Any] | None | Unset
        if isinstance(self.provenance, Unset):
            provenance = UNSET
        elif isinstance(self.provenance, AskCitationProvenanceType0):
            provenance = self.provenance.to_dict()
        else:
            provenance = self.provenance

        corroboration: dict[str, Any] | None | Unset
        if isinstance(self.corroboration, Unset):
            corroboration = UNSET
        elif isinstance(self.corroboration, AskCitationCorroborationType0):
            corroboration = self.corroboration.to_dict()
        else:
            corroboration = self.corroboration

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "title": title,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if type_ is not UNSET:
            field_dict["type"] = type_
        if product_id is not UNSET:
            field_dict["productId"] = product_id
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time
        if age_minutes is not UNSET:
            field_dict["ageMinutes"] = age_minutes
        if provenance is not UNSET:
            field_dict["provenance"] = provenance
        if corroboration is not UNSET:
            field_dict["corroboration"] = corroboration

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_citation_corroboration_type_0 import (
            AskCitationCorroborationType0,
        )
        from ..models.ask_citation_provenance_type_0 import AskCitationProvenanceType0

        d = dict(src_dict)
        id = d.pop("id")

        title = d.pop("title")

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        _type_ = d.pop("type", UNSET)
        type_: AskCitationType | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = AskCitationType(_type_)

        def _parse_product_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        product_id = _parse_product_id(d.pop("productId", UNSET))

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        step = _parse_step(d.pop("step", UNSET))

        def _parse_valid_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                valid_time_type_0 = datetime.datetime.fromisoformat(data)

                return valid_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        valid_time = _parse_valid_time(d.pop("validTime", UNSET))

        def _parse_age_minutes(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        age_minutes = _parse_age_minutes(d.pop("ageMinutes", UNSET))

        def _parse_provenance(
            data: object,
        ) -> AskCitationProvenanceType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                provenance_type_0 = AskCitationProvenanceType0.from_dict(data)

                return provenance_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AskCitationProvenanceType0 | None | Unset, data)

        provenance = _parse_provenance(d.pop("provenance", UNSET))

        def _parse_corroboration(
            data: object,
        ) -> AskCitationCorroborationType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                corroboration_type_0 = AskCitationCorroborationType0.from_dict(data)

                return corroboration_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AskCitationCorroborationType0 | None | Unset, data)

        corroboration = _parse_corroboration(d.pop("corroboration", UNSET))

        ask_citation = cls(
            id=id,
            title=title,
            seq=seq,
            type_=type_,
            product_id=product_id,
            frame_key=frame_key,
            run=run,
            step=step,
            valid_time=valid_time,
            age_minutes=age_minutes,
            provenance=provenance,
            corroboration=corroboration,
        )

        ask_citation.additional_properties = d
        return ask_citation

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
