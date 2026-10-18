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
        "url": "/v1/briefings/{briefing_id}/run".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BriefingRun | Problem | None:
    if response.status_code == 200:
        response_200 = BriefingRun.from_dict(response.json())

        return response_200

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
    """Run a briefing now

     Runs synchronously (up to 120 s), stores the run and delivers it to the briefing's channels. A run
    still going after 120 s is returned with status `running` and completes in the background. In LLM
    mode any model failure, refusal or exhausted LLM budget produces a `fallback` run (the digest, with
    the reason in `error` and `warnings`), not an error.

    **Scope:** `agent` or `admin`.

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
    """Run a briefing now

     Runs synchronously (up to 120 s), stores the run and delivers it to the briefing's channels. A run
    still going after 120 s is returned with status `running` and completes in the background. In LLM
    mode any model failure, refusal or exhausted LLM budget produces a `fallback` run (the digest, with
    the reason in `error` and `warnings`), not an error.

    **Scope:** `agent` or `admin`.

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
    """Run a briefing now

     Runs synchronously (up to 120 s), stores the run and delivers it to the briefing's channels. A run
    still going after 120 s is returned with status `running` and completes in the background. In LLM
    mode any model failure, refusal or exhausted LLM budget produces a `fallback` run (the digest, with
    the reason in `error` and `warnings`), not an error.

    **Scope:** `agent` or `admin`.

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
    """Run a briefing now

     Runs synchronously (up to 120 s), stores the run and delivers it to the briefing's channels. A run
    still going after 120 s is returned with status `running` and completes in the background. In LLM
    mode any model failure, refusal or exhausted LLM budget produces a `fallback` run (the digest, with
    the reason in `error` and `warnings`), not an error.

    **Scope:** `agent` or `admin`.

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
