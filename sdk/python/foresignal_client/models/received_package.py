from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.received_package_key_source import ReceivedPackageKeySource
from ..models.received_package_received_via import ReceivedPackageReceivedVia
from ..models.received_package_signature_status import ReceivedPackageSignatureStatus
from ..models.received_package_type import ReceivedPackageType
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.provenance_chain_entry import ProvenanceChainEntry
    from ..models.received_package_authors_item import ReceivedPackageAuthorsItem
    from ..models.received_package_contents_item import ReceivedPackageContentsItem
    from ..models.received_package_sender import ReceivedPackageSender


T = TypeVar("T", bound="ReceivedPackage")


@_attrs_define
class ReceivedPackage:
    """A package imported read-only from another instance, with sender, authors, signature status and TLP

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (ReceivedPackageType):
        object_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        title (str):
        sender (ReceivedPackageSender): Sender
        authors (list[ReceivedPackageAuthorsItem]):
        tlp (Tlp): Traffic Light Protocol 2.0 label
        signature_status (ReceivedPackageSignatureStatus):
        hashes_verified (bool):
        received_via (ReceivedPackageReceivedVia):
        received_at (datetime.datetime):
        key_source (ReceivedPackageKeySource | Unset):
        recipients (list[str] | Unset): Local principal handles
        message (None | str | Unset):
        acknowledged_at (datetime.datetime | None | Unset):
        provenance_chain (list[ProvenanceChainEntry] | Unset):
        contents (list[ReceivedPackageContentsItem] | Unset):
    """

    id: str
    package_id: str
    type_: ReceivedPackageType
    object_ref: str
    title: str
    sender: ReceivedPackageSender
    authors: list[ReceivedPackageAuthorsItem]
    tlp: Tlp
    signature_status: ReceivedPackageSignatureStatus
    hashes_verified: bool
    received_via: ReceivedPackageReceivedVia
    received_at: datetime.datetime
    key_source: ReceivedPackageKeySource | Unset = UNSET
    recipients: list[str] | Unset = UNSET
    message: None | str | Unset = UNSET
    acknowledged_at: datetime.datetime | None | Unset = UNSET
    provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
    contents: list[ReceivedPackageContentsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        package_id = self.package_id

        type_ = self.type_.value

        object_ref = self.object_ref

        title = self.title

        sender = self.sender.to_dict()

        authors = []
        for authors_item_data in self.authors:
            authors_item = authors_item_data.to_dict()
            authors.append(authors_item)

        tlp = self.tlp.value

        signature_status = self.signature_status.value

        hashes_verified = self.hashes_verified

        received_via = self.received_via.value

        received_at = self.received_at.isoformat()

        key_source: str | Unset = UNSET
        if not isinstance(self.key_source, Unset):
            key_source = self.key_source.value

        recipients: list[str] | Unset = UNSET
        if not isinstance(self.recipients, Unset):
            recipients = self.recipients

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        acknowledged_at: None | str | Unset
        if isinstance(self.acknowledged_at, Unset):
            acknowledged_at = UNSET
        elif isinstance(self.acknowledged_at, datetime.datetime):
            acknowledged_at = self.acknowledged_at.isoformat()
        else:
            acknowledged_at = self.acknowledged_at

        provenance_chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.provenance_chain, Unset):
            provenance_chain = []
            for provenance_chain_item_data in self.provenance_chain:
                provenance_chain_item = provenance_chain_item_data.to_dict()
                provenance_chain.append(provenance_chain_item)

        contents: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.contents, Unset):
            contents = []
            for contents_item_data in self.contents:
                contents_item = contents_item_data.to_dict()
                contents.append(contents_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "packageId": package_id,
                "type": type_,
                "objectRef": object_ref,
                "title": title,
                "sender": sender,
                "authors": authors,
                "tlp": tlp,
                "signatureStatus": signature_status,
                "hashesVerified": hashes_verified,
                "receivedVia": received_via,
                "receivedAt": received_at,
            }
        )
        if key_source is not UNSET:
            field_dict["keySource"] = key_source
        if recipients is not UNSET:
            field_dict["recipients"] = recipients
        if message is not UNSET:
            field_dict["message"] = message
        if acknowledged_at is not UNSET:
            field_dict["acknowledgedAt"] = acknowledged_at
        if provenance_chain is not UNSET:
            field_dict["provenanceChain"] = provenance_chain
        if contents is not UNSET:
            field_dict["contents"] = contents

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.provenance_chain_entry import ProvenanceChainEntry
        from ..models.received_package_authors_item import ReceivedPackageAuthorsItem
        from ..models.received_package_contents_item import ReceivedPackageContentsItem
        from ..models.received_package_sender import ReceivedPackageSender

        d = dict(src_dict)
        id = d.pop("id")

        package_id = d.pop("packageId")

        type_ = ReceivedPackageType(d.pop("type"))

        object_ref = d.pop("objectRef")

        title = d.pop("title")

        sender = ReceivedPackageSender.from_dict(d.pop("sender"))

        authors = []
        _authors = d.pop("authors")
        for authors_item_data in _authors:
            authors_item = ReceivedPackageAuthorsItem.from_dict(authors_item_data)

            authors.append(authors_item)

        tlp = Tlp(d.pop("tlp"))

        signature_status = ReceivedPackageSignatureStatus(d.pop("signatureStatus"))

        hashes_verified = d.pop("hashesVerified")

        received_via = ReceivedPackageReceivedVia(d.pop("receivedVia"))

        received_at = datetime.datetime.fromisoformat(d.pop("receivedAt"))

        _key_source = d.pop("keySource", UNSET)
        key_source: ReceivedPackageKeySource | Unset
        if isinstance(_key_source, Unset):
            key_source = UNSET
        else:
            key_source = ReceivedPackageKeySource(_key_source)

        recipients = cast(list[str], d.pop("recipients", UNSET))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        def _parse_acknowledged_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                acknowledged_at_type_0 = datetime.datetime.fromisoformat(data)

                return acknowledged_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        acknowledged_at = _parse_acknowledged_at(d.pop("acknowledgedAt", UNSET))

        _provenance_chain = d.pop("provenanceChain", UNSET)
        provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
        if _provenance_chain is not UNSET:
            provenance_chain = []
            for provenance_chain_item_data in _provenance_chain:
                provenance_chain_item = ProvenanceChainEntry.from_dict(
                    provenance_chain_item_data
                )

                provenance_chain.append(provenance_chain_item)

        _contents = d.pop("contents", UNSET)
        contents: list[ReceivedPackageContentsItem] | Unset = UNSET
        if _contents is not UNSET:
            contents = []
            for contents_item_data in _contents:
                contents_item = ReceivedPackageContentsItem.from_dict(
                    contents_item_data
                )

                contents.append(contents_item)

        received_package = cls(
            id=id,
            package_id=package_id,
            type_=type_,
            object_ref=object_ref,
            title=title,
            sender=sender,
            authors=authors,
            tlp=tlp,
            signature_status=signature_status,
            hashes_verified=hashes_verified,
            received_via=received_via,
            received_at=received_at,
            key_source=key_source,
            recipients=recipients,
            message=message,
            acknowledged_at=acknowledged_at,
            provenance_chain=provenance_chain,
            contents=contents,
        )

        received_package.additional_properties = d
        return received_package

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
