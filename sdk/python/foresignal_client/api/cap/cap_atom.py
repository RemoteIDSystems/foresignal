import datetime
from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.severity import Severity
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    rule_id: list[str] | Unset = UNSET,
    min_severity: Severity | Unset = UNSET,
    tags: list[str] | Unset = UNSET,
    place_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_rule_id: list[str] | Unset = UNSET
    if not isinstance(rule_id, Unset):
        json_rule_id = rule_id

    params["ruleId"] = json_rule_id

    json_min_severity: str | Unset = UNSET
    if not isinstance(min_severity, Unset):
        json_min_severity = min_severity.value

    params["minSeverity"] = json_min_severity

    json_tags: list[str] | Unset = UNSET
    if not isinstance(tags, Unset):
        json_tags = tags

    params["tags"] = json_tags

    params["placeId"] = place_id

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/cap/alerts.atom",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: list[str] | Unset = UNSET,
    min_severity: Severity | Unset = UNSET,
    tags: list[str] | Unset = UNSET,
    place_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """Atom index of current CAP messages

     Atom 1.0 feed with one entry per current CAP message (`<link rel="alternate"
    type="application/cap+xml">`). CAP messages are Public, carry the not-an-official-warning note, and
    use Alert, Update and Update/AllClear (never Cancel); exercise alerts have status Exercise. ETag and
    conditional GET; feed keys via `/v1/k/{key}/…`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[str] | Unset):
        min_severity (Severity | Unset):
        tags (list[str] | Unset):
        place_id (str | Unset):
        since (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        min_severity=min_severity,
        tags=tags,
        place_id=place_id,
        since=since,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    rule_id: list[str] | Unset = UNSET,
    min_severity: Severity | Unset = UNSET,
    tags: list[str] | Unset = UNSET,
    place_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | None:
    """Atom index of current CAP messages

     Atom 1.0 feed with one entry per current CAP message (`<link rel="alternate"
    type="application/cap+xml">`). CAP messages are Public, carry the not-an-official-warning note, and
    use Alert, Update and Update/AllClear (never Cancel); exercise alerts have status Exercise. ETag and
    conditional GET; feed keys via `/v1/k/{key}/…`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[str] | Unset):
        min_severity (Severity | Unset):
        tags (list[str] | Unset):
        place_id (str | Unset):
        since (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        rule_id=rule_id,
        min_severity=min_severity,
        tags=tags,
        place_id=place_id,
        since=since,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: list[str] | Unset = UNSET,
    min_severity: Severity | Unset = UNSET,
    tags: list[str] | Unset = UNSET,
    place_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem]:
    """Atom index of current CAP messages

     Atom 1.0 feed with one entry per current CAP message (`<link rel="alternate"
    type="application/cap+xml">`). CAP messages are Public, carry the not-an-official-warning note, and
    use Alert, Update and Update/AllClear (never Cancel); exercise alerts have status Exercise. ETag and
    conditional GET; feed keys via `/v1/k/{key}/…`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[str] | Unset):
        min_severity (Severity | Unset):
        tags (list[str] | Unset):
        place_id (str | Unset):
        since (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        min_severity=min_severity,
        tags=tags,
        place_id=place_id,
        since=since,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    rule_id: list[str] | Unset = UNSET,
    min_severity: Severity | Unset = UNSET,
    tags: list[str] | Unset = UNSET,
    place_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | None:
    """Atom index of current CAP messages

     Atom 1.0 feed with one entry per current CAP message (`<link rel="alternate"
    type="application/cap+xml">`). CAP messages are Public, carry the not-an-official-warning note, and
    use Alert, Update and Update/AllClear (never Cancel); exercise alerts have status Exercise. ETag and
    conditional GET; feed keys via `/v1/k/{key}/…`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[str] | Unset):
        min_severity (Severity | Unset):
        tags (list[str] | Unset):
        place_id (str | Unset):
        since (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            rule_id=rule_id,
            min_severity=min_severity,
            tags=tags,
            place_id=place_id,
            since=since,
            if_none_match=if_none_match,
        )
    ).parsed
