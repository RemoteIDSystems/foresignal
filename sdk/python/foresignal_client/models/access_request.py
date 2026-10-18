from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.access_request_direction import AccessRequestDirection
from ..models.access_request_status import AccessRequestStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.access_request_grant_type_0 import AccessRequestGrantType0
    from ..models.access_request_offer_share_type_0 import AccessRequestOfferShareType0


T = TypeVar("T", bound="AccessRequest")


@_attrs_define
class AccessRequest:
    """A request for access to a related chronicle; reveals only the requester's handle, message and optional share offer

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        direction (AccessRequestDirection):
        requester_handle (str):
        status (AccessRequestStatus):
        created_at (datetime.datetime):
        chronicle_id (None | str | Unset): Target chronicle (incoming requests only)
        requester_instance_id (None | str | Unset):
        message (None | str | Unset):
        offer_share (AccessRequestOfferShareType0 | None | Unset):
        decided_at (datetime.datetime | None | Unset):
        grant (AccessRequestGrantType0 | None | Unset):
    """

    id: str
    link_id: str
    direction: AccessRequestDirection
    requester_handle: str
    status: AccessRequestStatus
    created_at: datetime.datetime
    chronicle_id: None | str | Unset = UNSET
    requester_instance_id: None | str | Unset = UNSET
    message: None | str | Unset = UNSET
    offer_share: AccessRequestOfferShareType0 | None | Unset = UNSET
    decided_at: datetime.datetime | None | Unset = UNSET
    grant: AccessRequestGrantType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.access_request_grant_type_0 import AccessRequestGrantType0
        from ..models.access_request_offer_share_type_0 import (
            AccessRequestOfferShareType0,
        )

        id = self.id

        link_id = self.link_id

        direction = self.direction.value

        requester_handle = self.requester_handle

        status = self.status.value

        created_at = self.created_at.isoformat()

        chronicle_id: None | str | Unset
        if isinstance(self.chronicle_id, Unset):
            chronicle_id = UNSET
        else:
            chronicle_id = self.chronicle_id

        requester_instance_id: None | str | Unset
        if isinstance(self.requester_instance_id, Unset):
            requester_instance_id = UNSET
        else:
            requester_instance_id = self.requester_instance_id

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        offer_share: dict[str, Any] | None | Unset
        if isinstance(self.offer_share, Unset):
            offer_share = UNSET
        elif isinstance(self.offer_share, AccessRequestOfferShareType0):
            offer_share = self.offer_share.to_dict()
        else:
            offer_share = self.offer_share

        decided_at: None | str | Unset
        if isinstance(self.decided_at, Unset):
            decided_at = UNSET
        elif isinstance(self.decided_at, datetime.datetime):
            decided_at = self.decided_at.isoformat()
        else:
            decided_at = self.decided_at

        grant: dict[str, Any] | None | Unset
        if isinstance(self.grant, Unset):
            grant = UNSET
        elif isinstance(self.grant, AccessRequestGrantType0):
            grant = self.grant.to_dict()
        else:
            grant = self.grant

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "linkId": link_id,
                "direction": direction,
                "requesterHandle": requester_handle,
                "status": status,
                "createdAt": created_at,
            }
        )
        if chronicle_id is not UNSET:
            field_dict["chronicleId"] = chronicle_id
        if requester_instance_id is not UNSET:
            field_dict["requesterInstanceId"] = requester_instance_id
        if message is not UNSET:
            field_dict["message"] = message
        if offer_share is not UNSET:
            field_dict["offerShare"] = offer_share
        if decided_at is not UNSET:
            field_dict["decidedAt"] = decided_at
        if grant is not UNSET:
            field_dict["grant"] = grant

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.access_request_grant_type_0 import AccessRequestGrantType0
        from ..models.access_request_offer_share_type_0 import (
            AccessRequestOfferShareType0,
        )

        d = dict(src_dict)
        id = d.pop("id")

        link_id = d.pop("linkId")

        direction = AccessRequestDirection(d.pop("direction"))

        requester_handle = d.pop("requesterHandle")

        status = AccessRequestStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_chronicle_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chronicle_id = _parse_chronicle_id(d.pop("chronicleId", UNSET))

        def _parse_requester_instance_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        requester_instance_id = _parse_requester_instance_id(
            d.pop("requesterInstanceId", UNSET)
        )

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_offer_share(
            data: object,
        ) -> AccessRequestOfferShareType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                offer_share_type_0 = AccessRequestOfferShareType0.from_dict(data)

                return offer_share_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AccessRequestOfferShareType0 | None | Unset, data)

        offer_share = _parse_offer_share(d.pop("offerShare", UNSET))

        def _parse_decided_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                decided_at_type_0 = datetime.datetime.fromisoformat(data)

                return decided_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        decided_at = _parse_decided_at(d.pop("decidedAt", UNSET))

        def _parse_grant(data: object) -> AccessRequestGrantType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                grant_type_0 = AccessRequestGrantType0.from_dict(data)

                return grant_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AccessRequestGrantType0 | None | Unset, data)

        grant = _parse_grant(d.pop("grant", UNSET))

        access_request = cls(
            id=id,
            link_id=link_id,
            direction=direction,
            requester_handle=requester_handle,
            status=status,
            created_at=created_at,
            chronicle_id=chronicle_id,
            requester_instance_id=requester_instance_id,
            message=message,
            offer_share=offer_share,
            decided_at=decided_at,
            grant=grant,
        )

        access_request.additional_properties = d
        return access_request

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
