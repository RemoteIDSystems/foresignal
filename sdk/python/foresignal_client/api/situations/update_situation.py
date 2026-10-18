from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation import Situation
from ...models.situation_patch import SituationPatch
from ...types import Response


def _get_kwargs(
    situation_id: str,
    *,
    body: SituationPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/situations/{situation_id}".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Situation | None:
    if response.status_code == 200:
        response_200 = Situation.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Situation]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationPatch,
) -> Response[Problem | Situation]:
    """Update a situation

     JSON merge patch: rename, set theme, override severity (null clears it), activate, reopen a dormant
    situation or close it; every change is logged with the actor. A stale `version` returns 409
    `SITUATION_VERSION_CONFLICT`; changing a closed situation returns 409 `SITUATION_CLOSED` (closed is
    final).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationPatch): JSON merge patch for a situation; every change is logged with the
            actor

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationPatch,
) -> Problem | Situation | None:
    """Update a situation

     JSON merge patch: rename, set theme, override severity (null clears it), activate, reopen a dormant
    situation or close it; every change is logged with the actor. A stale `version` returns 409
    `SITUATION_VERSION_CONFLICT`; changing a closed situation returns 409 `SITUATION_CLOSED` (closed is
    final).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationPatch): JSON merge patch for a situation; every change is logged with the
            actor

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationPatch,
) -> Response[Problem | Situation]:
    """Update a situation

     JSON merge patch: rename, set theme, override severity (null clears it), activate, reopen a dormant
    situation or close it; every change is logged with the actor. A stale `version` returns 409
    `SITUATION_VERSION_CONFLICT`; changing a closed situation returns 409 `SITUATION_CLOSED` (closed is
    final).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationPatch): JSON merge patch for a situation; every change is logged with the
            actor

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    body: SituationPatch,
) -> Problem | Situation | None:
    """Update a situation

     JSON merge patch: rename, set theme, override severity (null clears it), activate, reopen a dormant
    situation or close it; every change is logged with the actor. A stale `version` returns 409
    `SITUATION_VERSION_CONFLICT`; changing a closed situation returns 409 `SITUATION_CLOSED` (closed is
    final).

    **Scope:** `agent` or `admin`.

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (SituationPatch): JSON merge patch for a situation; every change is logged with the
            actor

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
            body=body,
        )
    ).parsed
