from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.compact_message import CompactMessage
from ...models.federation_ingest_result import FederationIngestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: list[CompactMessage],
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/federation/bundle",
    }

    _kwargs["content"] = body.payload
    headers["Content-Type"] = "application/cbor"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FederationIngestResult | Problem | None:
    if response.status_code == 200:
        response_200 = FederationIngestResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FederationIngestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: list[CompactMessage],
) -> Response[FederationIngestResult | Problem]:
    """Receive a compact message bundle

     Accepts a CBOR array of CompactMessages from a peer and decodes them into `remote-event` records on
    `peer-{id}-events` (reliability F, quality flag compact). Messages whose origin (keys 10, 11) is
    this instance or already seen are dropped; received messages are never re-federated.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        body (list[CompactMessage]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationIngestResult | Problem]
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
    body: list[CompactMessage],
) -> FederationIngestResult | Problem | None:
    """Receive a compact message bundle

     Accepts a CBOR array of CompactMessages from a peer and decodes them into `remote-event` records on
    `peer-{id}-events` (reliability F, quality flag compact). Messages whose origin (keys 10, 11) is
    this instance or already seen are dropped; received messages are never re-federated.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        body (list[CompactMessage]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationIngestResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: list[CompactMessage],
) -> Response[FederationIngestResult | Problem]:
    """Receive a compact message bundle

     Accepts a CBOR array of CompactMessages from a peer and decodes them into `remote-event` records on
    `peer-{id}-events` (reliability F, quality flag compact). Messages whose origin (keys 10, 11) is
    this instance or already seen are dropped; received messages are never re-federated.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        body (list[CompactMessage]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationIngestResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: list[CompactMessage],
) -> FederationIngestResult | Problem | None:
    """Receive a compact message bundle

     Accepts a CBOR array of CompactMessages from a peer and decodes them into `remote-event` records on
    `peer-{id}-events` (reliability F, quality flag compact). Messages whose origin (keys 10, 11) is
    this instance or already seen are dropped; received messages are never re-federated.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        body (list[CompactMessage]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationIngestResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
