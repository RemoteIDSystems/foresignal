from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.opaque_chronicle_link_access_request_status_type_1 import (
    OpaqueChronicleLinkAccessRequestStatusType1,
)
from ..models.opaque_chronicle_link_access_request_status_type_2_type_1 import (
    OpaqueChronicleLinkAccessRequestStatusType2Type1,
)
from ..models.opaque_chronicle_link_access_request_status_type_3_type_1 import (
    OpaqueChronicleLinkAccessRequestStatusType3Type1,
)
from ..models.opaque_chronicle_link_dimensions_item import (
    OpaqueChronicleLinkDimensionsItem,
)
from ..models.opaque_chronicle_link_strength import OpaqueChronicleLinkStrength
from ..models.opaque_chronicle_link_time_overlap import OpaqueChronicleLinkTimeOverlap
from ..types import UNSET, Unset

T = TypeVar("T", bound="OpaqueChronicleLink")


@_attrs_define
class OpaqueChronicleLink:
    """A relationship to a chronicle the caller cannot see: strength band and dimensions only; title and owner only if
    revealed

        Attributes:
            link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            disclosure (Literal['opaque']):
            strength (OpaqueChronicleLinkStrength): From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
            dimensions (list[OpaqueChronicleLinkDimensionsItem]):
            time_overlap (OpaqueChronicleLinkTimeOverlap):
            chronicle_id (str | Unset): The caller's own chronicle in this link
            remote (bool | Unset):
            owner_handle (None | str | Unset):
            title (None | str | Unset):
            access_request_status (None | OpaqueChronicleLinkAccessRequestStatusType1 |
                OpaqueChronicleLinkAccessRequestStatusType2Type1 | OpaqueChronicleLinkAccessRequestStatusType3Type1 | Unset):
    """

    link_id: str
    disclosure: Literal["opaque"]
    strength: OpaqueChronicleLinkStrength
    dimensions: list[OpaqueChronicleLinkDimensionsItem]
    time_overlap: OpaqueChronicleLinkTimeOverlap
    chronicle_id: str | Unset = UNSET
    remote: bool | Unset = UNSET
    owner_handle: None | str | Unset = UNSET
    title: None | str | Unset = UNSET
    access_request_status: (
        None
        | OpaqueChronicleLinkAccessRequestStatusType1
        | OpaqueChronicleLinkAccessRequestStatusType2Type1
        | OpaqueChronicleLinkAccessRequestStatusType3Type1
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        link_id = self.link_id

        disclosure = self.disclosure

        strength = self.strength.value

        dimensions = []
        for dimensions_item_data in self.dimensions:
            dimensions_item = dimensions_item_data.value
            dimensions.append(dimensions_item)

        time_overlap = self.time_overlap.value

        chronicle_id = self.chronicle_id

        remote = self.remote

        owner_handle: None | str | Unset
        if isinstance(self.owner_handle, Unset):
            owner_handle = UNSET
        else:
            owner_handle = self.owner_handle

        title: None | str | Unset
        if isinstance(self.title, Unset):
            title = UNSET
        else:
            title = self.title

        access_request_status: None | str | Unset
        if isinstance(self.access_request_status, Unset):
            access_request_status = UNSET
        elif (
            isinstance(
                self.access_request_status, OpaqueChronicleLinkAccessRequestStatusType1
            )
            or isinstance(
                self.access_request_status,
                OpaqueChronicleLinkAccessRequestStatusType2Type1,
            )
            or isinstance(
                self.access_request_status,
                OpaqueChronicleLinkAccessRequestStatusType3Type1,
            )
        ):
            access_request_status = self.access_request_status.value
        else:
            access_request_status = self.access_request_status

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "linkId": link_id,
                "disclosure": disclosure,
                "strength": strength,
                "dimensions": dimensions,
                "timeOverlap": time_overlap,
            }
        )
        if chronicle_id is not UNSET:
            field_dict["chronicleId"] = chronicle_id
        if remote is not UNSET:
            field_dict["remote"] = remote
        if owner_handle is not UNSET:
            field_dict["ownerHandle"] = owner_handle
        if title is not UNSET:
            field_dict["title"] = title
        if access_request_status is not UNSET:
            field_dict["accessRequestStatus"] = access_request_status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        link_id = d.pop("linkId")

        disclosure = cast(Literal["opaque"], d.pop("disclosure"))
        if disclosure != "opaque":
            raise ValueError(
                f"disclosure must match const 'opaque', got '{disclosure}'"
            )

        strength = OpaqueChronicleLinkStrength(d.pop("strength"))

        dimensions = []
        _dimensions = d.pop("dimensions")
        for dimensions_item_data in _dimensions:
            dimensions_item = OpaqueChronicleLinkDimensionsItem(dimensions_item_data)

            dimensions.append(dimensions_item)

        time_overlap = OpaqueChronicleLinkTimeOverlap(d.pop("timeOverlap"))

        chronicle_id = d.pop("chronicleId", UNSET)

        remote = d.pop("remote", UNSET)

        def _parse_owner_handle(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        owner_handle = _parse_owner_handle(d.pop("ownerHandle", UNSET))

        def _parse_title(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        title = _parse_title(d.pop("title", UNSET))

        def _parse_access_request_status(
            data: object,
        ) -> (
            None
            | OpaqueChronicleLinkAccessRequestStatusType1
            | OpaqueChronicleLinkAccessRequestStatusType2Type1
            | OpaqueChronicleLinkAccessRequestStatusType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                access_request_status_type_1 = (
                    OpaqueChronicleLinkAccessRequestStatusType1(data)
                )

                return access_request_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                access_request_status_type_2_type_1 = (
                    OpaqueChronicleLinkAccessRequestStatusType2Type1(data)
                )

                return access_request_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                access_request_status_type_3_type_1 = (
                    OpaqueChronicleLinkAccessRequestStatusType3Type1(data)
                )

                return access_request_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | OpaqueChronicleLinkAccessRequestStatusType1
                | OpaqueChronicleLinkAccessRequestStatusType2Type1
                | OpaqueChronicleLinkAccessRequestStatusType3Type1
                | Unset,
                data,
            )

        access_request_status = _parse_access_request_status(
            d.pop("accessRequestStatus", UNSET)
        )

        opaque_chronicle_link = cls(
            link_id=link_id,
            disclosure=disclosure,
            strength=strength,
            dimensions=dimensions,
            time_overlap=time_overlap,
            chronicle_id=chronicle_id,
            remote=remote,
            owner_handle=owner_handle,
            title=title,
            access_request_status=access_request_status,
        )

        opaque_chronicle_link.additional_properties = d
        return opaque_chronicle_link

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
