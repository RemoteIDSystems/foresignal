from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.delivery_target_format import DeliveryTargetFormat
from ..models.delivery_target_kind import DeliveryTargetKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.delivery_target_options import DeliveryTargetOptions


T = TypeVar("T", bound="DeliveryTarget")


@_attrs_define
class DeliveryTarget:
    """Where an edition run goes and in what shape (NP-004). `channel` sends through a 018 notify channel; `website` is the
    hosted edition site (`getEditionSite`, NP-005); `podcast` is the briefing podcast feed (`getBriefingPodcast`, 141).

        Attributes:
            kind (DeliveryTargetKind):
            format_ (DeliveryTargetFormat): The payload shape: `email` the HTML edition with the audio attached and a link
                to the site; `push` a headline, summary and link to the site, never the full body; `alert` the alert-
                notification shape; `html` the standalone page (`website`); `audio` the synthesized edition (`podcast`)
            channel_id (None | Unset | UUID): The notify channel (`listChannels`); required for `channel`, null otherwise
            options (DeliveryTargetOptions | Unset): Per-kind options, passed to the channel's renderer as they are
    """

    kind: DeliveryTargetKind
    format_: DeliveryTargetFormat
    channel_id: None | Unset | UUID = UNSET
    options: DeliveryTargetOptions | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        kind = self.kind.value

        format_ = self.format_.value

        channel_id: None | str | Unset
        if isinstance(self.channel_id, Unset):
            channel_id = UNSET
        elif isinstance(self.channel_id, UUID):
            channel_id = str(self.channel_id)
        else:
            channel_id = self.channel_id

        options: dict[str, Any] | Unset = UNSET
        if not isinstance(self.options, Unset):
            options = self.options.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "format": format_,
            }
        )
        if channel_id is not UNSET:
            field_dict["channelId"] = channel_id
        if options is not UNSET:
            field_dict["options"] = options

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.delivery_target_options import DeliveryTargetOptions

        d = dict(src_dict)
        kind = DeliveryTargetKind(d.pop("kind"))

        format_ = DeliveryTargetFormat(d.pop("format"))

        def _parse_channel_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                channel_id_type_0 = UUID(data)

                return channel_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        channel_id = _parse_channel_id(d.pop("channelId", UNSET))

        _options = d.pop("options", UNSET)
        options: DeliveryTargetOptions | Unset
        if isinstance(_options, Unset):
            options = UNSET
        else:
            options = DeliveryTargetOptions.from_dict(_options)

        delivery_target = cls(
            kind=kind,
            format_=format_,
            channel_id=channel_id,
            options=options,
        )

        delivery_target.additional_properties = d
        return delivery_target

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
