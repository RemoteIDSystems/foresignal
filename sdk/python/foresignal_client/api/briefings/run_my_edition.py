from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing_run import BriefingRun
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    briefing_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/me/editions/{briefing_id}/run".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BriefingRun | Problem | None:
    if response.status_code == 202:
        response_202 = BriefingRun.from_dict(response.json())

        return response_202

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
) -> Response[BriefingRun | Problem]:
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
) -> Response[BriefingRun | Problem]:
    """Run my edition now

     Runs an edition the calling user owns on demand (2.1.1), producing a run delivered to the edition's
    targets. 404 unless the edition exists and the caller owns it. Any signed-in user holding
    `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRun | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> BriefingRun | Problem | None:
    """Run my edition now

     Runs an edition the calling user owns on demand (2.1.1), producing a run delivered to the edition's
    targets. 404 unless the edition exists and the caller owns it. Any signed-in user holding
    `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRun | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[BriefingRun | Problem]:
    """Run my edition now

     Runs an edition the calling user owns on demand (2.1.1), producing a run delivered to the edition's
    targets. 404 unless the edition exists and the caller owns it. Any signed-in user holding
    `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRun | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> BriefingRun | Problem | None:
    """Run my edition now

     Runs an edition the calling user owns on demand (2.1.1), producing a run delivered to the edition's
    targets. 404 unless the edition exists and the caller owns it. Any signed-in user holding
    `self.editions`.

    **Scope:** `read`.

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRun | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            client=client,
        )
    ).parsed
