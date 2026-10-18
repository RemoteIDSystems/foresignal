from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_test_spatial_type_0 import PrecursorTestSpatialType0
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_test_antecedent_type_0 import PrecursorTestAntecedentType0
    from ..models.precursor_test_antecedent_type_1 import PrecursorTestAntecedentType1
    from ..models.precursor_test_antecedent_type_2 import PrecursorTestAntecedentType2
    from ..models.precursor_test_antecedent_type_3 import PrecursorTestAntecedentType3
    from ..models.precursor_test_consequent_type_0 import PrecursorTestConsequentType0
    from ..models.precursor_test_consequent_type_1 import PrecursorTestConsequentType1
    from ..models.precursor_test_consequent_type_2 import PrecursorTestConsequentType2
    from ..models.precursor_test_consequent_type_3 import PrecursorTestConsequentType3
    from ..models.precursor_test_spatial_type_1 import PrecursorTestSpatialType1
    from ..models.precursor_test_spatial_type_2 import PrecursorTestSpatialType2


T = TypeVar("T", bound="PrecursorTest")


@_attrs_define
class PrecursorTest:
    """A stored precursor test (renamed from 'hypothesis'; 120 builds mining on it)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        name (str):
        antecedent (PrecursorTestAntecedentType0 | PrecursorTestAntecedentType1 | PrecursorTestAntecedentType2 |
            PrecursorTestAntecedentType3): Occurrence selector
        consequent (PrecursorTestConsequentType0 | PrecursorTestConsequentType1 | PrecursorTestConsequentType2 |
            PrecursorTestConsequentType3): Occurrence selector
        max_lag (str): Maximum lag, ≤ 30d. ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 6h.
        spatial (PrecursorTestSpatialType0 | PrecursorTestSpatialType1 | PrecursorTestSpatialType2): `sameEntity` |
            `{withinKm: n}` | `{sameRegion: country|admin1}` | `none`
        created_at (datetime.datetime):
        statement (str | Unset): Plain-language statement Example: GPS jamming precedes AIS gaps within 6 h within 100
            km.
        min_lag (str | Unset): Minimum lag (default 0). ISO-8601 duration or shorthand (30m, 6h, 7d). Example: 0s.
        dedup_window (str | Unset): Antecedent occurrences closer than this merge (default maxLag). ISO-8601 duration or
            shorthand (30m, 6h, 7d).
        created_by (str | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    name: str
    antecedent: (
        PrecursorTestAntecedentType0
        | PrecursorTestAntecedentType1
        | PrecursorTestAntecedentType2
        | PrecursorTestAntecedentType3
    )
    consequent: (
        PrecursorTestConsequentType0
        | PrecursorTestConsequentType1
        | PrecursorTestConsequentType2
        | PrecursorTestConsequentType3
    )
    max_lag: str
    spatial: (
        PrecursorTestSpatialType0
        | PrecursorTestSpatialType1
        | PrecursorTestSpatialType2
    )
    created_at: datetime.datetime
    statement: str | Unset = UNSET
    min_lag: str | Unset = UNSET
    dedup_window: str | Unset = UNSET
    created_by: str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.precursor_test_antecedent_type_0 import (
            PrecursorTestAntecedentType0,
        )
        from ..models.precursor_test_antecedent_type_1 import (
            PrecursorTestAntecedentType1,
        )
        from ..models.precursor_test_antecedent_type_2 import (
            PrecursorTestAntecedentType2,
        )
        from ..models.precursor_test_consequent_type_0 import (
            PrecursorTestConsequentType0,
        )
        from ..models.precursor_test_consequent_type_1 import (
            PrecursorTestConsequentType1,
        )
        from ..models.precursor_test_consequent_type_2 import (
            PrecursorTestConsequentType2,
        )
        from ..models.precursor_test_spatial_type_1 import PrecursorTestSpatialType1

        id = self.id

        name = self.name

        antecedent: dict[str, Any]
        if (
            isinstance(self.antecedent, PrecursorTestAntecedentType0)
            or isinstance(self.antecedent, PrecursorTestAntecedentType1)
            or isinstance(self.antecedent, PrecursorTestAntecedentType2)
        ):
            antecedent = self.antecedent.to_dict()
        else:
            antecedent = self.antecedent.to_dict()

        consequent: dict[str, Any]
        if (
            isinstance(self.consequent, PrecursorTestConsequentType0)
            or isinstance(self.consequent, PrecursorTestConsequentType1)
            or isinstance(self.consequent, PrecursorTestConsequentType2)
        ):
            consequent = self.consequent.to_dict()
        else:
            consequent = self.consequent.to_dict()

        max_lag = self.max_lag

        spatial: dict[str, Any] | str
        if isinstance(self.spatial, PrecursorTestSpatialType0):
            spatial = self.spatial.value
        elif isinstance(self.spatial, PrecursorTestSpatialType1):
            spatial = self.spatial.to_dict()
        else:
            spatial = self.spatial.to_dict()

        created_at = self.created_at.isoformat()

        statement = self.statement

        min_lag = self.min_lag

        dedup_window = self.dedup_window

        created_by = self.created_by

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "name": name,
                "antecedent": antecedent,
                "consequent": consequent,
                "maxLag": max_lag,
                "spatial": spatial,
                "createdAt": created_at,
            }
        )
        if statement is not UNSET:
            field_dict["statement"] = statement
        if min_lag is not UNSET:
            field_dict["minLag"] = min_lag
        if dedup_window is not UNSET:
            field_dict["dedupWindow"] = dedup_window
        if created_by is not UNSET:
            field_dict["createdBy"] = created_by
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_test_antecedent_type_0 import (
            PrecursorTestAntecedentType0,
        )
        from ..models.precursor_test_antecedent_type_1 import (
            PrecursorTestAntecedentType1,
        )
        from ..models.precursor_test_antecedent_type_2 import (
            PrecursorTestAntecedentType2,
        )
        from ..models.precursor_test_antecedent_type_3 import (
            PrecursorTestAntecedentType3,
        )
        from ..models.precursor_test_consequent_type_0 import (
            PrecursorTestConsequentType0,
        )
        from ..models.precursor_test_consequent_type_1 import (
            PrecursorTestConsequentType1,
        )
        from ..models.precursor_test_consequent_type_2 import (
            PrecursorTestConsequentType2,
        )
        from ..models.precursor_test_consequent_type_3 import (
            PrecursorTestConsequentType3,
        )
        from ..models.precursor_test_spatial_type_1 import PrecursorTestSpatialType1
        from ..models.precursor_test_spatial_type_2 import PrecursorTestSpatialType2

        d = dict(src_dict)
        id = d.pop("id")

        name = d.pop("name")

        def _parse_antecedent(
            data: object,
        ) -> (
            PrecursorTestAntecedentType0
            | PrecursorTestAntecedentType1
            | PrecursorTestAntecedentType2
            | PrecursorTestAntecedentType3
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_0 = PrecursorTestAntecedentType0.from_dict(data)

                return antecedent_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_1 = PrecursorTestAntecedentType1.from_dict(data)

                return antecedent_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                antecedent_type_2 = PrecursorTestAntecedentType2.from_dict(data)

                return antecedent_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            antecedent_type_3 = PrecursorTestAntecedentType3.from_dict(data)

            return antecedent_type_3

        antecedent = _parse_antecedent(d.pop("antecedent"))

        def _parse_consequent(
            data: object,
        ) -> (
            PrecursorTestConsequentType0
            | PrecursorTestConsequentType1
            | PrecursorTestConsequentType2
            | PrecursorTestConsequentType3
        ):
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_0 = PrecursorTestConsequentType0.from_dict(data)

                return consequent_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_1 = PrecursorTestConsequentType1.from_dict(data)

                return consequent_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                consequent_type_2 = PrecursorTestConsequentType2.from_dict(data)

                return consequent_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            consequent_type_3 = PrecursorTestConsequentType3.from_dict(data)

            return consequent_type_3

        consequent = _parse_consequent(d.pop("consequent"))

        max_lag = d.pop("maxLag")

        def _parse_spatial(
            data: object,
        ) -> (
            PrecursorTestSpatialType0
            | PrecursorTestSpatialType1
            | PrecursorTestSpatialType2
        ):
            try:
                if not isinstance(data, str):
                    raise TypeError()
                spatial_type_0 = PrecursorTestSpatialType0(data)

                return spatial_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                spatial_type_1 = PrecursorTestSpatialType1.from_dict(data)

                return spatial_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            spatial_type_2 = PrecursorTestSpatialType2.from_dict(data)

            return spatial_type_2

        spatial = _parse_spatial(d.pop("spatial"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        statement = d.pop("statement", UNSET)

        min_lag = d.pop("minLag", UNSET)

        dedup_window = d.pop("dedupWindow", UNSET)

        created_by = d.pop("createdBy", UNSET)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        precursor_test = cls(
            id=id,
            name=name,
            antecedent=antecedent,
            consequent=consequent,
            max_lag=max_lag,
            spatial=spatial,
            created_at=created_at,
            statement=statement,
            min_lag=min_lag,
            dedup_window=dedup_window,
            created_by=created_by,
            updated_at=updated_at,
        )

        precursor_test.additional_properties = d
        return precursor_test

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
