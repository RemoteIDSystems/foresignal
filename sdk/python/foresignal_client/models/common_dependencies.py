from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.common_dependencies_dependencies_item import (
        CommonDependenciesDependenciesItem,
    )


T = TypeVar("T", bound="CommonDependencies")


@_attrs_define
class CommonDependencies:
    """Facilities and infrastructure shared through `depends-on` and `located-in` paths

    Attributes:
        refs (list[str]):
        dependencies (list[CommonDependenciesDependenciesItem]):
        as_of (datetime.datetime | None | Unset):
        generated_at (datetime.datetime | Unset):
    """

    refs: list[str]
    dependencies: list[CommonDependenciesDependenciesItem]
    as_of: datetime.datetime | None | Unset = UNSET
    generated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        refs = self.refs

        dependencies = []
        for dependencies_item_data in self.dependencies:
            dependencies_item = dependencies_item_data.to_dict()
            dependencies.append(dependencies_item)

        as_of: None | str | Unset
        if isinstance(self.as_of, Unset):
            as_of = UNSET
        elif isinstance(self.as_of, datetime.datetime):
            as_of = self.as_of.isoformat()
        else:
            as_of = self.as_of

        generated_at: str | Unset = UNSET
        if not isinstance(self.generated_at, Unset):
            generated_at = self.generated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "refs": refs,
                "dependencies": dependencies,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if generated_at is not UNSET:
            field_dict["generatedAt"] = generated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.common_dependencies_dependencies_item import (
            CommonDependenciesDependenciesItem,
        )

        d = dict(src_dict)
        refs = cast(list[str], d.pop("refs"))

        dependencies = []
        _dependencies = d.pop("dependencies")
        for dependencies_item_data in _dependencies:
            dependencies_item = CommonDependenciesDependenciesItem.from_dict(
                dependencies_item_data
            )

            dependencies.append(dependencies_item)

        def _parse_as_of(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                as_of_type_0 = datetime.datetime.fromisoformat(data)

                return as_of_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        as_of = _parse_as_of(d.pop("asOf", UNSET))

        _generated_at = d.pop("generatedAt", UNSET)
        generated_at: datetime.datetime | Unset
        if isinstance(_generated_at, Unset):
            generated_at = UNSET
        else:
            generated_at = datetime.datetime.fromisoformat(_generated_at)

        common_dependencies = cls(
            refs=refs,
            dependencies=dependencies,
            as_of=as_of,
            generated_at=generated_at,
        )

        common_dependencies.additional_properties = d
        return common_dependencies

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
