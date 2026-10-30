from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing import Briefing
from ...models.briefing_input import BriefingInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    briefing_id: UUID,
    *,
    body: BriefingInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/me/editions/{briefing_id}".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Briefing | Problem | None:
    if response.status_code == 200:
        response_200 = Briefing.from_dict(response.json())

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
) -> Response[Briefing | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Response[Briefing | Problem]:
    """Update my edition

     Updates an edition the calling user owns (2.1.1). The body is a `BriefingInput`; `ownerUserId` is
    forced to the caller. 404 unless the edition exists and the caller owns it. Any signed-in user
    holding `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Briefing | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Briefing | Problem | None:
    """Update my edition

     Updates an edition the calling user owns (2.1.1). The body is a `BriefingInput`; `ownerUserId` is
    forced to the caller. 404 unless the edition exists and the caller owns it. Any signed-in user
    holding `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Briefing | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Response[Briefing | Problem]:
    """Update my edition

     Updates an edition the calling user owns (2.1.1). The body is a `BriefingInput`; `ownerUserId` is
    forced to the caller. 404 unless the edition exists and the caller owns it. Any signed-in user
    holding `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Briefing | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Briefing | Problem | None:
    """Update my edition

     Updates an edition the calling user owns (2.1.1). The body is a `BriefingInput`; `ownerUserId` is
    forced to the caller. 404 unless the edition exists and the caller owns it. Any signed-in user
    holding `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Briefing | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            client=client,
            body=body,
        )
    ).parsed
