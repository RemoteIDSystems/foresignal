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

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_external_stream_selector_filter_type_0_item import (
        ChronicleExternalStreamSelectorFilterType0Item,
    )


T = TypeVar("T", bound="ChronicleExternalStreamSelector")


@_attrs_define
class ChronicleExternalStreamSelector:
    """Chronicle layer capturing an external stream (138)

    Attributes:
        type_ (Literal['external-stream']):
        source_id (str):
        stream_id (str):
        filter_ (list[ChronicleExternalStreamSelectorFilterType0Item] | None | Unset):
    """

    type_: Literal["external-stream"]
    source_id: str
    stream_id: str
    filter_: list[ChronicleExternalStreamSelectorFilterType0Item] | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        source_id = self.source_id

        stream_id = self.stream_id

        filter_: list[dict[str, Any]] | None | Unset
        if isinstance(self.filter_, Unset):
            filter_ = UNSET
        elif isinstance(self.filter_, list):
            filter_ = []
            for filter_type_0_item_data in self.filter_:
                filter_type_0_item = filter_type_0_item_data.to_dict()
                filter_.append(filter_type_0_item)

        else:
            filter_ = self.filter_

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "sourceId": source_id,
                "streamId": stream_id,
            }
        )
        if filter_ is not UNSET:
            field_dict["filter"] = filter_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_external_stream_selector_filter_type_0_item import (
            ChronicleExternalStreamSelectorFilterType0Item,
        )

        d = dict(src_dict)
        type_ = cast(Literal["external-stream"], d.pop("type"))
        if type_ != "external-stream":
            raise ValueError(f"type must match const 'external-stream', got '{type_}'")

        source_id = d.pop("sourceId")

        stream_id = d.pop("streamId")

        def _parse_filter_(
            data: object,
        ) -> list[ChronicleExternalStreamSelectorFilterType0Item] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                filter_type_0 = []
                _filter_type_0 = data
                for filter_type_0_item_data in _filter_type_0:
                    filter_type_0_item = (
                        ChronicleExternalStreamSelectorFilterType0Item.from_dict(
                            filter_type_0_item_data
                        )
                    )

                    filter_type_0.append(filter_type_0_item)

                return filter_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                list[ChronicleExternalStreamSelectorFilterType0Item] | None | Unset,
                data,
            )

        filter_ = _parse_filter_(d.pop("filter", UNSET))

        chronicle_external_stream_selector = cls(
            type_=type_,
            source_id=source_id,
            stream_id=stream_id,
            filter_=filter_,
        )

        chronicle_external_stream_selector.additional_properties = d
        return chronicle_external_stream_selector

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
