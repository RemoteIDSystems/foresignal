from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="SituationChangesSeverity")


@_attrs_define
class SituationChangesSeverity:
    """Severity before and after

    Attributes:
        after (Severity):
        changed (bool):
        before (None | Severity | Unset):
    """

    after: Severity
    changed: bool
    before: None | Severity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        after = self.after.value

        changed = self.changed

        before: None | str | Unset
        if isinstance(self.before, Unset):
            before = UNSET
        elif isinstance(self.before, Severity):
            before = self.before.value
        else:
            before = self.before

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "after": after,
                "changed": changed,
            }
        )
        if before is not UNSET:
            field_dict["before"] = before

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        after = Severity(d.pop("after"))

        changed = d.pop("changed")

        def _parse_before(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                before_type_0 = Severity(data)

                return before_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        before = _parse_before(d.pop("before", UNSET))

        situation_changes_severity = cls(
            after=after,
            changed=changed,
            before=before,
        )

        situation_changes_severity.additional_properties = d
        return situation_changes_severity

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
