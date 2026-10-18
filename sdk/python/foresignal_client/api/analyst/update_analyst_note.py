from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analyst_note import AnalystNote
from ...models.analyst_note_patch import AnalystNotePatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    note_id: str,
    *,
    body: AnalystNotePatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/analyst/notes/{note_id}".format(
            note_id=quote(str(note_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalystNote | Problem | None:
    if response.status_code == 200:
        response_200 = AnalystNote.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalystNote | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    note_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystNotePatch,
) -> Response[AnalystNote | Problem]:
    """Mark a note read or dismissed

     Changes the note status.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        note_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystNotePatch): Note status change

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystNote | Problem]
    """

    kwargs = _get_kwargs(
        note_id=note_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    note_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystNotePatch,
) -> AnalystNote | Problem | None:
    """Mark a note read or dismissed

     Changes the note status.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        note_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystNotePatch): Note status change

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystNote | Problem
    """

    return sync_detailed(
        note_id=note_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    note_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystNotePatch,
) -> Response[AnalystNote | Problem]:
    """Mark a note read or dismissed

     Changes the note status.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        note_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystNotePatch): Note status change

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystNote | Problem]
    """

    kwargs = _get_kwargs(
        note_id=note_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    note_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalystNotePatch,
) -> AnalystNote | Problem | None:
    """Mark a note read or dismissed

     Changes the note status.

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        note_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalystNotePatch): Note status change

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystNote | Problem
    """

    return (
        await asyncio_detailed(
            note_id=note_id,
            client=client,
            body=body,
        )
    ).parsed
