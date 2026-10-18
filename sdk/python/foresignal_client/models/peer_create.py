from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.peer_create_mode import PeerCreateMode
from ..models.peer_create_reliability import PeerCreateReliability
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.peer_selection import PeerSelection


T = TypeVar("T", bound="PeerCreate")


@_attrs_define
class PeerCreate:
    """Register a peer instance (113)

    Attributes:
        id (str):
        name (str):
        base_url (str): Checked by the outbound URL policy (035)
        mode (PeerCreateMode | Unset): PULL: consume the peer's feed; RECEIVE: the peer (an edge) pushes here Default:
            PeerCreateMode.PULL.
        enabled (bool | Unset):
        token (str | Unset): The remote instance's token (read + federation); required for PULL
        selection (PeerSelection | Unset): Selective replication from a peer; remote filters are passed as query
            parameters and re-checked locally (113)
        same_operator (bool | Unset): The operator's own hub: LOCAL_ONLY records may be pushed to it (and stay
            LOCAL_ONLY there) Default: False.
        reliability (PeerCreateReliability | Unset): Admiralty source reliability: A completely reliable, B usually
            reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
        exchange_key (str | Unset): The fsp_ ingest key the remote instance issued to this one (its peer row for this
            instance); the Bearer token of POST {baseUrl}/v1/exchange/inbox and /v1/exchange/receipts (132)
    """

    id: str
    name: str
    base_url: str
    mode: PeerCreateMode | Unset = PeerCreateMode.PULL
    enabled: bool | Unset = UNSET
    token: str | Unset = UNSET
    selection: PeerSelection | Unset = UNSET
    same_operator: bool | Unset = False
    reliability: PeerCreateReliability | Unset = UNSET
    exchange_key: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        name = self.name

        base_url = self.base_url

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        enabled = self.enabled

        token = self.token

        selection: dict[str, Any] | Unset = UNSET
        if not isinstance(self.selection, Unset):
            selection = self.selection.to_dict()

        same_operator = self.same_operator

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        exchange_key = self.exchange_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "baseUrl": base_url,
            }
        )
        if mode is not UNSET:
            field_dict["mode"] = mode
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
        if exchange_key is not UNSET:
            field_dict["exchangeKey"] = exchange_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.peer_selection import PeerSelection

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        base_url = d.pop("baseUrl")

        _mode = d.pop("mode", UNSET)
        mode: PeerCreateMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = PeerCreateMode(_mode)

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
        reliability: PeerCreateReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = PeerCreateReliability(_reliability)

        exchange_key = d.pop("exchangeKey", UNSET)

        peer_create = cls(
            id=id,
            name=name,
            base_url=base_url,
            mode=mode,
            enabled=enabled,
            token=token,
            selection=selection,
            same_operator=same_operator,
            reliability=reliability,
            exchange_key=exchange_key,
        )

        peer_create.additional_properties = d
        return peer_create

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
