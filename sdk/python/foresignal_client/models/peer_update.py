from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.peer_update_reliability import PeerUpdateReliability
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.peer_selection import PeerSelection


T = TypeVar("T", bound="PeerUpdate")


@_attrs_define
class PeerUpdate:
    """Replace a peer's settings; omit token to keep it (113)

    Attributes:
        name (str):
        base_url (str):
        enabled (bool | Unset):
        token (str | Unset):
        selection (PeerSelection | Unset): Selective replication from a peer; remote filters are passed as query
            parameters and re-checked locally (113)
        same_operator (bool | Unset):
        reliability (PeerUpdateReliability | Unset): Admiralty source reliability: A completely reliable, B usually
            reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
        pinned_kid (None | str | Unset): Admin re-pin of the remote signing key; null re-pins on the next successful
            /info (TOFU)
        exchange_key (None | str | Unset): Replace the exchange key; omit to keep it, empty or null to remove it (132)
    """

    name: str
    base_url: str
    enabled: bool | Unset = UNSET
    token: str | Unset = UNSET
    selection: PeerSelection | Unset = UNSET
    same_operator: bool | Unset = UNSET
    reliability: PeerUpdateReliability | Unset = UNSET
    pinned_kid: None | str | Unset = UNSET
    exchange_key: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        base_url = self.base_url

        enabled = self.enabled

        token = self.token

        selection: dict[str, Any] | Unset = UNSET
        if not isinstance(self.selection, Unset):
            selection = self.selection.to_dict()

        same_operator = self.same_operator

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        pinned_kid: None | str | Unset
        if isinstance(self.pinned_kid, Unset):
            pinned_kid = UNSET
        else:
            pinned_kid = self.pinned_kid

        exchange_key: None | str | Unset
        if isinstance(self.exchange_key, Unset):
            exchange_key = UNSET
        else:
            exchange_key = self.exchange_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "baseUrl": base_url,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if token is not UNSET:
            field_dict["token"] = token
        if selection is not UNSET:
            field_dict["selection"] = selection
        if same_operator is not UNSET:
            field_dict["sameOperator"] = same_operator
        if reliability is not UNSET:
            field_dict["reliability"] = reliability
        if pinned_kid is not UNSET:
            field_dict["pinnedKid"] = pinned_kid
        if exchange_key is not UNSET:
            field_dict["exchangeKey"] = exchange_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.peer_selection import PeerSelection

        d = dict(src_dict)
        name = d.pop("name")

        base_url = d.pop("baseUrl")

        enabled = d.pop("enabled", UNSET)

        token = d.pop("token", UNSET)

        _selection = d.pop("selection", UNSET)
        selection: PeerSelection | Unset
        if isinstance(_selection, Unset):
            selection = UNSET
        else:
            selection = PeerSelection.from_dict(_selection)

        same_operator = d.pop("sameOperator", UNSET)

        _reliability = d.pop("reliability", UNSET)
        reliability: PeerUpdateReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = PeerUpdateReliability(_reliability)

        def _parse_pinned_kid(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        pinned_kid = _parse_pinned_kid(d.pop("pinnedKid", UNSET))

        def _parse_exchange_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        exchange_key = _parse_exchange_key(d.pop("exchangeKey", UNSET))

        peer_update = cls(
            name=name,
            base_url=base_url,
            enabled=enabled,
            token=token,
            selection=selection,
            same_operator=same_operator,
            reliability=reliability,
            pinned_kid=pinned_kid,
            exchange_key=exchange_key,
        )

        peer_update.additional_properties = d
        return peer_update

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
