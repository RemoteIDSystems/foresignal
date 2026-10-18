from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_member_input_member_type import SituationMemberInputMemberType
from ..models.situation_member_input_role import SituationMemberInputRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationMemberInput")


@_attrs_define
class SituationMemberInput:
    """A member to add to a situation; records are pinned and verified by `seq`

    Attributes:
        member_type (SituationMemberInputMemberType):
        ref (str): Member id, or `sourceId/externalId` for records
        seq (int | None | Unset): Record version seq to pin (required for records)
        role (SituationMemberInputRole | Unset):  Default: SituationMemberInputRole.CORE.
        move_from_situation_id (None | str | Unset): Move the member from this situation (logged on both)
    """

    member_type: SituationMemberInputMemberType
    ref: str
    seq: int | None | Unset = UNSET
    role: SituationMemberInputRole | Unset = SituationMemberInputRole.CORE
    move_from_situation_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        member_type = self.member_type.value

        ref = self.ref

        seq: int | None | Unset
        if isinstance(self.seq, Unset):
            seq = UNSET
        else:
            seq = self.seq

        role: str | Unset = UNSET
        if not isinstance(self.role, Unset):
            role = self.role.value

        move_from_situation_id: None | str | Unset
        if isinstance(self.move_from_situation_id, Unset):
            move_from_situation_id = UNSET
        else:
            move_from_situation_id = self.move_from_situation_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "memberType": member_type,
                "ref": ref,
            }
        )
        if seq is not UNSET:
            field_dict["seq"] = seq
        if role is not UNSET:
            field_dict["role"] = role
        if move_from_situation_id is not UNSET:
            field_dict["moveFromSituationId"] = move_from_situation_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        member_type = SituationMemberInputMemberType(d.pop("memberType"))

        ref = d.pop("ref")

        def _parse_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seq = _parse_seq(d.pop("seq", UNSET))

        _role = d.pop("role", UNSET)
        role: SituationMemberInputRole | Unset
        if isinstance(_role, Unset):
            role = UNSET
        else:
            role = SituationMemberInputRole(_role)

        def _parse_move_from_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        move_from_situation_id = _parse_move_from_situation_id(
            d.pop("moveFromSituationId", UNSET)
        )

        situation_member_input = cls(
            member_type=member_type,
            ref=ref,
            seq=seq,
            role=role,
            move_from_situation_id=move_from_situation_id,
        )

        situation_member_input.additional_properties = d
        return situation_member_input

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
