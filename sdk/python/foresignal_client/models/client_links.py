from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ClientLinks")


@_attrs_define
class ClientLinks:
    """Deep-link templates clients and notifications use (023). Defaults use the `foresignal://` scheme; operators may set
    verified https links.

        Attributes:
            alert (str | Unset):  Default: 'foresignal://alerts/{id}'.
            record (str | Unset):  Default: 'foresignal://records/{source}/{externalId}'.
            briefing_run (str | Unset):  Default: 'foresignal://briefings/{briefingId}/runs/{id}'.
            pair (str | Unset):  Default: 'foresignal://pair?u={publicBaseUrl}&c={code}'.
            share (None | str | Unset): A share link opened in a client (112); placeholder {key}. No default: `?open=1` on a
                share-link URL redirects only when this is set.
    """

    alert: str | Unset = "foresignal://alerts/{id}"
    record: str | Unset = "foresignal://records/{source}/{externalId}"
    briefing_run: str | Unset = "foresignal://briefings/{briefingId}/runs/{id}"
    pair: str | Unset = "foresignal://pair?u={publicBaseUrl}&c={code}"
    share: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert = self.alert

        record = self.record

        briefing_run = self.briefing_run

        pair = self.pair

        share: None | str | Unset
        if isinstance(self.share, Unset):
            share = UNSET
        else:
            share = self.share

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if alert is not UNSET:
            field_dict["alert"] = alert
        if record is not UNSET:
            field_dict["record"] = record
        if briefing_run is not UNSET:
            field_dict["briefingRun"] = briefing_run
        if pair is not UNSET:
            field_dict["pair"] = pair
        if share is not UNSET:
            field_dict["share"] = share

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        alert = d.pop("alert", UNSET)

        record = d.pop("record", UNSET)

        briefing_run = d.pop("briefingRun", UNSET)

        pair = d.pop("pair", UNSET)

        def _parse_share(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        share = _parse_share(d.pop("share", UNSET))

        client_links = cls(
            alert=alert,
            record=record,
            briefing_run=briefing_run,
            pair=pair,
            share=share,
        )

        client_links.additional_properties = d
        return client_links

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
