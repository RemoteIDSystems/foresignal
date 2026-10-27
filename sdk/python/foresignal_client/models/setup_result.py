from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.session_token import SessionToken
    from ..models.user import User


T = TypeVar("T", bound="SetupResult")


@_attrs_define
class SetupResult:
    """Result of first-run setup

    Attributes:
        user (User): A platform user (user table, 164)
        session (None | SessionToken | Unset): A session for the new admin; null only if the sign-in asked for a second
            factor
    """

    user: User
    session: None | SessionToken | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.session_token import SessionToken

        user = self.user.to_dict()

        session: dict[str, Any] | None | Unset
        if isinstance(self.session, Unset):
            session = UNSET
        elif isinstance(self.session, SessionToken):
            session = self.session.to_dict()
        else:
            session = self.session

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "user": user,
            }
        )
        if session is not UNSET:
            field_dict["session"] = session

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.session_token import SessionToken
        from ..models.user import User

        d = dict(src_dict)
        user = User.from_dict(d.pop("user"))

        def _parse_session(data: object) -> None | SessionToken | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                session_type_0 = SessionToken.from_dict(data)

                return session_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SessionToken | Unset, data)

        session = _parse_session(d.pop("session", UNSET))

        setup_result = cls(
            user=user,
            session=session,
        )

        setup_result.additional_properties = d
        return setup_result

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
