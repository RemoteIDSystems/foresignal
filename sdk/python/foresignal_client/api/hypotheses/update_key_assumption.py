from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.key_assumption import KeyAssumption
from ...models.key_assumption_patch import KeyAssumptionPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    set_id: str,
    assumption_id: str,
    *,
    body: KeyAssumptionPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/hypotheses/sets/{set_id}/assumptions/{assumption_id}".format(
            set_id=quote(str(set_id), safe=""),
            assumption_id=quote(str(assumption_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> KeyAssumption | Problem | None:
    if response.status_code == 200:
        response_200 = KeyAssumption.from_dict(response.json())

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

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[KeyAssumption | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    set_id: str,
    assumption_id: str,
    *,
    client: AuthenticatedClient,
    body: KeyAssumptionPatch,
) -> Response[KeyAssumption | Problem]:
    """Update a key assumption

     JSON merge patch. A broken linchpin flags the set for review, lowers confidence one step and emits
    `assumption.broken`.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (KeyAssumptionPatch): JSON merge patch for a key assumption; `broken` emits
            `assumption.broken`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[KeyAssumption | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        assumption_id=assumption_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    set_id: str,
    assumption_id: str,
    *,
    client: AuthenticatedClient,
    body: KeyAssumptionPatch,
) -> KeyAssumption | Problem | None:
    """Update a key assumption

     JSON merge patch. A broken linchpin flags the set for review, lowers confidence one step and emits
    `assumption.broken`.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (KeyAssumptionPatch): JSON merge patch for a key assumption; `broken` emits
            `assumption.broken`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        KeyAssumption | Problem
    """

    return sync_detailed(
        set_id=set_id,
        assumption_id=assumption_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    set_id: str,
    assumption_id: str,
    *,
    client: AuthenticatedClient,
    body: KeyAssumptionPatch,
) -> Response[KeyAssumption | Problem]:
    """Update a key assumption

     JSON merge patch. A broken linchpin flags the set for review, lowers confidence one step and emits
    `assumption.broken`.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (KeyAssumptionPatch): JSON merge patch for a key assumption; `broken` emits
            `assumption.broken`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[KeyAssumption | Problem]
    """

    kwargs = _get_kwargs(
        set_id=set_id,
        assumption_id=assumption_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    set_id: str,
    assumption_id: str,
    *,
    client: AuthenticatedClient,
    body: KeyAssumptionPatch,
) -> KeyAssumption | Problem | None:
    """Update a key assumption

     JSON merge patch. A broken linchpin flags the set for review, lowers confidence one step and emits
    `assumption.broken`.

    **Scope:** `agent`.

    Args:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        assumption_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (KeyAssumptionPatch): JSON merge patch for a key assumption; `broken` emits
            `assumption.broken`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        KeyAssumption | Problem
    """

    return (
        await asyncio_detailed(
            set_id=set_id,
            assumption_id=assumption_id,
            client=client,
            body=body,
        )
    ).parsed
