from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.receive_web_sub_notification_json_body import (
    ReceiveWebSubNotificationJsonBody,
)
from ...types import Response


def _get_kwargs(
    source_id: str,
    token: str,
    *,
    body: ReceiveWebSubNotificationJsonBody,
    x_hub_signature: str,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    headers["X-Hub-Signature"] = x_hub_signature

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/websub/{source_id}/{token}".format(
            source_id=quote(str(source_id), safe=""),
            token=quote(str(token), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/feed+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 202:
        response_202 = cast(Any, None)
        return response_202

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

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
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    body: ReceiveWebSubNotificationJsonBody,
    x_hub_signature: str,
) -> Response[Any | Problem]:
    """WebSub content distribution

     Hub content delivery for a subscribed feed. `X-Hub-Signature` (`sha256=` preferred, `sha1=`
    accepted) is verified over the raw body with a constant-time compare; the body (≤ 2 MB) is parsed
    with the feed-kit `FeedParser` and handed to the source's sink. Per-source cap 60/min.

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        x_hub_signature (str):
        body (ReceiveWebSubNotificationJsonBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        token=token,
        body=body,
        x_hub_signature=x_hub_signature,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    body: ReceiveWebSubNotificationJsonBody,
    x_hub_signature: str,
) -> Any | Problem | None:
    """WebSub content distribution

     Hub content delivery for a subscribed feed. `X-Hub-Signature` (`sha256=` preferred, `sha1=`
    accepted) is verified over the raw body with a constant-time compare; the body (≤ 2 MB) is parsed
    with the feed-kit `FeedParser` and handed to the source's sink. Per-source cap 60/min.

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        x_hub_signature (str):
        body (ReceiveWebSubNotificationJsonBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        source_id=source_id,
        token=token,
        client=client,
        body=body,
        x_hub_signature=x_hub_signature,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    body: ReceiveWebSubNotificationJsonBody,
    x_hub_signature: str,
) -> Response[Any | Problem]:
    """WebSub content distribution

     Hub content delivery for a subscribed feed. `X-Hub-Signature` (`sha256=` preferred, `sha1=`
    accepted) is verified over the raw body with a constant-time compare; the body (≤ 2 MB) is parsed
    with the feed-kit `FeedParser` and handed to the source's sink. Per-source cap 60/min.

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        x_hub_signature (str):
        body (ReceiveWebSubNotificationJsonBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        token=token,
        body=body,
        x_hub_signature=x_hub_signature,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    token: str,
    *,
    client: AuthenticatedClient | Client,
    body: ReceiveWebSubNotificationJsonBody,
    x_hub_signature: str,
) -> Any | Problem | None:
    """WebSub content distribution

     Hub content delivery for a subscribed feed. `X-Hub-Signature` (`sha256=` preferred, `sha1=`
    accepted) is verified over the raw body with a constant-time compare; the body (≤ 2 MB) is parsed
    with the feed-kit `FeedParser` and handed to the source's sink. Per-source cap 60/min.

    **Scope:** `open` (no token; the unguessable path token identifies the subscription).

    Args:
        source_id (str):
        token (str):
        x_hub_signature (str):
        body (ReceiveWebSubNotificationJsonBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            token=token,
            client=client,
            body=body,
            x_hub_signature=x_hub_signature,
        )
    ).parsed
