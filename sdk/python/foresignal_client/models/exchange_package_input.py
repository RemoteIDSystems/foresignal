from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.exchange_package_input_deliver import ExchangePackageInputDeliver
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.exchange_package_input_extract_type_0 import (
        ExchangePackageInputExtractType0,
    )
    from ..models.package_recipient import PackageRecipient


T = TypeVar("T", bound="ExchangePackageInput")


@_attrs_define
class ExchangePackageInput:
    """Build a signed package of a report, investigation, chronicle extract or evidence

    Attributes:
        object_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        recipients (list[PackageRecipient] | Literal['*']):
        message (None | str | Unset):
        deliver (ExchangePackageInputDeliver | Unset): Push to 113 peers or only build a downloadable file Default:
            ExchangePackageInputDeliver.PEER.
        extract (ExchangePackageInputExtractType0 | None | Unset):
    """

    object_ref: str
    recipients: list[PackageRecipient] | Literal["*"]
    message: None | str | Unset = UNSET
    deliver: ExchangePackageInputDeliver | Unset = ExchangePackageInputDeliver.PEER
    extract: ExchangePackageInputExtractType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.exchange_package_input_extract_type_0 import (
            ExchangePackageInputExtractType0,
        )

        object_ref = self.object_ref

        recipients: list[dict[str, Any]] | Literal["*"]
        if isinstance(self.recipients, list):
            recipients = []
            for recipients_type_0_item_data in self.recipients:
                recipients_type_0_item = recipients_type_0_item_data.to_dict()
                recipients.append(recipients_type_0_item)

        else:
            recipients = self.recipients

        message: None | str | Unset
        if isinstance(self.message, Unset):
            message = UNSET
        else:
            message = self.message

        deliver: str | Unset = UNSET
        if not isinstance(self.deliver, Unset):
            deliver = self.deliver.value

        extract: dict[str, Any] | None | Unset
        if isinstance(self.extract, Unset):
            extract = UNSET
        elif isinstance(self.extract, ExchangePackageInputExtractType0):
            extract = self.extract.to_dict()
        else:
            extract = self.extract

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "objectRef": object_ref,
                "recipients": recipients,
            }
        )
        if message is not UNSET:
            field_dict["message"] = message
        if deliver is not UNSET:
            field_dict["deliver"] = deliver
        if extract is not UNSET:
            field_dict["extract"] = extract

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.exchange_package_input_extract_type_0 import (
            ExchangePackageInputExtractType0,
        )
        from ..models.package_recipient import PackageRecipient

        d = dict(src_dict)
        object_ref = d.pop("objectRef")

        def _parse_recipients(data: object) -> list[PackageRecipient] | Literal["*"]:
            try:
                if not isinstance(data, list):
                    raise TypeError()
                recipients_type_0 = []
                _recipients_type_0 = data
                for recipients_type_0_item_data in _recipients_type_0:
                    recipients_type_0_item = PackageRecipient.from_dict(
                        recipients_type_0_item_data
                    )

                    recipients_type_0.append(recipients_type_0_item)

                return recipients_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            recipients_type_1 = cast(Literal["*"], data)
            if recipients_type_1 != "*":
                raise ValueError(
                    f"recipients_type_1 must match const '*', got '{recipients_type_1}'"
                )
            return recipients_type_1

        recipients = _parse_recipients(d.pop("recipients"))

        def _parse_message(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        message = _parse_message(d.pop("message", UNSET))

        _deliver = d.pop("deliver", UNSET)
        deliver: ExchangePackageInputDeliver | Unset
        if isinstance(_deliver, Unset):
            deliver = UNSET
        else:
            deliver = ExchangePackageInputDeliver(_deliver)

        def _parse_extract(
            data: object,
        ) -> ExchangePackageInputExtractType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                extract_type_0 = ExchangePackageInputExtractType0.from_dict(data)

                return extract_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ExchangePackageInputExtractType0 | None | Unset, data)

        extract = _parse_extract(d.pop("extract", UNSET))

        exchange_package_input = cls(
            object_ref=object_ref,
            recipients=recipients,
            message=message,
            deliver=deliver,
            extract=extract,
        )

        exchange_package_input.additional_properties = d
        return exchange_package_input

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
