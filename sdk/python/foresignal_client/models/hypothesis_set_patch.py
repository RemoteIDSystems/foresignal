from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_set_patch_cadence import HypothesisSetPatchCadence
from ..models.hypothesis_set_patch_status import HypothesisSetPatchStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="HypothesisSetPatch")


@_attrs_define
class HypothesisSetPatch:
    """JSON merge patch for a set's description, cadence or status

    Attributes:
        version (int): Optimistic lock (409 HYPOTHESIS_SET_VERSION_CONFLICT)
        description (None | str | Unset):
        cadence (HypothesisSetPatchCadence | Unset):
        status (HypothesisSetPatchStatus | Unset):
    """

    version: int
    description: None | str | Unset = UNSET
    cadence: HypothesisSetPatchCadence | Unset = UNSET
    status: HypothesisSetPatchStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        version = self.version

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        cadence: str | Unset = UNSET
        if not isinstance(self.cadence, Unset):
            cadence = self.cadence.value

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if cadence is not UNSET:
            field_dict["cadence"] = cadence
        if status is not UNSET:
            field_dict["status"] = status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        version = d.pop("version")

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        _cadence = d.pop("cadence", UNSET)
        cadence: HypothesisSetPatchCadence | Unset
        if isinstance(_cadence, Unset):
            cadence = UNSET
        else:
            cadence = HypothesisSetPatchCadence(_cadence)

        _status = d.pop("status", UNSET)
        status: HypothesisSetPatchStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = HypothesisSetPatchStatus(_status)

        hypothesis_set_patch = cls(
            version=version,
            description=description,
            cadence=cadence,
            status=status,
        )

        hypothesis_set_patch.additional_properties = d
        return hypothesis_set_patch

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
