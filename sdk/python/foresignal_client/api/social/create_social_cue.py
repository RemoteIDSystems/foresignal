from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.social_cue import SocialCue
from ...types import Response


def _get_kwargs(
    *,
    body: Any,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/social/cues",
    }

    _kwargs["json"] = body

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialCue | None:
    if response.status_code == 202:
        response_202 = SocialCue.from_dict(response.json())

        return response_202

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SocialCue]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Response[Problem | SocialCue]:
    """Run a manual social cue

     Queues a targeted search built from the place, area, entity or situation (names and multilingual
    aliases, nearby facilities, family lexicon terms in the region's languages) and extra terms, on
    enabled search-capable platforms within the cost cap. Results are stored as posts with `collection =
    cue`, joined into narratives and compared; they never feed baselines. Emits `cue.completed`.

    **Scope:** `agent`.

    Args:
        body (Any): A manual cue: a targeted, budgeted social search around a place, area, entity
            or situation (agent)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialCue]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Problem | SocialCue | None:
    """Run a manual social cue

     Queues a targeted search built from the place, area, entity or situation (names and multilingual
    aliases, nearby facilities, family lexicon terms in the region's languages) and extra terms, on
    enabled search-capable platforms within the cost cap. Results are stored as posts with `collection =
    cue`, joined into narratives and compared; they never feed baselines. Emits `cue.completed`.

    **Scope:** `agent`.

    Args:
        body (Any): A manual cue: a targeted, budgeted social search around a place, area, entity
            or situation (agent)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialCue
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Response[Problem | SocialCue]:
    """Run a manual social cue

     Queues a targeted search built from the place, area, entity or situation (names and multilingual
    aliases, nearby facilities, family lexicon terms in the region's languages) and extra terms, on
    enabled search-capable platforms within the cost cap. Results are stored as posts with `collection =
    cue`, joined into narratives and compared; they never feed baselines. Emits `cue.completed`.

    **Scope:** `agent`.

    Args:
        body (Any): A manual cue: a targeted, budgeted social search around a place, area, entity
            or situation (agent)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialCue]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Problem | SocialCue | None:
    """Run a manual social cue

     Queues a targeted search built from the place, area, entity or situation (names and multilingual
    aliases, nearby facilities, family lexicon terms in the region's languages) and extra terms, on
    enabled search-capable platforms within the cost cap. Results are stored as posts with `collection =
    cue`, joined into narratives and compared; they never feed baselines. Emits `cue.completed`.

    **Scope:** `agent`.

    Args:
        body (Any): A manual cue: a targeted, budgeted social search around a place, area, entity
            or situation (agent)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialCue
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
