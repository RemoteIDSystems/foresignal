from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DigestNotableItem")


@_attrs_define
class DigestNotableItem:
    """A notable current record in a digest (029)

    Attributes:
        id (str): <sourceId>:<externalId>
        seq (int): Persisted head seq (cite with it)
        source (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        title (None | str | Unset):
        live (bool | Unset):
        observed_at (datetime.datetime | Unset):
        effective_time (datetime.datetime | None | Unset):
        rank_value (float | None | str | Unset): Value of the descriptor's rankField when present
        link (str | Unset): publicBaseUrl/v1/records/{source}/{externalId}
    """

    id: str
    seq: int
    source: str | Unset = UNSET
    external_id: str | Unset = UNSET
    kind: str | Unset = UNSET
    title: None | str | Unset = UNSET
    live: bool | Unset = UNSET
    observed_at: datetime.datetime | Unset = UNSET
    effective_time: datetime.datetime | None | Unset = UNSET
    rank_value: float | None | str | Unset = UNSET
    link: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        seq = self.seq

        source = self.source

        external_id = self.external_id

        kind = self.kind

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        live = self.live

        observed_at: str | Unset = UNSET
        if not isinstance(self.observed_at, Unset):
            observed_at = self.observed_at.isoformat()

        effective_time: None | str | Unset
        if isinstance(self.effective_time, Unset):
            effective_time = UNSET
        elif isinstance(self.effective_time, datetime.datetime):
            effective_time = self.effective_time.isoformat()
        else:
            effective_time = self.effective_time

        rank_value: float | None | str | Unset
        if isinstance(self.rank_value, Unset):
            rank_value = UNSET
        else:
            rank_value = self.rank_value

        link = self.link

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "seq": seq,
            }
        )
        if source is not UNSET:
            field_dict["source"] = source
        if external_id is not UNSET:
            field_dict["externalId"] = external_id
        if kind is not UNSET:
            field_dict["kind"] = kind
        if title is not UNSET:
            field_dict["title"] = title
        if live is not UNSET:
            field_dict["live"] = live
        if observed_at is not UNSET:
            field_dict["observedAt"] = observed_at
        if effective_time is not UNSET:
            field_dict["effectiveTime"] = effective_time
        if rank_value is not UNSET:
            field_dict["rankValue"] = rank_value
        if link is not UNSET:
            field_dict["link"] = link

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        seq = d.pop("seq")

        source = d.pop("source", UNSET)

        external_id = d.pop("externalId", UNSET)

        kind = d.pop("kind", UNSET)

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        live = d.pop("live", UNSET)

        _observed_at = d.pop("observedAt", UNSET)
        observed_at: datetime.datetime | Unset
        if isinstance(_observed_at, Unset):
            observed_at = UNSET
        else:
            observed_at = datetime.datetime.fromisoformat(_observed_at)

        def _parse_effective_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effective_time_type_0 = datetime.datetime.fromisoformat(data)

                return effective_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        effective_time = _parse_effective_time(d.pop("effectiveTime", UNSET))

        def _parse_rank_value(data: object) -> float | None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | str | Unset, data)

        rank_value = _parse_rank_value(d.pop("rankValue", UNSET))

        link = d.pop("link", UNSET)

        digest_notable_item = cls(
            id=id,
            seq=seq,
            source=source,
            external_id=external_id,
            kind=kind,
            title=title,
            live=live,
            observed_at=observed_at,
            effective_time=effective_time,
            rank_value=rank_value,
            link=link,
        )

        digest_notable_item.additional_properties = d
        return digest_notable_item

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
